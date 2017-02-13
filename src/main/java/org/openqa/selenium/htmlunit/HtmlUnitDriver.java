package org.openqa.selenium.htmlunit;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverCommandExecutor;

public class HtmlUnitDriver extends RemoteWebDriver {

  public HtmlUnitDriver() {
    this(new DesiredCapabilities());
  }

  public HtmlUnitDriver(Capabilities capabilities) {
    super(new DriverCommandExecutor(HtmlUnitDriverService.createDefaultService()),
        capabilities);
  }
}