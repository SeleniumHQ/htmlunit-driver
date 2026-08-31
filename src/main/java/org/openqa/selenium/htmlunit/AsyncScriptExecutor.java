// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.htmlunit;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.htmlunit.ScriptException;
import org.htmlunit.ScriptResult;
import org.htmlunit.corejs.javascript.Function;
import org.htmlunit.corejs.javascript.NativeJavaObject;
import org.htmlunit.corejs.javascript.ScriptableObject;
import org.htmlunit.corejs.javascript.VarScope;
import org.htmlunit.corejs.javascript.lc.type.TypeInfo;
import org.htmlunit.corejs.javascript.lc.type.TypeInfoFactory;
import org.htmlunit.html.HtmlPage;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.ScriptTimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriverException;

/**
 * Injects an asynchronous script into the current page for execution. The
 * script signals completion by invoking the callback function, which is always
 * the last argument passed to the injected script.
 *
 * <p>Thread-safety model: {@link #execute(String, Object[])} is called from the
 * WebDriver/test thread. {@link #alertTriggered(String)} is called from
 * HtmlUnit's alert-handler thread. The injected script's callback, timeout, and
 * unload signals are invoked from HtmlUnit's JavaScript thread. All cross-thread
 * communication is mediated by {@link AsyncScriptResult}, which is designed to
 * be safe for concurrent use by these threads.</p>
 *
 * @author Alexei Barantsev
 * @author Ahmed Ashour
 * @author Simon Stewart
 * @author Ronald Brill
 */
class AsyncScriptExecutor {

    private final HtmlPage page_;
    private final long timeoutMillis_;

    /**
     * Holds the result object for the script currently being executed.
     *
     * <p>Written by the WebDriver thread inside {@link #execute(String, Object[])}
     * and read by HtmlUnit's alert-handler thread inside
     * {@link #alertTriggered(String)}. Declared {@code volatile} to ensure the
     * alert-handler thread always sees the most recently published value and never
     * operates on a stale {@code null} or a previous execution's result.</p>
     */
    private volatile AsyncScriptResult asyncResult_;

    /**
     * Prepares a new asynchronous script executor for the given page.
     *
     * @param page          the page to inject the script into; must not be {@code null}
     * @param timeoutMillis how long to wait for the script to complete, in milliseconds;
     *                      must be positive
     */
    AsyncScriptExecutor(final HtmlPage page, final long timeoutMillis) {
        page_ = page;
        timeoutMillis_ = timeoutMillis;
    }

    /**
     * Forwards an alert notification to the currently executing async script result.
     * Called from HtmlUnit's alert-handler thread when {@code window.alert()} fires
     * while an async script is running.
     *
     * <p>If no script is currently executing (i.e. {@link #asyncResult_} is
     * {@code null}), this method is a no-op.</p>
     *
     * @param message the alert message text
     */
    void alertTriggered(final String message) {
        final AsyncScriptResult result = asyncResult_;
        if (result != null) {
            result.alert(message);
        }
    }

    /**
     * Injects an asynchronous script into the current page and blocks until it
     * signals completion, a timeout occurs, an alert fires, or a page unload is
     * detected.
     *
     * <p>The interrupt flag is preserved: if {@link InterruptedException} is thrown
     * while waiting for the result, the thread's interrupt status is restored before
     * rethrowing as {@link WebDriverException}.</p>
     *
     * @param scriptBody the body of the script to execute; must not be {@code null}
     * @param parameters the script parameters, accessible via the {@code arguments}
     *                   JavaScript object; must not be {@code null}
     * @return the value passed to the script's callback function, or {@code null}
     *         if the callback was invoked with {@code undefined}
     * @throws WebDriverException       if the script throws a JavaScript exception,
     *                                  or if the waiting thread is interrupted
     * @throws ScriptTimeoutException   if the script does not complete within
     *                                  {@code timeoutMillis}
     * @throws UnhandledAlertException  if a {@code window.alert()} fires during execution
     * @throws JavascriptException      if a page unload is detected during execution
     */
    public Object execute(final String scriptBody, final Object[] parameters) {
        try {
            asyncResult_ = new AsyncScriptResult();
            final Function function = createInjectedScriptFunction(scriptBody, asyncResult_);

            try {
                page_.executeJavaScriptFunction(function, function, parameters, page_.getDocumentElement());
            }
            catch (final ScriptException e) {
                throw new WebDriverException(e);
            }

            try {
                return asyncResult_.waitForResult(timeoutMillis_);
            }
            catch (final InterruptedException e) {
                // Restore the interrupt flag before rethrowing so that callers
                // inspecting Thread.currentThread().isInterrupted() are not misled.
                Thread.currentThread().interrupt();
                throw new WebDriverException(e);
            }
        }
        finally {
            asyncResult_ = null;
        }
    }

