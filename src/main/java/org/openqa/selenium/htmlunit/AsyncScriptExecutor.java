// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
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

import org.htmlunit.ScriptException;
import org.htmlunit.ScriptResult;
import org.htmlunit.corejs.javascript.Function;
import org.htmlunit.corejs.javascript.NativeJavaObject;
import org.htmlunit.html.HtmlPage;
import org.openqa.selenium.ScriptTimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriverException;

/**
 * Injects an asynchronous script into the current page for execution. The
 * script should signal that it is finished by invoking the callback function,
 * which will always be the last argument passed to the injected script.
 *
 * @author Alexei Barantsev
 * @author Ahmed Ashour
 * @author Simon Stewart
 * @author Ronald Brill
 */
class AsyncScriptExecutor {

    private final HtmlPage page_;
    private final long timeoutMillis_;
    private AsyncScriptResult asyncResult_;

    /**
     * Prepares a new asynchronous script for execution.
     *
     * @param page          The page to inject the script into.
     * @param timeoutMillis How long to wait for the script to complete, in
     *                      milliseconds.
     */
    AsyncScriptExecutor(final HtmlPage page, final long timeoutMillis) {
        page_ = page;
        timeoutMillis_ = timeoutMillis;
    }

    void alertTriggered(final String message) {
        asyncResult_.alert(message);
    }

    /**
     * Injects an asynchronous script for execution and waits for its result.
     *
     * @param scriptBody The script body.
     * @param parameters The script parameters, which can be referenced using the
     *                   {@code arguments} JavaScript object.
     * @return The script result.
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
                throw new WebDriverException(e);
            }
        }
        finally {
            asyncResult_ = null;
        }
    }

    private Function createInjectedScriptFunction(final String userScript, final AsyncScriptResult asyncResult) {
        final String script =
                "function() {"
                + "  var self = this, timeoutId;"
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
                // Convert arguments into an actual array, then add the callback object.
                + "  arguments = Array.prototype.slice.call(arguments, 0);"
                + "  arguments.push(function(value) {"
                + "    cleanUp();"
                + "    self.host.callback(typeof value == 'undefined' ? null : value);"
                + "  });"
                // Add an event listener to trap unload events; page loads are not supported
                // with async
                // script execution.
                + "  if (window.attachEvent) {"
                + "    window.attachEvent('onunload', catchUnload);"
                + "  } else {"
                + "    window.addEventListener('unload', catchUnload, false);"
                + "  }"
                // Execute the user's script
                + "  (function() {"
                + userScript
                + "}).apply(null, arguments);"
                // Register our timeout for the script. If the script invokes the callback
                // immediately
                // (e.g. it's not really async), then this will still fire. That's OK because
                // the host
                // object should ignore the extra timeout.
                + "  timeoutId = window.setTimeout(function() {"
                + "    self.host.timeout();"
                + "  }, "
                + timeoutMillis_
                + ");"
                + "}";

        // Compile our script.
        final ScriptResult result = page_.executeJavaScript(script);
        final Function function = (Function) result.getJavaScriptResult();

        // Finally, update the script with the callback host object.
        function.put("host", function, new NativeJavaObject(function, asyncResult, null));

        return function;
    }

    /**
     * Host object used to capture the result of an asynchronous script.
     *
     * <p/>
     * This class has public visibility so it can be correctly wrapped in a
     * {@link NativeJavaObject}.
     *
     * @see AsyncScriptExecutor
     */
    public static class AsyncScriptResult {

        private final CountDownLatch latch_ = new CountDownLatch(1);

        private volatile Object value_;
        private volatile boolean isTimeout_;
        private volatile String alertMessage_;
        private volatile boolean unloadDetected_;

        /**
         * Waits for the script to signal it is done by calling {@link #callback(Object)
         * callback}.
         *
         * @return The script result.
         * @throws InterruptedException If this thread is interrupted before a result is
         *                              ready.
         */
        Object waitForResult(final long timeoutMillis) throws InterruptedException {
            final long startTimeNanos = System.nanoTime();
            latch_.await();
            if (isTimeout_) {
                final long elapsedTimeNanos = System.nanoTime() - startTimeNanos;
                final long elapsedTimeMillis = TimeUnit.NANOSECONDS.toMillis(elapsedTimeNanos);
                throw new ScriptTimeoutException(
                        "Timed out waiting for async script result after " + elapsedTimeMillis + "ms");
            }
            if (alertMessage_ != null) {
                throw new UnhandledAlertException("Alert found", alertMessage_);
            }

            if (unloadDetected_) {
                throw new WebDriverException(
                        "Detected a page unload event; executeAsyncScript does not work across page loads");
            }
            return value_;
        }

        /**
         * Callback function to be exposed in JavaScript.
         *
         * <p/>
         * This method has public visibility for Rhino and should never be called by
         * code outside of Rhino.
         *
         * @param callbackValue The asynchronous script result.
         */
        public void callback(final Object callbackValue) {
            if (latch_.getCount() > 0) {
                this.value_ = callbackValue;
                latch_.countDown();
            }
        }

        /**
         * Function exposed in JavaScript to signal a timeout. Has no effect if called
         * after the {@link #callback(Object) callback} function.
         *
         * <p/>
         * This method has public visibility for Rhino and should never be called by
         * code outside of Rhino.
         */
        public void timeout() {
            if (latch_.getCount() > 0) {
                isTimeout_ = true;
                latch_.countDown();
            }
        }

        /**
         * Function to signal an alert.
         */
        private void alert(final String message) {
            if (latch_.getCount() > 0) {
                this.alertMessage_ = message;
                latch_.countDown();
            }
        }

        /**
         * Function exposed to JavaScript to signal that a page unload event was fired.
         * WebDriver's asynchronous script execution model does not permit new page
         * loads.
         *
         * <p/>
         * This method has public visibility for Rhino and should never be called by
         * code outside of Rhino.
         */
        public void unload() {
            if (latch_.getCount() > 0) {
                unloadDetected_ = true;
                latch_.countDown();
            }
        }
    }
}
