# HtmlUnitDriver

HtmlUnitDriver is a WebDriver compatible driver for the [HtmlUnit](http://htmlunit.sourceforge.net/) headless browser.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.seleniumhq.selenium/htmlunit-driver/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.seleniumhq.selenium/htmlunit-driver)

##### News
[<img src="http://htmlunit.sourceforge.net/images/logos/twitter.png" alt="Twitter" height="44" width="60">](https://twitter.com/HtmlUnit)

[![Build Status](https://jenkins.wetator.org/buildStatus/icon?job=HtmlUnitDriver+-+Selenium+4)](https://jenkins.wetator.org/view/HtmlUnit%20Driver/job/HtmlUnitDriver%20-%20Selenium%204/)

## Download and Installation

There are two versions available

### Selenium compatibility

Starting with Selenium 4.5 we use the same version numbers for the driver - e.g. HtmlUnit-Driver 4.5
is for Selenium 4.5.
For older versions and Selenium 3 please check the following table:

| selenium | htmlunit-driver |    htmlunit     |
|----------|-----------------|-----------------|
|    4.5.0 |           4.5.0 |          2.65.1 |
|          |                 |                 |
|    4.4.0 |          3.64.0 |          2.64.0 |
|    4.3.0 |          3.63.0 |          2.63.0 |
|    4.2.1 |          3.62.0 |          2.62.0 |
|    4.1.3 |          3.61.0 |          2.61.0 |
|    4.1.0 | 3.56.0 - 3.60.0 | 2.56.0 - 2.60.0 |
|    4.0.0 |          3.55.0 |          3.55.0 |
|          |                 |                 |
| 3.141.59 |          2.65.0 |          2.65.1 |
| 3.141.59 |          2.64.0 |          2.64.0 |
| 3.141.59 |          2.63.0 |          2.63.0 |
| 3.141.59 |          2.62.0 |          2.62.0 |
| 3.141.59 |          2.61.0 |          2.61.0 |
| 3.141.59 |          2.60.0 |          2.60.0 |


**Maven/Gradle/...**

Simply add a dependency on the latest `htmlunit-driver` version available in the Maven Central.

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>htmlunit-driver</artifactId>
    <version>4.5.0</version>
</dependency>
```

Add to your `build.gradle`:

```groovy
implementation group: 'org.seleniumhq.selenium', name: 'htmlunit-driver', version: '4.5.0'
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
