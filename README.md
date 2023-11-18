# HtmlUnitDriver

HtmlUnitDriver is a WebDriver compatible driver for the [HtmlUnit](https://www.htmlunit.org) headless browser.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.seleniumhq.selenium/htmlunit3-driver/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.seleniumhq.selenium/htmlunit3-driver)

##### News
[<img src="https://www.htmlunit.org/images/logos/twitter.png" alt="Twitter" height="44" width="60">](https://twitter.com/HtmlUnit)

[![Build Status](https://jenkins.wetator.org/buildStatus/icon?job=HtmlUnitDriver+-+Selenium+4)](https://jenkins.wetator.org/view/HtmlUnit%20Driver/job/HtmlUnitDriver%20-%20Selenium%204/)

## Download and Installation

There are two versions available

### Selenium compatibility

Starting with Selenium 4.5 we use the same version numbers for the driver - e.g. HtmlUnit-Driver 4.15.0
is for Selenium 4.15.0.

Because there are so many tools working on top HtmlUnitDriver, we now maintain two version (artifact id's).

**htmlunit3-driver** uses the (latest) version of HtmlUnit 3.x and is therefore not backward compatible.

**htmlunit-driver** is backward compatible but it still is based on HtmlUnit 2.70.

For an overview please check the following tables:

***htmlunit3-driver***

| selenium | htmlunit-driver |    htmlunit     |    artifactId    |  JDK |
|----------|-----------------|-----------------|------------------|------|
|   4.15.0 |      **4.15.0** |          3.8.0  | htmlunit3-driver |   11 |
|   4.14.1 |          4.14.1 |          3.7.0  | htmlunit3-driver |   11 |
|   4.13.0 |          4.13.0 |          3.6.0  | htmlunit3-driver |  1.8 |
|   4.12.0 |          4.12.0 |          3.5.0  | htmlunit3-driver |  1.8 |
|   4.11.0 |          4.11.0 |          3.4.0  | htmlunit3-driver |  1.8 |
|   4.10.0 |          4.10.0 |          3.3.0  | htmlunit3-driver |  1.8 |
|    4.9.1 |           4.9.1 |          3.2.0  | htmlunit3-driver |  1.8 |
|    4.9.0 |           4.9.0 |          3.1.0  | htmlunit3-driver |  1.8 |
|    4.8.3 |           4.8.3 |          3.1.0  | htmlunit3-driver |  1.8 |
|    4.8.1 |           4.8.1 |          3.0.0  | htmlunit3-driver |  1.8 |


***htmlunit-driver***

| selenium | htmlunit-driver |    htmlunit     |    artifactId    |
|----------|-----------------|-----------------|------------------|
|   4.13.0 |          4.13.0 |          2.70.0 |  htmlunit-driver |
|   4.12.0 |          4.12.0 |          2.70.0 |  htmlunit-driver |
|   4.11.0 |          4.11.0 |          2.70.0 |  htmlunit-driver |
|   4.10.0 |          4.10.0 |          2.70.0 |  htmlunit-driver |
|    4.9.1 |           4.9.1 |          2.70.0 |  htmlunit-driver |
|    4.9.0 |           4.9.0 |          2.70.0 |  htmlunit-driver |
|          |                 |                 |                  |
|    4.8.3 |           4.8.3 |          2.70.0 |  htmlunit-driver |
|    4.8.1 |         4.8.1.1 |          2.70.0 |  htmlunit-driver |
|    4.8.1 |           4.8.1 |          3.0.0  |  htmlunit-driver |
|    4.8.0 |           4.8.0 |          2.70.0 |  htmlunit-driver |
|          |                 |                 |                  |
|    4.7.2 |           4.7.2 |          2.67.0 |  htmlunit-driver |
|    4.7.0 |           4.7.0 |          2.67.0 |  htmlunit-driver |
|    4.6.0 |           4.6.0 |          2.66.0 |  htmlunit-driver |
|          |                 |                 |                  |
|    4.5.2 |           4.5.2 |          2.66.0 |  htmlunit-driver |
|    4.5.0 |           4.5.0 |          2.65.1 |  htmlunit-driver |
|          |                 |                 |                  |
|    4.4.0 |          3.64.0 |          2.64.0 |  htmlunit-driver |
|    4.3.0 |          3.63.0 |          2.63.0 |  htmlunit-driver |
|    4.2.1 |          3.62.0 |          2.62.0 |  htmlunit-driver |
|    4.1.3 |          3.61.0 |          2.61.0 |  htmlunit-driver |
|    4.1.0 | 3.56.0 - 3.60.0 | 2.56.0 - 2.60.0 |  htmlunit-driver |
|    4.0.0 |          3.55.0 |          3.55.0 |  htmlunit-driver |
|          |                 |                 |                  |
| 3.141.59 |      **2.70.0** |          2.70.0 |  htmlunit-driver |
| 3.141.59 |          2.67.0 |          2.67.0 |  htmlunit-driver |
| 3.141.59 |          2.66.0 |          2.66.0 |  htmlunit-driver |
| 3.141.59 |          2.65.0 |          2.65.1 |  htmlunit-driver |
| 3.141.59 |          2.64.0 |          2.64.0 |  htmlunit-driver |
| 3.141.59 |          2.63.0 |          2.63.0 |  htmlunit-driver |
| 3.141.59 |          2.62.0 |          2.62.0 |  htmlunit-driver |
| 3.141.59 |          2.61.0 |          2.61.0 |  htmlunit-driver |
| 3.141.59 |          2.60.0 |          2.60.0 |  htmlunit-driver |


**Maven/Gradle/...**

Simply add a dependency on the latest `htmlunit3-driver` version available in the Maven Central.

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>htmlunit3-driver</artifactId>
    <version>4.15.0</version>
</dependency>
```

Add to your `build.gradle`:

```groovy
implementation group: 'org.seleniumhq.selenium', name: 'htmlunit3-driver', version: '4.15.0'
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