    /**
     * Builds and compiles the wrapper function that will execute the user's script
     * asynchronously. The wrapper:
     * <ul>
     *   <li>Appends a callback function as the final argument to the user script.</li>
     *   <li>Installs an {@code unload} listener to detect page navigation.</li>
     *   <li>Schedules a {@code setTimeout} to signal a timeout if the script
     *       does not call the callback in time.</li>
     * </ul>
     *
     * <p>The {@code host} property on the returned function is set to a
     * {@link NativeJavaObject} wrapping {@code asyncResult}, allowing Rhino to
     * invoke {@link AsyncScriptResult#callback(Object)}, {@link AsyncScriptResult#timeout()},
     * and {@link AsyncScriptResult#unload()} directly from JavaScript.</p>
     *
     * @param userScript  the user-supplied script body to wrap
     * @param asyncResult the result holder to wire up as the {@code host} property
     * @return the compiled and configured wrapper {@link Function}
     */
    private Function createInjectedScriptFunction(final String userScript, final AsyncScriptResult asyncResult) {
        // Note: the first `var self, timeoutId` declaration was intentionally removed.
        // The original code declared both variables twice in the same function scope,
        // making the first declaration redundant and potentially confusing. `catchUnload`
        // is now declared in the same `var` statement as `self` and `timeoutId` so that
        // `cleanUp` (defined immediately before) can safely reference it via closure.
        final String script =
                "let huDriverAsyncFoo = "
                + "function() {"

                + "  var cleanUp = function() {"
                + "    window.clearTimeout(timeoutId);"
                + "    if (window.detachEvent) {"
                + "      window.detachEvent('onunload', catchUnload);"
                + "    } else {"
                + "      window.removeEventListener('unload', catchUnload, false);"
                + "    }"
                + "  };"

                + "  var self = this, timeoutId, catchUnload = function() {"
                + "    cleanUp();"
                + "    self.host.unload();"
                + "  };"

                // Convert arguments into an actual array, then append the callback.
                + "  arguments = Array.prototype.slice.call(arguments, 0);"
                + "  arguments.push(function(value) {"
                + "    cleanUp();"
                + "    self.host.callback(typeof value == 'undefined' ? null : value);"
                + "  });"

                // Install a listener to detect page unloads; async scripts must not
                // span page navigations.
                + "  if (window.attachEvent) {"
                + "    window.attachEvent('onunload', catchUnload);"
                + "  } else {"
                + "    window.addEventListener('unload', catchUnload, false);"
                + "  }"

                // Execute the user's script.
                + "  (function() {" + userScript + "}).apply(null, arguments);"

                // Register the timeout after starting the script. If the callback fires
                // synchronously the extra timeout signal is safely ignored by the host.
                + "  timeoutId = window.setTimeout(function() { self.host.timeout(); }, " + timeoutMillis_ + ");"
                + "}; "
                + "huDriverAsyncFoo;";

        // Compile the wrapper script.
        final ScriptResult result = page_.executeJavaScript(script);
        final Function function = (Function) result.getJavaScriptResult();

        final org.htmlunit.javascript.host.Window window = page_.getEnclosingWindow().getScriptableObject();
        final VarScope scope = ScriptableObject.getTopLevelScope(window.getParentScope());

        // Wire the Java host object into the compiled function so Rhino can call back.
        final TypeInfo staticType = TypeInfoFactory
                                    .getOrElse(scope, TypeInfoFactory.GLOBAL)
                                    .create(AsyncScriptResult.class);
        final NativeJavaObject nativeJavaObject = new NativeJavaObject(scope, asyncResult, staticType);
        function.put("host", function, nativeJavaObject);

        return function;
    }

    /**
     * Host object used to capture the result of an asynchronous script.
     *
     * <p>This class is thread-safe and designed to be called concurrently from
     * HtmlUnit's JavaScript thread (via Rhino), from the WebDriver/test thread
     * (via {@link #waitForResult(long)}), and from HtmlUnit's alert-handler thread
     * (via {@link #alert(String)}).</p>
     *
     * <p>Only one signal ({@link #callback(Object)}, {@link #timeout()},
     * {@link #alert(String)}, or {@link #unload()}) is ever honoured; the rest
     * are silently discarded. This is guaranteed by {@link #claimed_}: a caller
     * acquires exclusive ownership via {@link AtomicBoolean#compareAndSet(boolean,
     * boolean)} before writing any state field, ensuring that no partial or mixed
     * state can be observed by {@link #waitForResult(long)}.</p>
     *
     * <p>This class has public visibility so that it can be correctly wrapped in a
     * {@link NativeJavaObject} by Rhino.</p>
     *
     * @see AsyncScriptExecutor
     */
    public static class AsyncScriptResult {

        /**
         * Counts down from 1 to 0 exactly once, signalling {@link #waitForResult}
         * that a terminal event has occurred.
         */
        private final CountDownLatch latch_ = new CountDownLatch(1);

