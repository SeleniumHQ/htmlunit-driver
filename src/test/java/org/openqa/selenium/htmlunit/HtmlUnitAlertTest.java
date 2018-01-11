package org.openqa.selenium.htmlunit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
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

  @Test
  public void confirmWithRedirect() throws Exception {
    String message = "Are you sure?";

    String html = "<html>\n"
            + "<a id='confirm' href='http://htmlunit.sourceforge.net/' onclick='return confirm(\"" + message + "\");'>Confirm</a>\n"
            + "<div id='message'>Default</div>"
            + "</html>\n"
            ;

    WebDriver driver = loadPage2(html);
    driver.findElement(By.id("confirm")).click();

    assertEquals(message, driver.switchTo().alert().getText());
    driver.switchTo().alert().accept();

    Thread.sleep(1000);

    assertTrue(driver.getTitle(), driver.getTitle().contains("Welcome to HtmlUnit"));
  }

  @Test
  public void confirmWithoutRedirect() throws Exception {
    String message = "Are you sure?";

    String html = "<html>\n"
            + "<head>\n"
            + "<title>ConfirmWithoutRedirect</title>\n"
            + "<script>\n"
            + "function runConfirm() {\n"
            + "if (!confirm('" + message + "')) {"
            + "document.getElementById('message').innerHTML = 'False';\n"
            + "return false;\n"
            + "}\n"
            + "}\n"
            + "</script>\n"
            + "</head>\n"
            + "<a id='confirm' href='http://htmlunit.sourceforge.net/' onclick='return runConfirm();'>Confirm</a>\n"
            + "<div id='message'>Default</div>"
            + "</html>\n"
            ;

    WebDriver driver = loadPage2(html);
    driver.findElement(By.id("confirm")).click();

    assertEquals(message, driver.switchTo().alert().getText());
    driver.switchTo().alert().dismiss();

    Thread.sleep(1000);

    assertEquals("False", driver.findElement(By.id("message")).getText());
    assertEquals("ConfirmWithoutRedirect", driver.getTitle());
  }

}
