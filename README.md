# HtmlUnitDriver

HtmlUnitDriver is a WebDriver compatible driver for the [HtmlUnit](https://www.htmlunit.org) headless browser.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.seleniumhq.selenium/htmlunit3-driver/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.seleniumhq.selenium/htmlunit3-driver)

##### News
[<img src="https://www.htmlunit.org/images/logos/twitter.png" alt="Twitter" height="44" width="60">](https://twitter.com/HtmlUnit)

[![Build Status](https://jenkins.wetator.org/buildStatus/icon?job=HtmlUnitDriver+-+Selenium+4)](https://jenkins.wetator.org/view/HtmlUnit%20Driver/job/HtmlUnitDriver%20-%20Selenium%204/)

## Download and Installation

There are two versions available

### Selenium compatibility

Starting with Selenium 4.5 we use the same version numbers for the driver - e.g. HtmlUnit-Driver 4.22.0
is for Selenium 4.22.0.

An overview of the different versions, the HtmlUnit version used in each case and the compatibility 
can be found in these [tables](compatibility.md).


## Get it!

### Maven

Simply add a dependency on the latest `htmlunit3-driver` version available in the Maven Central.

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>htmlunit3-driver</artifactId>
    <version>4.22.0</version>
</dependency>
```

### Gradle

Add to your `build.gradle`:

```groovy
implementation group: 'org.seleniumhq.selenium', name: 'htmlunit3-driver', version: '4.22.0'
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
