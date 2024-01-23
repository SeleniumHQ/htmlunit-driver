//Licensed to the Software Freedom Conservancy (SFC) under one
//or more contributor license agreements.  See the NOTICE file
//distributed with this work for additional information
//regarding copyright ownership.  The SFC licenses this file
//to you under the Apache License, Version 2.0 (the
//"License"); you may not use this file except in compliance
//with the License.  You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,
//software distributed under the License is distributed on an
//"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//KIND, either express or implied.  See the License for the
//specific language governing permissions and limitations
//under the License.

package org.openqa.selenium.htmlunit.options;

public interface HtmlUnitOptionNames {
    String SECTION = "htmlunit-option";
    String optWebClientVersion = "webClientVersion";
    String optJavaScriptEnabled = "javascriptEnabled";
    String optCssEnabled = "cssEnabled";
    String optPrintContentOnFailingStatusCode = "printContentOnFailingStatusCode";
    String optThrowExceptionOnFailingStatusCode = "throwExceptionOnFailingStatusCode";
    String optThrowExceptionOnScriptError = "throwExceptionOnScriptError";
    String optPopupBlockerEnabled = "popupBlockerEnabled";
    String optIsRedirectEnabled = "isRedirectEnabled";
    String optTempFileDirectory = "tempFileDirectory";
    String optSslClientCertificateStore = "sslClientCertificateStore";
    String optSslClientCertificateType = "sslClientCertificateType";
    String optSslClientCertificatePassword = "sslClientCertificatePassword";
    String optSslTrustStore = "sslTrustStore";
    String optSslTrustStoreType = "sslTrustStoreType";
    String optSslTrustStorePassword = "sslTrustStorePassword";
    String optSslClientProtocols = "sslClientProtocols";
    String optSslClientCipherSuites = "sslClientCipherSuites";
    String optGeolocationEnabled = "geolocationEnabled";
    String optDoNotTrackEnabled = "doNotTrackEnabled";
    String optHomePage = "homePage";
    String optProxyConfig = "proxyConfig";
    String optTimeout = "timeout";
    String optConnectionTimeToLive = "connectionTimeToLive";
    String optUseInsecureSSL = "useInsecureSSL";
    String optSslInsecureProtocol = "sslInsecureProtocol";
    String optMaxInMemory = "maxInMemory";
    String optHistorySizeLimit = "historySizeLimit";
    String optHistoryPageCacheLimit = "historyPageCacheLimit";
    String optLocalAddress = "localAddress";
    String optDownloadImages = "downloadImages";
    String optScreenWidth = "screenWidth";
    String optScreenHeight = "screenHeight";
    String optWebSocketEnabled = "webSocketEnabled";
    String optWebSocketMaxTextMessageSize = "webSocketMaxTextMessageSize";
    String optWebSocketMaxTextMessageBufferSize = "webSocketMaxTextMessageBufferSize";
    String optWebSocketMaxBinaryMessageSize = "webSocketMaxBinaryMessageSize";
    String optWebSocketMaxBinaryMessageBufferSize = "webSocketMaxBinaryMessageBufferSize";
    String optFetchPolyfillEnabled = "fetchPolyfillEnabled";
}
