# ![HtmlUnitDriver Logo](https://github.com/SeleniumHQ/htmlunit-driver/blob/master/htmlunit_webdriver.png)

HtmlUnitDriver is a WebDriver compatible driver for the [HtmlUnit](https://www.htmlunit.org) headless browser.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.seleniumhq.selenium/htmlunit3-driver/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.seleniumhq.selenium/htmlunit3-driver)

## News

**[Developer Blog][15]**

[HtmlUnit@mastodon](https://fosstodon.org/@HtmlUnit) | [HtmlUnit@LinkedIn](https://www.linkedin.com/in/ronald-brill-a93271197/) | [HtmlUnit@Twitter](https://twitter.com/HtmlUnit)


[![Build Status](https://jenkins.wetator.org/buildStatus/icon?job=HtmlUnitDriver+-+Selenium+4)](https://jenkins.wetator.org/view/HtmlUnit%20Driver/job/HtmlUnitDriver%20-%20Selenium%204/)

## HtmlUnit Remote - Selenium 4 Grid support

Please have a look at the **[HtmlUnit Remote](https://github.com/sbabcoc/htmlunit-remote)** project if you like to use this driver from [Selenium 4 Grid](https://www.selenium.dev/documentation/grid).


## Get it!

An overview of the different versions, the HtmlUnit version used in each case and the compatibility 
can be found in these [tables](compatibility.md).

### Maven

Simply add a dependency on the latest `htmlunit3-driver` version available in the Maven Central.

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>htmlunit3-driver</artifactId>
    <version>4.27.0</version>
</dependency>
```

### Gradle

Add to your `build.gradle`:

```groovy
implementation group: 'org.seleniumhq.selenium', name: 'htmlunit3-driver', version: '4.27.0'
```


## Usage

### Simple

You can simply use one of the constructors from the HtmlUnit driver class

```java
// simple case - no javascript support
WebDriver webDriver = new HtmlUnitDriver();
```

```java
// specify the browser - no javascript support
WebDriver webDriver = new HtmlUnitDriver(BrowserVersion.FIREFOX);
```

```java
// simple case - javascript support enabled
WebDriver webDriver = new HtmlUnitDriver(true);
```

```java
// specify the browser - javascript support enabled
WebDriver webDriver = new HtmlUnitDriver(BrowserVersion.FIREFOX, true);
```


### Customization

HtmlUnit offers many customization options.
Similar to the other WebDriver's the class HtmlUnitDriverOptions can be used to customize your HtmlUnit driver.

```java
final HtmlUnitDriverOptions driverOptions = new HtmlUnitDriverOptions(BrowserVersion.FIREFOX);

// configure e.g.
driverOptions.setCapability(HtmlUnitOption.optThrowExceptionOnScriptError, false);

HtmlUnitDriver webDriver = new HtmlUnitDriver(driverOptions);
// use the driver
```

Please check the 

### Selenium compatibility

An overview of the different versions, the HtmlUnit version used in each case and the compatibility 
can be found in these [tables](compatibility.md).

## License

HtmlUnitDriver is distributed under Apache License 2.0.
