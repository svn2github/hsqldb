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

	echo HSQLDB build file for jdk 1.1.x and above
	echo we recommend the use of the ANT build.xml instead of this method
#	echo for all jdk's include the path to jdk1.x.x/bin in your system path statement
	echo for jdk1.1.x also use the -cp:a \<classpath\> option to append
	echo    jdk1.1.x/lib/classes.zip to the classpath on your system
	echo example: -cp:a /usr/java/jdk1.1.8/lib/classes.zip

# Note: we are starting at $dbhome/build
echo cd ../
cd ../
echo $pwd_tmp
pwd_tmp=`pwd`
echo rm -r -f classes
rm -r -f classes
echo mkdir classes
mkdir classes
echo cd src
cd src
echo mkdir ../temp
mkdir ../temp
echo cp org/hsqldb/jdbcDataSource*.java ../temp/
cp org/hsqldb/jdbcDataSource*.java ../temp/
echo rm -f org/hsqldb/jdbcDataSource*.java
rm -f org/hsqldb/jdbcDataSource*.java
echo cp org/hsqldb/util/*Swing.java ../temp/
cp org/hsqldb/util/*Swing.java ../temp/
echo rm -f org/hsqldb/util/*Swing.java
rm -f org/hsqldb/util/*Swing.java
echo "$jdkhome/bin/javac" -O -nowarn -d ../classes -classpath "$cp:../classes:../lib/servlet.jar" *.java org/hsqldb/*.java org/hsqldb/lib/*.java org/hsqldb/util/*.java
"$jdkhome/bin/javac" -O -nowarn -d ../classes -classpath "$cp:../classes:../lib/servlet.jar" *.java org/hsqldb/*.java org/hsqldb/lib/*.java org/hsqldb/util/*.java
echo cp ../temp/jdbcDataSource*.java org/hsqldb
cp ../temp/jdbcDataSource*.java org/hsqldb
echo rm -f ../temp/jdbcDataSource*.java
rm -f ../temp/jdbcDataSource*.java
echo cp ../temp/*Swing.java org/hsqldb/util
cp ../temp/*Swing.java org/hsqldb/util
echo rm -f ../temp/*Swing.java
rm -f ../temp/*Swing.java
echo rm -r -f ../temp
rm -r -f ../temp
echo cd ../classes
cd ../classes
echo "$jdkhome/bin/jar" -cf ../lib/hsqldb.jar *.class org/hsqldb/*.class org/hsqldb/lib/*.class org/hsqldb/util/*.class
"$jdkhome/bin/jar" -cf ../lib/hsqldb.jar *.class org/hsqldb/*.class org/hsqldb/lib/*.class org/hsqldb/util/*.class
echo cd ../build
cd ../build

# and we exit.
