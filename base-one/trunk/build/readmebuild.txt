Build instructions for HSQLDB 1.7.2

A jar file for HSQLDB is provided in the /lib directory of the 
.zip package. This jar contains both the database and the 
utilities and has been built with JDK 1.3.1. (#RELEASE VERSION)

The supplied jar can be used with JRE version 1.1.x., 1.2.x,
1.3.x and 1.4.x. No recompilation is necessary.

Possible reasons to rebuild the jar are:

(a) Rebuild with JDK 1.4 to gain cccess to JDBC 3 methods and 
classes such as Savepoint and ParameterMetaData. Also to
speed up large databases with CACHED tables via the nio classes.

(b) Rebuild with any JDK to reduce the size of the jar for small
devices or for better download speed.

(c) Rebuild to work around known compatibility issues with
certain non-standard JDK's.


Different jar sizes

HSQLDB can be built in any combination of four different sizes.

The smallest jar size (hsqldbmin.jar) contains only the database
and JDBC support for in-process mode databases. The next smallest
jar size (hsqldbmain.jar) also contains support for server modes.
The default size (hsqldb.jar) additionally contains the
utilities such as Database Manager and Transfer Tool. The largest 
size (hsqldbtest.jar) includes some test classes as well. You need
the JUnit jar in the /lib directory in order to build and run the
test classes.

The JDK used for the build has a marginal effect on the size.
Newer JDK's support more JDBC methods and classes, resulting in
slightly larger jars. JDK 1.1 is not as advanced as the newer ones
and produces larger class files.

JDK 1.1.x

It is recommended not to use JDK 1.1.x for building the
jar, as this version produces much larger jar sizes and the result
is not upward compatible with newer JDK'S / JRE's. Use JDK 1.3.x
to build the jar instead. You can then deploy the jar in JRE 1.1.

Build methods:

The preferred method of rebuilding the jar is with Ant. After
installing Ant on your system use the following command from the
/build directory:

ant

The command displays a list of different options for building 
different sizes of the HSQLDB Jar. The default jar is built using:

ant jar

The Ant method always builds a jar with the
JDK that is used by Ant and specified in the JAVA_HOME environment
variable. 

Before building the hsqldbtest.jar package, you should download the
junit.jar and put it in the /lib directory, alongside servlet.jar, 
which is included in the .zip package.

Batch Build

The Unix / Linux builds have been written by Blaine Simpson and
support a wide range of JDK's.

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

You can use any recent JDK for building the jar. Use of JDK 1.3.x is
recommended for the widest compatibility.

Javadoc can be built with Ant and batch files.

fredt@users
