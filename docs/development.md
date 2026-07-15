# ![HtmlUnitDriver Logo](https://github.com/SeleniumHQ/htmlunit-driver/blob/master/htmlunit_webdriver.png)

## Versioning

We use [Semantic Versioning](https://semver.org/):
- **MAJOR** version for incompatible API changes
- **MINOR** version for backwards-compatible functionality additions
- **PATCH** version for backwards-compatible bug fixes


## HtmlUnitDriver Development

These instructions will help you set up the project on your local machine for development and testing purposes.

### Prerequisites

You only need:
- Java Development Kit (JDK) 8 or higher
- Apache Maven (latest stable version recommended)

### Building

1. Clone the repository to your local machine:
   ```bash
   git clone https://github.com/SeleniumHQ/htmlunit-driver.git
   cd htmlunit-driver
   ```

2. Compile the project:
   ```bash
   mvn compile
   ```

### Running the Tests

Execute the test suite with:
    ```bash
    mvn test
    ```

To run a clean build with all tests:
    ```bash
    mvn clean test
    ```

## Contributing

Pull requests and all other community contributions are essential for open source software.
Every contribution—from bug reports to feature requests, typos to full new features—is greatly appreciated.

Before submitting a pull request:
- Ensure all tests pass
- Follow the existing code style
- Add tests for new features
- Update documentation as needed

## Deployment and Versioning

**This section is intended for committers who are packaging a release.**

### New Commiter configuration 

Ensure you have been securely given credentials to deploy to Central

Update ~/.m2/settings.xml to include a central server auth details

```xml
  <server>
      ...
      <server>
          <id>central</id>
          <username>${username-hash}</username>
          <password>${apitoken}</password>
      </server>
  </servers>

```

### Pre-release Checklist

1. Ensure all changes are committed and pushed

2. Verify everything is up to date and all tests pass:
   ```bash
   mvn versions:display-plugin-updates
   mvn versions:display-dependency-updates
   mvn -U clean test
   ```

3. Update expected version details 
   - Update version in 'README.md'
   - Update 'docs\compatibility.md'
   - Commit these changes

### Maven release process (Preferred)

1. Use the release prepare, perform steps which will do the version increments and tagging
   - This will give sane patch level increments, if this is not a patch then override the suggestion given.

   - Prepare release
   ```shell
   mvn release:prepare -Prelease
   ```
   - Perform release
   ```shell
   mvn release:perform -Prelease
   ```

### Manual Deployment process

1. Update version numbers:
    - Update version in 'pom.xml' (remove '-SNAPSHOT' suffix)

#### Build and Deploy

1. Build and deploy artifacts to Maven Central:
   ```bash
   mvn -up clean deploy -Prelease
   ```

2. Publish to Maven Central:
    - Go to [Maven Central Portal](https://central.sonatype.com/)
    - Log in and locate your deployment
    - Verify the contents
    - Publish the package
    - Wait for processing to complete (usually 10-30 minutes)

### Create GitHub Release

1. Navigate to https://github.com/SeleniumHQ/htmlunit-driver
2. Click **Releases** → **Draft a new release**
3. Fill in the release information:
    - **Tag**: Version number (e.g., `4.0.0`)
    - **Release title**: Same as tag (e.g., `4.0.0`)
    - **Description**: Add release notes highlighting new features, fixes, and breaking changes
4. Attach build artifacts (JARs)
5. Click **Publish release**

### Post-release Tasks (Manual process only)

Note: `Maven release process (Preferred)` will do this for you.

1. Update `pom.xml` to next SNAPSHOT version:
   ```xml
   <version>X.Y.Z-SNAPSHOT</version>
   ```

2. Commit the snapshot version:
   ```bash
   git commit -am "Begin development of X.Y.Z-SNAPSHOT"
   git push
   ```

3. Update dependent projects:
    - Update the HtmlUnit `pom.xml` to reference the new release version
