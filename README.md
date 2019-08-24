[![Travis Build Status](https://travis-ci.com/SeleniumHQ/htmlunit-driver.svg?branch=master)](https://travis-ci.com/SeleniumHQ/htmlunit-driver/)

# HtmlUnitDriver

HtmlUnitDriver is a WebDriver compatible driver for the [HtmlUnit](http://htmlunit.sourceforge.net/) headless browser.

##### News
[<img src="http://htmlunit.sourceforge.net/images/logos/twitter.png" alt="Twitter" height="44" width="60">](https://twitter.com/HtmlUnit)

## Download and Installation

### Maven/Gradle/...

Add a dependency on the latest `htmlunit-driver` version available in the Maven Central, please note that both `artifactId` and `version` are changed to match the dependent HtmlUnit version:

```xml
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>htmlunit-driver</artifactId>
    <version>2.36.0</version>
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
WebDriver webDriver = new HtmlUnitDriver(BrowserVersion.FIREFOX_60);
```

```java
// simple case - javascript support enabled
WebDriver webDriver = new HtmlUnitDriver(true);
```

```java
// specify the browser - javascript support enabled
WebDriver webDriver = new HtmlUnitDriver(BrowserVersion.FIREFOX_60, true);
```


### More customization

HtmlUnit offers a lot more customization options. To adjust these options you can use this pattern.

```java
WebDriver webDriver = new HtmlUnitDriver(BrowserVersion.FIREFOX_60, true) {
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
