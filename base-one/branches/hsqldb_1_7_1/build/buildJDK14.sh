#!/bin/sh
# -----------------------------------------------------
# Run with -help for usage.
# If $JAVA_HOME is set, editing this script should not be required.
# Send any questions to fchoong@user.sourceforge.net
# -----------------------------------------------------

# the value set here will override the value passed by $JAVA_HOME or the -jdkhome switch
jdkhome=""

PRG=$0

#
# resolve symlinks
#

while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '^.*-> \(.*\)$' 2>/dev/null`
    if expr "$link" : '^/' 2> /dev/null >/dev/null; then
	PRG="$link"
    else
	PRG="`dirname $PRG`/$link"
    fi
done

progdir=`dirname $PRG`
progname=`basename $0`

# ../ will lead us to the home
dbhome="$progdir/.."

# absolutize dbhome

dbhome=`cd ${dbhome}; pwd`

#
# bring in needed functions

. ${dbhome}/lib/functions

#--------------------------------------------------------------------------------------------------------------
pre_main

#
# let's go
#

# Note: we are starting at $dbhome/build
echo HSQLDB build file for jdk 1.4
echo we recommend the use of the ANT build.xml instead of this method
echo for all jdk\'s include the path to jdk1.x.x\bin in your system path statement
echo cd ../
cd ../
echo rm -r -f classes
rm -r -f classes
echo mkdir classes
mkdir classes
echo cd src
cd src
echo "$jdkhome/bin/javac" -O -nowarn -d ../classes -classpath "$cp:../classes:../lib/servlet.jar" *.java org/hsqldb/*.java org/hsqldb/lib/*.java org/hsqldb/util/*.java
"$jdkhome/bin/javac" -O -nowarn -d ../classes -classpath "$cp:../classes:../lib/servlet.jar" *.java org/hsqldb/*.java org/hsqldb/lib/*.java org/hsqldb/util/*.java
echo cd ../classes
cd ../classes
echo cp ../src/org/hsqldb/util/hsqldb.gif org/hsqldb/util
cp ../src/org/hsqldb/util/hsqldb.gif org/hsqldb/util
echo "$jdkhome/bin/jar" -cf ../lib/hsqldb.jar *.class org/hsqldb/*.class org/hsqldb/lib/*.class org/hsqldb/util/*.class org/hsqldb/util/*.gif
"$jdkhome/bin/jar" -cf ../lib/hsqldb.jar *.class org/hsqldb/*.class org/hsqldb/lib/*.class org/hsqldb/util/*.class org/hsqldb/util/*.gif
echo cd ../build
cd ../build

# and we exit.
