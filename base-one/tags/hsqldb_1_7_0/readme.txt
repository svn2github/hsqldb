Readme File

2002.07.14

HSQLDB 1.7.0

HSQLDB 1.7.0 offers major new functionality over version 1.6.1 released
a year ago and the previous versions of HSQLDB and HypersonicSQL.

A very large number of bugs have been fixed, new features added and
doucumentation improved.

A JAR file compiled with 1.3.0 is included in the /lib directory.

The source has been tested with JDK 1.1.8, 1.3.0 and 1.4.0.
Both the Ant (build.xml) and batch build methods are supported. The
build scripts are all in the /build directory of the distribution.
The new Ant script detects the JDK version that has been set up
with the JAVA_HOME environment variable and compiles HSQLDB to run
with that version of JVM.

Also note that the enclosed JAR is the output of "ant jar" which
excludes the test classes. You can now use "ant jartest" to build a
more inclusive JAR which contains the test classes as well. You
need the JUnit JAR in your classpath or in the /lib
directory in order to compile the hsqldbtest.jar. This JAR is
available from the JUnit SourceForge site.

Some documentation is included in the /doc directory.
changelog.txt lists the more important changes from version
1.6.1. Various HTML files cover the new features of the software
in more detail.

Javadoc documentation features new, extensive coverage for all 
jdbcXXXX.java files. This documentation covers the interaction between
application programs and HSQLDB. Javadoc for public classes is
included.

I would like to thank all our users, contributors and developers who
have made this release possible.

Fred Toussi (fredt@users)
http://hsqldb.sourceforge.net
