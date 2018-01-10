package org.openqa.selenium.htmlunit;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.junit.Assert.fail;
import static org.openqa.selenium.remote.CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

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

  @Test
  public void handleUnexpectedAlertDismiss() throws Exception {
    String html = "<html>\n"
            + "<head>\n"
            + "<title>UnexpectedAlertDismiss</title>\n"
            + "</head>\n"
            + "<body>\n"
            + "<script>\n"
            + "    alert('hi');\n"
            + "</script>\n"
            + "</body>\n"
            + "</html>\n"
            ;

    DesiredCapabilities capabilities = new DesiredCapabilities();
    capabilities.setCapability(UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.DISMISS);
    WebDriver driver = loadPage2(html, URL_FIRST, "text/html;charset=ISO-8859-1", ISO_8859_1, capabilities);


    assertEquals("hi", driver.switchTo().alert().getText());

    assertEquals("UnexpectedAlertDismiss", driver.getTitle());
    try {
        driver.switchTo().alert();
        fail("NoAlertPresentException expected");
    } catch (final NoAlertPresentException e) {
        // expected
    }
  }

  @Test
  public void handleUnexpectedAlertDismissAndNotify() throws Exception {
    String html = "<html>\n"
            + "<head>\n"
            + "<title>UnexpectedAlertDismiss</title>\n"
            + "</head>\n"
            + "<body>\n"
            + "<script>\n"
            + "    alert('hi');\n"
            + "</script>\n"
            + "</body>\n"
            + "</html>\n"
            ;
    DesiredCapabilities capabilities = new DesiredCapabilities();
    capabilities.setCapability(UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.DISMISS_AND_NOTIFY);
    WebDriver driver = loadPage2(html, URL_FIRST, "text/html;charset=ISO-8859-1", ISO_8859_1, capabilities);

    assertEquals("hi", driver.switchTo().alert().getText());

    try {
        driver.getTitle();
        fail("UnhandledAlertException expected");
    } catch (final UnhandledAlertException e) {
        // expected
        assertTrue(e.getMessage(), e.getMessage().startsWith("Alert found: hi"));
    }

    try {
        driver.switchTo().alert();
        fail("NoAlertPresentException expected");
    } catch (final NoAlertPresentException e) {
        // expected
    }
  }

  @Test
  public void handleUnexpectedAlertAccept() throws Exception {
    String html = "<html>\n"
            + "<head>\n"
            + "<title>UnexpectedAlertAccept</title>\n"
            + "</head>\n"
            + "<body>\n"
            + "<script>\n"
            + "    alert('hi');\n"
            + "</script>\n"
            + "</body>\n"
            + "</html>\n"
            ;
    DesiredCapabilities capabilities = new DesiredCapabilities();
    capabilities.setCapability(UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
    WebDriver driver = loadPage2(html, URL_FIRST, "text/html;charset=ISO-8859-1", ISO_8859_1, capabilities);

    assertEquals("hi", driver.switchTo().alert().getText());

    assertEquals("UnexpectedAlertAccept", driver.getTitle());

    try {
        driver.switchTo().alert();
        fail("NoAlertPresentException expected");
    } catch (final NoAlertPresentException e) {
        // expected
    }
  }

  @Test
  public void handleUnexpectedAlertAcceptAndNotify() throws Exception {
    String html = "<html>\n"
            + "<head>\n"
            + "<title>UnexpectedAlertDismiss</title>\n"
            + "</head>\n"
            + "<body>\n"
            + "<script>\n"
            + "    alert('hi');\n"
            + "</script>\n"
            + "</body>\n"
            + "</html>\n"
            ;
    DesiredCapabilities capabilities = new DesiredCapabilities();
    capabilities.setCapability(UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT_AND_NOTIFY);
    WebDriver driver = loadPage2(html, URL_FIRST, "text/html;charset=ISO-8859-1", ISO_8859_1, capabilities);

    assertEquals("hi", driver.switchTo().alert().getText());

    try {
        driver.getTitle();
        fail("UnhandledAlertException expected");
    } catch (final UnhandledAlertException e) {
        // expected
        assertTrue(e.getMessage(), e.getMessage().startsWith("Alert found: hi"));
    }

    try {
        driver.switchTo().alert();
        fail("NoAlertPresentException expected");
    } catch (final NoAlertPresentException e) {
        // expected
    }
  }

  @Test
  public void handleUnexpectedAlertIgnore() throws Exception {
    String html = "<html>\n"
            + "<head>\n"
            + "<title>UnexpectedAlertIgnore</title>\n"
            + "</head>\n"
            + "<body>\n"
            + "<script>\n"
            + "    alert('hi');\n"
            + "</script>\n"
            + "</body>\n"
            + "</html>\n"
            ;
    DesiredCapabilities capabilities = new DesiredCapabilities();
    capabilities.setCapability(UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
    WebDriver driver = loadPage2(html, URL_FIRST, "text/html;charset=ISO-8859-1", ISO_8859_1, capabilities);

    assertEquals("hi", driver.switchTo().alert().getText());

    try {
        driver.getTitle();
        fail("UnhandledAlertException expected");
    } catch (final UnhandledAlertException e) {
        // expected
        assertTrue(e.getMessage(), e.getMessage().startsWith("Alert found: hi"));
    }

    // still present
    assertEquals("hi", driver.switchTo().alert().getText());
  }
}
