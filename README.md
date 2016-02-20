# HtmlUnitDriver

HtmlUnitDriver is a WebDriver compatible driver for HtmlUnit headless browser.

**WARNING: This instruction is written for future, it is not actual until Selenium 2.53 is released.**

HtmlUnitDriver was a part of Selenium main distribution package prior to Selenium version 2.53. If you are using
Selenium 2.52 or earlier you don't need to download and install HtmlUnitDriver, it is already there.

According to Selenium evolution strategy drivers should be separated from Selenium, and a driver release cycle should
be synchronized with the target browser release cycle instead of Selenium release cycle. So this happened to
HtmlUnitDriver too. It's not a part of the main Selenium distribution since version 2.53.

## Download and Installation

### Maven/Gradle/...

Add a dependency on the latest htmlunit-driver version available in the Maven Central along with dependencies on
selenium-api and selenium-support:

```
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>htmlunit-driver</artifactId>
    <version>2.19.0</version>
</dependency>
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-api</artifactId>
    <version>2.53.0</version>
</dependency>
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-support</artifactId>
    <version>2.53.0</version>
</dependency>
```

### Single-Jar Installation for Java

Get the standalone JAR file of the latest release that includes HtmlUnit with all the dependencies and HtmlUnitDriver
and add it to the classpath.

### Multi-Jar Installation for Java

Get a ZIP archive of the latest release that includes HtmlUnit with all the dependencies and HtmlUnitDriver
and add desired JAR files from this archive to the classpath.

### Deploying HtmlUnitDriver to Selenium Server

Download Selenium Server Standalone distribution. Get the standalone JAR file of the latest release that includes
HtmlUnit with all the dependencies and HtmlUnitDriver. Start Selenium Server (standalone or a grid node) like this:

```
java -cp selenium-server-standalone-2.53.0.jar;htmlunit-driver-standalone-2.19.0.jar org.openqa.grid.selenium.GridLauncher <server options>
```

## License

HtmlUnitDriver is distributed under Apache License 2.0.
