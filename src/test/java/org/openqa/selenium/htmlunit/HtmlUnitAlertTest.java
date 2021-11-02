package org.openqa.selenium.htmlunit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

import static org.openqa.selenium.WaitingConditions.pageSourceToContain;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBe;

@RunWith(BrowserRunner.class)
public class HtmlUnitAlertTest extends WebDriverTestCase {

  protected Wait<WebDriver> wait;

  @Before
  public void setUpWait() {
    this.wait = new WebDriverWait(getWebDriver(), Duration.ofSeconds(30));
  }

  @Test
  public void confirm() throws Exception {
    String message = "Are you sure?";

    String html = "<html>\n"
        + "<head>\n"
        + "<script>\n"
        + "    confirm('" + message + "');\n"
        + "</script>\n"
        + "</head>\n"
        + "<body>\n"
        + "</body>\n"
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

    wait.until(pageSourceToContain("Welcome to HtmlUnit"));

    assertTrue("Title was '" + driver.getTitle() + "'",
            driver.getTitle().contains("Welcome to HtmlUnit"));
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
            + "<body>\n"
            + "<a id='confirm' href='http://htmlunit.sourceforge.net/' onclick='return runConfirm();'>Confirm</a>\n"
            + "<div id='message'>Default</div>"
            + "</body>\n"
            + "</html>\n"
            ;

    WebDriver driver = loadPage2(html);
    driver.findElement(By.id("confirm")).click();

    assertEquals(message, driver.switchTo().alert().getText());
    driver.switchTo().alert().dismiss();

    wait.until(textToBe(By.id("message"), "False"));

    assertEquals("False", driver.findElement(By.id("message")).getText());
    assertEquals("ConfirmWithoutRedirect", driver.getTitle());
  }


  @Test
  public void alertWithLineBreak() throws Exception {
    String html = "<html>\n"
            + "<head>\n"
            + "</head>\n"
            + "<body>\n"
            + "  <button id='clickMe' onClick='alert(\"1\\n2\\r\\n3\\t4\\r5\\n\\r6\")'>do it</button>\n"
            + "</body>\n"
            + "</html>\n"
            ;

    WebDriver driver = loadPage2(html);
    driver.findElement(By.id("clickMe")).click();

    // selenium seems to normalize this
    if (getBrowserVersion().isIE()) {
        assertEquals("1\n2\n3\t4\r5\n\r6", driver.switchTo().alert().getText());
    }
    else {
        assertEquals("1\n2\n3\t4\n5\n\n6", driver.switchTo().alert().getText());
    }
    driver.switchTo().alert().dismiss();
  }
}
