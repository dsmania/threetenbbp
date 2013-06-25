## ThreeTen back-backport project
JSR-310 provides a new date and time library for JDK 1.8.
This project is the backport to JDK 1.6 based on the official backport to JDK 1.7.

The backport is NOT an implementation of JSR-310, as that would require
jumping through lots of unecessary hoops.
Instead, this is a simple backport intended to allow users to quickly
use the JSR-310 API on JDK 1.6.
The backport should be referred to using the "ThreeTen" name.

Active development on JSR-310 is at [OpenJDK](http://openjdk.java.net/projects/threeten/):

This GitHub repository is a fork of the repository used for the official backport that which was also a fork of that one originally used to create JSR-310.
That repository used the same BSD 3-clause license as the backport one, and so does this repository.

Issues related to the backport should be reported on the backport site on GitHub, as it might improve it properly. Anything related with this back-backport to JDK 1.6 will be addressed here.
Pull requests and issues will only be considered so far as matching the behavior
of the real JSR-310. Additional requested features will be rejected.

#### Building
This project builds using maven.

#### Time-zone data
The time-zone database is stored as a pre-compiled dat file that is included in the built jar.
The version of the time-zone data used is stored within the dat file (near the start).
Updating the time-zone database involves using the `TzdbZoneRulesCompiler` class
and re-compiling the jar file.
Pull requests with later versions of the dat file will be accepted.

#### Locale issues
JDK 1.6 does not support calendar information on the Locale class, thus few features related with the chronologies are missing. The only calendar present in Locale resolution is for ja_JP_JP locale, as it is defined as a proper variant with the Japanese Imperial Calendar.
Please, pay attention to the documentation of the method [Chronology.ofLocale(Locale)](http://threeten.github.io/threetenbp/apidocs/org/threeten/bp/chrono/Chronology.html#ofLocale(java.util.Locale\)) as this method had to be reworked to fit the documentation.
Anyway, any code implemented with this back-backport should work exactly the same once ported forward to JDK 1.7 or doing the same changes as in 1.7 to 1.8, just there are some missing features in the Locale.

#### Distribution
If you want to use ThreeTen back-backport in your projects, a distribution is added in this repository under the directory `repo`.

You can choose to use the jars directly or using maven. Since this is not in maven central the workaround would be using a project local repository:

1. Copy the `repo` directory of this project to yours.

2. Include a project local repository in your `pom.xml`:
```
    <repositories>
      ...
      <repository>
        <name>Project local repository</name>
        <id>project.repo</id>
        <url>file:${project.basedir}/repos</url>
      </repository>
      ...
    </repositories>
```
3. Add the dependency:
```
    <dependencies>
      ...
      <dependency>
        <groupId>org.threeten</groupId>
        <artifactId>threetenbp</artifactId>
        <version>0.8.1-jdk6</version>
        <scope>test</scope>
      </dependency>
      ...
    </dependencies>
```
Be careful when using a pom type project on modules, as the local repository is defined under referencing the project.basedir variable. If you're willing to use it so, make it dependent on a custom variable and redefine it in every module (you can read further [here](http://stackoverflow.com/questions/1012402/maven2-property-that-indicates-the-parent-directory/)).

#### FAQs

1. What version of JDK 1.8 does this project map to?
This project currently maps to the M7 milestone.

2. Will the backport be kept up to date?
There will be a release matching the final JDK 1.8 version.
There may or may not be further updates between now and then.

3. Is this project derived from OpenJDK?
No. This project is derived from the Reference Implementation previously hosted on GitHub.
That project had a BSD license, which has been preserved here.
Thus, this project is a fork of the original code before entry to OpenJDK.

4. What's the purpose on doing a back-backport?
Stephen Colebourne has done an impressive job with either Joda Time or the JSR-310. While existing Joda Time, some people have chosen to use the first implementations of the JSR-310 being the v0.6.3 the most used in long time. The original had a small problem when used in some environments and that was it overrode some classes from the standard API (as it's supposed to do any JSR if needed). The Backport had not that issue and this back-backport is an option to upgrade some projects still running in JDK 1.6.
Personally, this is a share of something I needed, since I've been using the JSR-310 v.0.6.3 for about 5 years now.
