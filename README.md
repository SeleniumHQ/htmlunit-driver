# ![HtmlUnitDriver Logo](https://github.com/SeleniumHQ/htmlunit-driver/blob/master/htmlunit_webdriver.png)

Version 4.40.0 / January 20, 2026

**HtmlUnitDriver** is a WebDriver compatible driver for the [HtmlUnit](https://www.htmlunit.org) headless browser.

[![Maven Central Version](https://img.shields.io/maven-central/v/org.seleniumhq.selenium/htmlunit3-driver)](https://central.sonatype.com/artifact/org.seleniumhq.selenium/htmlunit3-driver)

:heart: [Sponsor](https://github.com/sponsors/rbri)

## News

**[Developer Blog](https://htmlunit.github.io/htmlunit-blog/)**

[HtmlUnit@mastodon](https://fosstodon.org/@HtmlUnit) | [HtmlUnit@bsky](https://bsky.app/profile/htmlunit.bsky.social) | [HtmlUnit@Twitter](https://twitter.com/HtmlUnit)


[![Build Status](https://jenkins.wetator.org/buildStatus/icon?job=HtmlUnitDriver+-+Selenium+4)](https://jenkins.wetator.org/view/HtmlUnit%20Driver/job/HtmlUnitDriver%20-%20Selenium%204/)

## HtmlUnit Remote - Selenium 4 Grid support

Please have a look at the **[HtmlUnit Remote](https://github.com/sbabcoc/htmlunit-remote)** project if you like to use
this driver from [Selenium 4 Grid](https://www.selenium.dev/documentation/grid).


## Get it!

An overview of the different versions, the HtmlUnit version used in each case and the compatibility 
can be found in these [tables](docs/compatibility.md).

### Maven

Simply add a dependency on the latest `htmlunit3-driver` version available in the
[Maven Central](https://repo.maven.apache.org/maven2/org/seleniumhq/selenium/htmlunit3-driver/) repository.

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>htmlunit3-driver</artifactId>
    <version>4.40.0</version>
</dependency>
```

### Gradle

Add to your `build.gradle`:

```groovy
implementation group: 'org.seleniumhq.selenium', name: 'htmlunit3-driver', version: '4.40.0'
```


## Usage

### Simple

You can simply use one of the constructors from the **HtmlUnit** driver class

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

**HtmlUnit** offers many customization options. Similar to the other WebDriver implementations, the **HtmlUnitDriverOptions**
class can be used to customize your **HtmlUnit** driver.

```java
final HtmlUnitDriverOptions driverOptions = new HtmlUnitDriverOptions(BrowserVersion.FIREFOX);

// configure e.g.
driverOptions.setCapability(HtmlUnitOption.optThrowExceptionOnScriptError, false);

HtmlUnitDriver webDriver = new HtmlUnitDriver(driverOptions);
// use the driver
```

**NOTE**: Complete details for the **HtmlUnitDriverOptions** class can be found [here](docs/options.md).

### Selenium compatibility

An overview of the different versions, the **HtmlUnit** version used in each case and the compatibility 
can be found in these [tables](docs/compatibility.md).

## License

**HtmlUnitDriver** is distributed under Apache License 2.0.

## Development Tools

Special thanks to:

<a href="https://www.jetbrains.com/community/opensource/"><img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg" alt="JetBrains" width="42"></a>
<a href="https://www.jetbrains.com/idea/"><img src="https://resources.jetbrains.com/storage/products/company/brand/logos/IntelliJ_IDEA_icon.svg" alt="IntelliJ IDEA" width="42"></a>  
**[JetBrains](https://www.jetbrains.com/)** for providing IntelliJ IDEA under their [open source development license](https://www.jetbrains.com/community/opensource/) and

<a href="https://www.eclipse.org/"><img src="https://www.eclipse.org/eclipse.org-common/themes/solstice/public/images/logo/eclipse-foundation-grey-orange.svg" alt="Eclipse Foundation" width="80"></a>  
Eclipse Foundation for their Eclipse IDE

<a href="https://www.syntevo.com/smartgit/"><img src="https://www.syntevo.com/assets/images/logos/smartgit-8c1aa1e2.svg" alt="SmartGit" width="54"></a>  
to **[Syntevo](https://www.syntevo.com/)** for their excellent [SmartGit](https://www.smartgit.dev/)!
