## HowTo create a release build

1. Update the dependency version of htmlunit.

2. Update the pom version to match htmlunit:

        mvn versions:set -DnewVersion=2.28

4. Commit&Push the changes

5. Deploy to maven central (`clean` is a must to avoid Eclipse/maven conflicts):

        mvn clean deploy

6. Log into sonatype nexus - https://oss.sonatype.org/index.html#stagingRepositories
   close and release the package

7. Deploy the javadoc to gh-pages:

        mvn javadoc:javadoc scm-publish:publish-scm

   You may need to:

        git config --global commit.gpgsign false

8. Create version on github
   open https://github.com/SeleniumHQ/htmlunit-driver/releases and click 'Draft a new release'
   fill the release number and use the same as title
   write some notes about the changes done for this release

   add the files
   * htmlunit-driver-2.xx.x-jar-with-dependencies.jar and
   * htmlunit-driver-2.28.5.jar

   Publish the release (github will create a tag for you in the background)



9. Update the pom to the next snapshot version:

        mvn versions:set -DnewVersion=2.29-SNAPSHOT

10. Change the `README.md`

11. Commit the new version and then push:

        git commit -am "bumping to 2.29-SNAPSHOT"; git push

