package org.openqa.selenium.htmlunit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

@RunWith(BrowserRunner.class)
public class HtmlUnitAlertTest extends WebDriverTestCase {

  @Test
  public void confirm() throws Exception {
    String message = "Are you sure?";

    String html = "<html>\n"
        + "<head>\n"
        + "<script>\n"
        + "    confirm('" + message + "');\n"
        + "</script>\n"
        + "</head>\n"
        + "</html>\n"
        ;

    WebDriver driver = loadPage2(html);
    assertEquals(message, driver.switchTo().alert().getText());
    driver.switchTo().alert().accept();
  }
}
