HSQLDB can be built in any combination of four different sizes and
three JRE (Java Runtime Environment) versions.

The smallest jar size (hsqldbmin.jar) contains only the database
and JDBC support for in-process mode databases. The next smallest jar size
(hsqldbmain.jar) also contains support for server modes.
The default size (hsqldb.jar) additionally contains the
utilities. The largest size (hsqldbtest.jar) includes some test
classes as well. You need the JUnit jar in the /lib directory in
order to build and run the test classes.

A jar file for HSQLDB is provided in the .zip package. This jar
contains both the database and the utilities and has been built
with JDK 1.3.1.

From version 1.7.2 you can also run this jar with JRE version 1.1.x.
Unlike previous versions, no recompilation is necessary.

The preferred method of rebuilding the jar is with Ant. After
installing Ant on your system use the following command from the
/build directory:

ant

The command displays a list of different options for building 
different sizes of the HSQLDB Jar. The default is built using:

ant jar

The Ant method always builds a jar that is compatible with the
JDK that is used by Ant and specified in the JAVA_HOME environment
variable. It is recommended not to use JDK 1.1.x for building the
jar, as this version produces much larger jar sizes.

Before building the hsqldbtest.jar package, you should download the
junit.jar and put it in the /lib directory, alongside servlet.jar, 
which is included in the .zip package.

Batch Build

A set of MSDOS batch files is also provided. These produce only
the default jar size. The path and classpath variables for the JDK
should of course be set before running any of the batch files.

If you are compiling for JDK's other than 1.2.x or 1.3.x, you should
use the appropriate switchtoJDK11.bat or switchtoJDK14.bat to adapt
the source files to the target JDK before running the appropriate
buildJDK11.bat or buildJDK14.bat

Linux build

Linux scripts with the same functionality as the MSDOS batch files
are provided.

JDK and JRE versions

You can use any recent JDK for building the jar. Use of JDK 1.3.x or
1.4.x is recommended.

Javadoc can be built with Ant and batch files.

fredt@users
