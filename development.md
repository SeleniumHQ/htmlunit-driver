# ![HtmlUnitDriver Logo](https://github.com/SeleniumHQ/htmlunit-driver/blob/master/htmlunit_webdriver.png)

## HtmlUnitDriver Development

These instructions will get you a copy of the project up and running on your local machine 
for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

You simply only need a local maven installation.


### Building

Create a local clone of the repository and you are ready to start.

Open a command line window from the root folder of the project and call

```
mvn compile
```

### Running the tests

```
mvn test
```

## Contributing

Pull Requests and all other Community Contributions are essential for open source software.
Every contribution - from bug reports to feature requests, typos to full new features - are greatly appreciated.

## Deployment and Versioning

This part is intended for committer who are packaging a release.

* Check all your files are checked in
* Execute these mvn commands to be sure all tests are passing and everything is up to data

```
   mvn versions:display-plugin-updates
   mvn versions:display-dependency-updates
   mvn -U clean test
```

* Update the version number in pom.xml and README.md
* Commit the changes


* Build and deploy the artifacts

```
   mvn -up clean deploy
```

* Go to [Maven Central Portal](https://central.sonatype.com/) and process the deploy
    - publish the package and wait until it is processed

* Create the version on GitHub
    * login to GitHub and open project https://github.com/HtmlUnit/htmlunit-cssparser
    * click Releases > Draft new release
    * fill the tag and title field with the release number (e.g. 4.0.0)
    * append/ the build artifacts and
    * publish the release

* Update the version number in pom.xml to start next snapshot development
* Update the htmlunit pom to use the new release