        /**
         * Guards against multiple concurrent signals. The first thread to
         * successfully {@code compareAndSet(false, true)} wins exclusive rights to
         * write the state fields and count down the latch. All subsequent callers
         * find {@code true} already set and return immediately.
         *
         * <p>This replaces the previous {@code latch_.getCount() > 0} idiom, which
         * was not atomic with {@code latch_.countDown()} and could allow two threads
         * to both believe they were the sole winner.</p>
         */
        private final AtomicBoolean claimed_ = new AtomicBoolean(false);

        /** The value passed to {@link #callback(Object)}; valid only when the latch is down and neither timeout nor alert nor unload was signalled. */
        private volatile Object value_;

        /** Set to {@code true} by {@link #timeout()} when the script exceeds the allowed duration. */
        private volatile boolean isTimeout_;

        /** The alert message set by {@link #alert(String)}; non-null only when an alert fired. */
        private volatile String alertMessage_;

        /** Set to {@code true} by {@link #unload()} when a page navigation is detected. */
        private volatile boolean unloadDetected_;

        /**
         * Blocks until the script signals a result, or until the Java-side timeout
         * elapses as a safety net (in case HtmlUnit's {@code window.setTimeout} never
         * fires, e.g. due to a JS engine hang or page navigation that swallows the
         * timer).
         *
         * <p>The {@code timeoutMillis} parameter is used both by the injected
         * JavaScript {@code setTimeout} and as an upper bound for this method's
         * {@link CountDownLatch#await(long, TimeUnit)} call. A small grace period
         * ({@code + 2000ms}) is added to the Java-side timeout so that the JS timer
         * is given a fair chance to fire first under normal conditions, keeping
         * the JS-side timeout message as the primary signal.</p>
         *
         * @param timeoutMillis the script timeout in milliseconds, as configured on
         *                      the driver
         * @return the script result value; may be {@code null}
         * @throws InterruptedException   if the waiting thread is interrupted
         * @throws ScriptTimeoutException if the script does not complete in time
         * @throws UnhandledAlertException if a {@code window.alert()} fired during execution
         * @throws JavascriptException    if a page unload was detected during execution
         */
        Object waitForResult(final long timeoutMillis) throws InterruptedException {
            // Add a grace period so the JS-side setTimeout fires first under normal
            // conditions. The Java timeout is only a safety net for pathological cases
            // (hung JS engine, swallowed timer events, etc.).
            final long javaTimeoutMillis = timeoutMillis + 2_000L;
            final boolean completed = latch_.await(javaTimeoutMillis, TimeUnit.MILLISECONDS);

            if (!completed || isTimeout_) {
                // Either the JS timer signalled a timeout, or the Java safety-net
                // timeout elapsed without any signal at all.
                throw new ScriptTimeoutException(
                        "Timed out waiting for async script result after " + timeoutMillis + "ms");
            }
            if (alertMessage_ != null) {
                throw new UnhandledAlertException("Alert found", alertMessage_);
            }
            if (unloadDetected_) {
                throw new JavascriptException(
                        "Detected a page unload event; executeAsyncScript does not work across page loads");
            }
            return value_;
        }

        /**
         * Invoked by the injected script's callback function to deliver the script's
         * result value. Has no effect if any other terminal event has already been
         * claimed (timeout, alert, or unload).
         *
         * <p>This method has public visibility for Rhino and must not be called by
         * any code outside of the Rhino JavaScript engine.</p>
         *
         * @param callbackValue the value passed to the callback by the script;
         *                      may be {@code null} (representing JavaScript {@code undefined}
         *                      or an explicit {@code null})
         */
        public void callback(final Object callbackValue) {
            if (claimed_.compareAndSet(false, true)) {
                value_ = callbackValue;
                latch_.countDown();
            }
        }

        /**
         * Invoked by the injected {@code window.setTimeout} handler when the script
         * exceeds the configured timeout. Has no effect if the script has already
         * called the callback or if another terminal event was claimed first.
         *
         * <p>This method has public visibility for Rhino and must not be called by
         * any code outside of the Rhino JavaScript engine.</p>
         */
        public void timeout() {
            if (claimed_.compareAndSet(false, true)) {
                isTimeout_ = true;
                latch_.countDown();
            }
        }

        /**
         * Invoked when a {@code window.alert()} fires while the script is running.
         * Has no effect if another terminal event was claimed first.
         *
         * <p>Called from HtmlUnit's alert-handler thread via
         * {@link AsyncScriptExecutor#alertTriggered(String)}.</p>
         *
         * @param message the alert message text; must not be {@code null}
         */
        void alert(final String message) {
            if (claimed_.compareAndSet(false, true)) {
                alertMessage_ = message;
                latch_.countDown();
            }
        }

        /**
         * Invoked by the injected {@code unload} event listener when a page navigation
         * is detected. WebDriver's async script execution model does not permit
         * navigation while a script is running. Has no effect if another terminal
         * event was claimed first.
         *
         * <p>This method has public visibility for Rhino and must not be called by
         * any code outside of the Rhino JavaScript engine.</p>
         */
        public void unload() {
            if (claimed_.compareAndSet(false, true)) {
                unloadDetected_ = true;
                latch_.countDown();
            }
        }
    }
}
