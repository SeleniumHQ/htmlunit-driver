# HtmlUnitDriver

HtmlUnitDriver is a WebDriver compatible driver for the [HtmlUnit](http://htmlunit.sourceforge.net/) headless browser.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.seleniumhq.selenium/htmlunit-driver/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.seleniumhq.selenium/htmlunit-driver)

##### News
[<img src="http://htmlunit.sourceforge.net/images/logos/twitter.png" alt="Twitter" height="44" width="60">](https://twitter.com/HtmlUnit)

[![Build Status](https://jenkins.wetator.org/buildStatus/icon?job=HtmlUnitDriver+-+Selenium+4)](https://jenkins.wetator.org/view/HtmlUnit%20Driver/job/HtmlUnitDriver%20-%20Selenium%204/)

## Download and Installation

There are two versions available

### Version 3.xx
This version is compatible with Selenium 4 - xx points to the matching HtmlUnit version.

**Maven/Gradle/...**

Simply add a dependency on the latest `htmlunit-driver` version available in the Maven Central.

```xml
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>htmlunit-driver</artifactId>
    <version>3.64.0</version>
</dependency>
```

| selenium | htmlunit-driver |
|----------|-----------------|
|    4.4.0 |          3.64.0 |
|    4.3.0 |          3.63.0 |
|    4.2.1 |          3.62.0 |
|    4.1.3 |          3.61.0 |
|    4.1.0 | 3.56.0 - 3.60.0 |
|    4.0.0 |          3.55.0 |


### Version 2.xx
This version is compatible with Selenium 3.141.59 - again xx points to the matching HtmlUnit version.

**Maven/Gradle/...**

Simply add a dependency on the latest `htmlunit-driver` version available in the Maven Central.

```xml
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>htmlunit-driver</artifactId>
    <version>2.64.0</version>
</dependency>
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


### More customization

HtmlUnit offers a lot more customization options. To adjust these options you can use this pattern.

```java
WebDriver webDriver = new HtmlUnitDriver(BrowserVersion.FIREFOX, true) {
    @Override
    protected WebClient modifyWebClient(WebClient client) {
        final WebClient webClient = super.modifyWebClient(client);
        // you might customize the client here
        webClient.getOptions().setCssEnabled(false);

       return webClient;
    }
};
```

And for some special cases you and also overwrite the method newWebClient(final BrowserVersion version) to
adjust the webClient before the standard WebDriver setup takes place or for constructing your
own webClient.


## License

HtmlUnitDriver is distributed under Apache License 2.0.
