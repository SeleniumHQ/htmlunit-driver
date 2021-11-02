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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

public class HtmlUnitDriverTest {

  @Test
  public void customizeWebClient() {
      final StringBuffer detector = new StringBuffer();

      WebDriver webDriver = new HtmlUnitDriver() {
          @Override
          protected WebClient newWebClient(final BrowserVersion version) {
              detector.append("newWebClient");
              final WebClient webClient = super.newWebClient(version);
              return webClient;
          }

        @Override
        protected WebClient modifyWebClient(WebClient client) {
            detector.append("-modifyWebClient");
            return super.modifyWebClient(client);
        }
      };

      assertTrue(webDriver instanceof HtmlUnitDriver);
      assertEquals("newWebClient-modifyWebClient", detector.toString());
  }

  @Test
  public void ctorWebClient() {
    new HtmlUnitDriver(){
      @Override
      protected WebClient modifyWebClient(WebClient client){

        assertEquals(BrowserVersion.getDefault(), client.getBrowserVersion());

        assertFalse("client.getOptions().isJavaScriptEnabled() is true", client.getOptions().isJavaScriptEnabled());
        assertFalse("client.isJavaScriptEnabled() is true", client.isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

        return client;
      }
    };
  }

  @Test
  public void ctorWebClientJsFalse() {
    new HtmlUnitDriver(false){
      @Override
      protected WebClient modifyWebClient(WebClient client){

        assertEquals(BrowserVersion.getDefault(), client.getBrowserVersion());

        assertFalse("client.getOptions().isJavaScriptEnabled() is true", client.getOptions().isJavaScriptEnabled());
        assertFalse("client.isJavaScriptEnabled() is true", client.isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

        return client;
      }
    };
  }

  @Test
  public void ctorWebClientJsTrue() {
    new HtmlUnitDriver(true){
      @Override
      protected WebClient modifyWebClient(WebClient client){

        assertEquals(BrowserVersion.getDefault(), client.getBrowserVersion());

        assertTrue("client.getOptions().isJavaScriptEnabled() is false", client.getOptions().isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEnabled() is false", client.isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

        return client;
      }
    };
  }

  @Test
  public void ctorWebClientBrowserVersionChrome() {
    new HtmlUnitDriver(BrowserVersion.CHROME){
      @Override
      protected WebClient modifyWebClient(WebClient client){

        assertEquals(BrowserVersion.CHROME, client.getBrowserVersion());

        assertFalse("client.getOptions().isJavaScriptEnabled() is true", client.getOptions().isJavaScriptEnabled());
        assertFalse("client.isJavaScriptEnabled() is true", client.isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

        return client;
      }
    };
  }

  @Test
  public void ctorWebClientBrowserVersionEdge() {
    new HtmlUnitDriver(BrowserVersion.EDGE){
      @Override
      protected WebClient modifyWebClient(WebClient client){

        assertEquals(BrowserVersion.EDGE, client.getBrowserVersion());

        assertFalse("client.getOptions().isJavaScriptEnabled() is true", client.getOptions().isJavaScriptEnabled());
        assertFalse("client.isJavaScriptEnabled() is true", client.isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

        return client;
      }
    };
  }

  @Test
  public void ctorWebClientBrowserVersionFirefox() {
    new HtmlUnitDriver(BrowserVersion.FIREFOX){
      @Override
      protected WebClient modifyWebClient(WebClient client){

        assertEquals(BrowserVersion.FIREFOX, client.getBrowserVersion());

        assertFalse("client.getOptions().isJavaScriptEnabled() is true", client.getOptions().isJavaScriptEnabled());
        assertFalse("client.isJavaScriptEnabled() is true", client.isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

        return client;
      }
    };
  }

  @Test
  public void ctorWebClientBrowserVersionFirefox78() {
    new HtmlUnitDriver(BrowserVersion.FIREFOX_78) {
      @Override
      protected WebClient modifyWebClient(WebClient client){

        assertEquals(BrowserVersion.FIREFOX_78, client.getBrowserVersion());

        assertFalse("client.getOptions().isJavaScriptEnabled() is true", client.getOptions().isJavaScriptEnabled());
        assertFalse("client.isJavaScriptEnabled() is true", client.isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

        return client;
      }
    };
  }

  @Test
  public void ctorWebClientBrowserVersionIE() {
    new HtmlUnitDriver(BrowserVersion.INTERNET_EXPLORER){
      @Override
      protected WebClient modifyWebClient(WebClient client){

        assertEquals(BrowserVersion.INTERNET_EXPLORER, client.getBrowserVersion());

        assertFalse("client.getOptions().isJavaScriptEnabled() is true", client.getOptions().isJavaScriptEnabled());
        assertFalse("client.isJavaScriptEnabled() is true", client.isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

        return client;
      }
    };
  }

  @Test
  public void ctorWebClientBrowserVersionFirefoxJsFalse() {
    new HtmlUnitDriver(BrowserVersion.FIREFOX, false){
      @Override
      protected WebClient modifyWebClient(WebClient client){

        assertEquals(BrowserVersion.FIREFOX, client.getBrowserVersion());

        assertFalse("client.getOptions().isJavaScriptEnabled() is true", client.getOptions().isJavaScriptEnabled());
        assertFalse("client.isJavaScriptEnabled() is true", client.isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

        return client;
      }
    };
  }

  @Test
  public void ctorWebClientBrowserVersionFirefox78JsTrue() {
    new HtmlUnitDriver(BrowserVersion.FIREFOX_78, true){
      @Override
      protected WebClient modifyWebClient(WebClient client){

        assertEquals(BrowserVersion.FIREFOX_78, client.getBrowserVersion());

        assertTrue("client.getOptions().isJavaScriptEnabled() is false", client.getOptions().isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEnabled() is false", client.isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

        return client;
      }
    };
  }

  @Test
  public void ctorWebClientCapabilitiesJsTrue() {
    DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
    capabilities.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT, true);

    new HtmlUnitDriver(capabilities){
      @Override
      protected WebClient modifyWebClient(WebClient client){
        assertEquals(BrowserVersion.getDefault(), client.getBrowserVersion());

        assertTrue("client.getOptions().isJavaScriptEnabled() is false", client.getOptions().isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEnabled() is false", client.isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

        return client;
      }
    };
  }

  @Test
  public void ctorWebClientCapabilitiesJsFalse() {
    DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
    capabilities.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT, false);

    new HtmlUnitDriver(capabilities){
      @Override
      protected WebClient modifyWebClient(WebClient client){
        assertEquals(BrowserVersion.getDefault(), client.getBrowserVersion());

        assertFalse("client.getOptions().isJavaScriptEnabled() is true", client.getOptions().isJavaScriptEnabled());
        assertFalse("client.isJavaScriptEnabled() is true", client.isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

        return client;
      }
    };
  }

  @Test
  public void ctorWebClientCapabilitiesVersionString() {
    DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
    capabilities.setVersion("firefox");

    new HtmlUnitDriver(capabilities){
      @Override
      protected WebClient modifyWebClient(WebClient client){
        assertEquals(BrowserVersion.FIREFOX, client.getBrowserVersion());

        return client;
      }
    };

    capabilities = DesiredCapabilities.htmlUnit();
    capabilities.setVersion("googlechrome");

    new HtmlUnitDriver(capabilities){
      @Override
      protected WebClient modifyWebClient(WebClient client){
        assertEquals(BrowserVersion.CHROME, client.getBrowserVersion());

        return client;
      }
    };

    capabilities = DesiredCapabilities.htmlUnit();
    capabilities.setVersion("firefox-78");

    new HtmlUnitDriver(capabilities){
      @Override
      protected WebClient modifyWebClient(WebClient client){
        assertEquals(BrowserVersion.FIREFOX_78, client.getBrowserVersion());

        return client;
      }
    };
  }

  @Test
  public void ctorWebClientCapabilitiesVersion() {
    DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
    capabilities.setVersion(BrowserType.FIREFOX);

    new HtmlUnitDriver(capabilities){
      @Override
      protected WebClient modifyWebClient(WebClient client){
        assertEquals(BrowserVersion.FIREFOX, client.getBrowserVersion());

        return client;
      }
    };

    capabilities = DesiredCapabilities.htmlUnit();
    capabilities.setVersion(BrowserType.CHROME);

    new HtmlUnitDriver(capabilities){
      @Override
      protected WebClient modifyWebClient(WebClient client){
        assertEquals(BrowserVersion.CHROME, client.getBrowserVersion());

        return client;
      }
    };

    capabilities = DesiredCapabilities.htmlUnit();
    capabilities.setVersion(BrowserType.FIREFOX + "-78");

    new HtmlUnitDriver(capabilities){
      @Override
      protected WebClient modifyWebClient(WebClient client){
        assertEquals(BrowserVersion.FIREFOX_78, client.getBrowserVersion());

        return client;
      }
    };
  }

  @Test
  public void ctorWebClientCapabilitiesJsEnabledTrue() {
    DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
    capabilities.setJavascriptEnabled(true);

    new HtmlUnitDriver(capabilities){
      @Override
      protected WebClient modifyWebClient(WebClient client){
        assertEquals(BrowserVersion.getDefault(), client.getBrowserVersion());

        assertTrue("client.getOptions().isJavaScriptEnabled() is false", client.getOptions().isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEnabled() is false", client.isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

        return client;
      }
    };
  }

  @Test
  public void ctorWebClientCapabilitiesJsEnabledFalse() {
    DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
    capabilities.setJavascriptEnabled(false);

    new HtmlUnitDriver(capabilities){
      @Override
      protected WebClient modifyWebClient(WebClient client){
        assertEquals(BrowserVersion.getDefault(), client.getBrowserVersion());

        assertFalse("client.getOptions().isJavaScriptEnabled() is true", client.getOptions().isJavaScriptEnabled());
        assertFalse("client.isJavaScriptEnabled() is true", client.isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

        return client;
      }
    };
  }

  @Test
  public void ctorWebClientCapabilitiesProxy() {
    Proxy proxy = new Proxy();
    proxy.setHttpProxy("hostname:1234");

    DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
    capabilities.setCapability(CapabilityType.PROXY, proxy);

    new HtmlUnitDriver(capabilities){
      @Override
      protected WebClient modifyWebClient(WebClient client){
        assertEquals(BrowserVersion.getDefault(), client.getBrowserVersion());

        assertTrue("client.getOptions().isJavaScriptEnabled() is false", client.getOptions().isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEnabled() is false", client.isJavaScriptEnabled());
        assertTrue("client.isJavaScriptEngineEnabled() is false", client.isJavaScriptEngineEnabled());

        assertEquals("hostname", client.getOptions().getProxyConfig().getProxyHost());
        assertEquals(1234, client.getOptions().getProxyConfig().getProxyPort());

        return client;
      }
    };
  }
}
