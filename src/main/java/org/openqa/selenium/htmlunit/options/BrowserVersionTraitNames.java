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

package org.openqa.selenium.htmlunit.options;

/**
 * @author Scott Babcock
 * @author Ronald Brill
 */
public interface BrowserVersionTraitNames {
    /** "browser-version-trait". */
    String SECTION = "browser-version-trait";

    /** "numericCode". */
    String optNumericCode = "numericCode";

    /** "nickname". */
    String optNickname = "nickname";

    /** "applicationVersion". */
    String optApplicationVersion = "applicationVersion";

    /** "userAgent". */
    String optUserAgent = "userAgent";

    /** "applicationName". */
    String optApplicationName = "applicationName";

    /** "applicationCodeName". */
    String optApplicationCodeName = "applicationCodeName";

    /** "applicationMinorVersion". */
    String optApplicationMinorVersion = "applicationMinorVersion";

    /** "vendor". */
    String optVendor = "vendor";

    /** "browserLanguage". */
    String optBrowserLanguage = "browserLanguage";

    /** "isOnline". */
    String optIsOnline = "isOnline";

    /** "platform". */
    String optPlatform = "platform";

    /** "systemTimezone". */
    String optSystemTimezone = "systemTimezone";

    /** "acceptEncodingHeader". */
    String optAcceptEncodingHeader = "acceptEncodingHeader";

    /** "acceptLanguageHeader". */
    String optAcceptLanguageHeader = "acceptLanguageHeader";

    /** "htmlAcceptHeader". */
    String optHtmlAcceptHeader = "htmlAcceptHeader";

    /** "imgAcceptHeader". */
    String optImgAcceptHeader = "imgAcceptHeader";

    /** "cssAcceptHeader". */
    String optCssAcceptHeader = "cssAcceptHeader";

    /** "scriptAcceptHeader". */
    String optScriptAcceptHeader = "scriptAcceptHeader";

    /** "xmlHttpRequestAcceptHeader". */
    String optXmlHttpRequestAcceptHeader = "xmlHttpRequestAcceptHeader";

    /** "secClientHintUserAgentHeader". */
    String optSecClientHintUserAgentHeader = "secClientHintUserAgentHeader";

    /** "secClientHintUserAgentPlatformHeader". */
    String optSecClientHintUserAgentPlatformHeader = "secClientHintUserAgentPlatformHeader";
}
