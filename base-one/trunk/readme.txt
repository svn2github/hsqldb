Readme File

2002.10.04

HSQLDB 1.7.1

HSQLDB 1.7.1 is a maintenance release, based on 1.7.0. A large
number of bug fixes and minor feature enhancemnts, together with major
speed and memory optimisations are included. No new keyword or major new
capability has been added to 1.7.0.

1.7.1 features major memory usage optimisations, resulting in up to 30%
savings with memory tables and less with cached tables.

This improvement is due to a rewrite on the internal Row and Index Node
classes. While maintaining exactly the same orgininal logic, the new
versions have a smaller memory footprint. This has also resulted in
a modest improvement in speed.

This release also features major speed improvements to all procedure
calls and particularly the identity() function.

The Transfer tool features new functionality, such as database
dump and restore.

Big improvements were made to startup speed where certain types of
VIEW were defined. Also VIEWS featuring function calls are now
supported.

Almost all the bugs that were reported since the release of
1.7.0 have been fixed. Some of these bugs related to data consistency
after some ALTER TABLE commands were issued.

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

Fred Toussi (fredt@users)
http://hsqldb.sourceforge.net
