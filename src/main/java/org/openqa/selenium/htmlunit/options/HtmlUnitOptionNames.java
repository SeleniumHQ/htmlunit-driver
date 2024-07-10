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
public interface HtmlUnitOptionNames {
    /** "htmlunit-option". */
    String SECTION = "htmlunit-option";

    /** "webClientVersion". */
    String optWebClientVersion = "webClientVersion";

    /** "javascriptEnabled". */
    String optJavaScriptEnabled = "javascriptEnabled";

    /** "cssEnabled". */
    String optCssEnabled = "cssEnabled";

    /** "printContentOnFailingStatusCode". */
    String optPrintContentOnFailingStatusCode = "printContentOnFailingStatusCode";

    /** "throwExceptionOnFailingStatusCode". */
    String optThrowExceptionOnFailingStatusCode = "throwExceptionOnFailingStatusCode";

    /** "throwExceptionOnScriptError". */
    String optThrowExceptionOnScriptError = "throwExceptionOnScriptError";

    /** "popupBlockerEnabled". */
    String optPopupBlockerEnabled = "popupBlockerEnabled";

    /** "isRedirectEnabled". */
    String optIsRedirectEnabled = "isRedirectEnabled";

    /** "tempFileDirectory". */
    String optTempFileDirectory = "tempFileDirectory";

    /** "sslClientCertificateStore". */
    String optSslClientCertificateStore = "sslClientCertificateStore";

    /** "sslClientCertificateType". */
    String optSslClientCertificateType = "sslClientCertificateType";

    /** "sslClientCertificatePassword". */
    String optSslClientCertificatePassword = "sslClientCertificatePassword";

    /** "sslTrustStore". */
    String optSslTrustStore = "sslTrustStore";

    /** "sslTrustStoreType". */
    String optSslTrustStoreType = "sslTrustStoreType";

    /** "sslTrustStorePassword". */
    String optSslTrustStorePassword = "sslTrustStorePassword";

    /** "sslClientProtocols". */
    String optSslClientProtocols = "sslClientProtocols";

    /** "sslClientCipherSuites". */
    String optSslClientCipherSuites = "sslClientCipherSuites";

    /** "geolocationEnabled". */
    String optGeolocationEnabled = "geolocationEnabled";

    /** "doNotTrackEnabled". */
    String optDoNotTrackEnabled = "doNotTrackEnabled";

    /** "homePage". */
    String optHomePage = "homePage";

    /** "proxyConfig". */
    String optProxyConfig = "proxyConfig";

    /** "timeout". */
    String optTimeout = "timeout";

    /** "connectionTimeToLive". */
    String optConnectionTimeToLive = "connectionTimeToLive";

    /** "useInsecureSSL". */
    String optUseInsecureSSL = "useInsecureSSL";

    /** "sslInsecureProtocol". */
    String optSslInsecureProtocol = "sslInsecureProtocol";

    /** "maxInMemory". */
    String optMaxInMemory = "maxInMemory";

    /** "historySizeLimit". */
    String optHistorySizeLimit = "historySizeLimit";

    /** "historyPageCacheLimit". */
    String optHistoryPageCacheLimit = "historyPageCacheLimit";

    /** "localAddress". */
    String optLocalAddress = "localAddress";

    /** "downloadImages". */
    String optDownloadImages = "downloadImages";

    /** "screenWidth". */
    String optScreenWidth = "screenWidth";

    /** "screenHeight". */
    String optScreenHeight = "screenHeight";

    /** "webSocketEnabled". */
    String optWebSocketEnabled = "webSocketEnabled";

    /** "webSocketMaxTextMessageSize". */
    String optWebSocketMaxTextMessageSize = "webSocketMaxTextMessageSize";

    /** "webSocketMaxTextMessageBufferSize". */
    String optWebSocketMaxTextMessageBufferSize = "webSocketMaxTextMessageBufferSize";

    /** "webSocketMaxBinaryMessageSize". */
    String optWebSocketMaxBinaryMessageSize = "webSocketMaxBinaryMessageSize";

    /** "webSocketMaxBinaryMessageBufferSize". */
    String optWebSocketMaxBinaryMessageBufferSize = "webSocketMaxBinaryMessageBufferSize";

    /** "fetchPolyfillEnabled". */
    String optFetchPolyfillEnabled = "fetchPolyfillEnabled";

    /** "fileProtocolForXMLHttpRequestsAllowed". */
    String optFileProtocolForXMLHttpRequestsAllowed = "fileProtocolForXMLHttpRequestsAllowed";
}
