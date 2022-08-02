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

package org.openqa.selenium.htmlunit.w3;

import java.util.Map;

/**
 * Errors are represented in the WebDriver protocol by an HTTP response with an
 * HTTP status in the 4xx or 5xx range, and a JSON body containing details of
 * the error. The body is a JSON Object and has a field named "value" whose
 * value is an object bearing three, and sometimes four, fields: - "error",
 * containing a string indicating the error code. - "message", containing an
 * implementation-defined string with a human readable description of the kind
 * of error that occurred. - "stacktrace", containing an implementation-defined
 * string with a stack trace report of the active stack frames at the time when
 * the error occurred. Optionally "data", which is a JSON Object with additional
 * error data helpful in diagnosing the error.
 *
 * @see <a href="https://www.w3.org/TR/webdriver/#errors">Errors</a>
 *
 * @author Ronald Brill
 */
public class Error {
    private final String error_;
    private final String message_;
    private final String stacktrace_;

    private Map<String, String> data_;

    /**
     * Ctor.
     *
     * @param error the error text
     * @param message the message text
     * @param stacktrace the stack trace
     */
    public Error(final String error, final String message, final String stacktrace) {
        error_ = error;
        message_ = message;
        stacktrace_ = stacktrace;
    }
}
