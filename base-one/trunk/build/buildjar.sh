#!/bin/sh

#  N.b.:   NOT FUNCTIONAL YET!!!!!


# -----------------------------------------------------
# If $JAVA_HOME is set, editing this script should not be required.
# Post questions to the appropriate hsqldb forum at sourceforge.net
# -----------------------------------------------------

# the value set here will override the value passed by $JAVA_HOME or the -jdkhome switch
jdkhome=""

PRG=$0

# resolve symlinks
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

VERBOSE=
JDKVER=
while [ $# -gt 0 ]; do
    case "$1" in -*)	# Switch
    	    case "$1" in *v*) VERBOSE=1;; esac
	;;
	*)	# Version
	    case "$1" in
	    	1.4|1.2|1.1) JDKVER="$1";;
		1.3) JDKVER=1.2;;
	    esac
	;;
    esac
    shift
done
[ -n "$JDKVER" ] || {
    echo "SYNTAX:  $progname [-v] {1.1|1.2|1.3|1.4}" 1>&2
    exit 2
}

Failout() {
    [ "$#" -gt 0 ] || Failout "There is a bad Failout invocation in $progname"
    echo "$progname fatal error:  $@" 1>&2
    exit 1
}

# ../ will lead us to the home
dbhome="$progdir/.."

# absolutize dbhome

dbhome=`cd ${dbhome}; pwd`

$dbhome/build/switchjdk.sh $JDKVER || exit $?

# Generic initialization for $CLASSPATH, etc.
. ${dbhome}/lib/functions
pre_main

#############################   Main   ###########################

cd $dbhome || Failout "Failed to cd to '$dbhome'"
rm -r -f classes
cd src || Failout "Failed to cd to '$dbhome/src'"
mkdir $dbhome/classes || Failout "Failed to create directory '$dbhome/classes'"

[ "$JDKVER" = 1.4 ] || {
    # Following will fail if user interrupted a previous invocation and "temp"
    # is still populated.
    mkdir ../temp
    # Following will fail if user interrupted a previous invocation and these
    # files are already moved to "temp".
    cp -p org/hsqldb/jdbcDataSource*.java ../temp/
    rm -f org/hsqldb/jdbcDataSource*.java
}
[ "$JDKVER" = 1.1 ] && {
    # Following will fail if user interrupted a previous invocation and these
    # files are already moved to "temp".
    cp -p org/hsqldb/util/*Swing.java ../temp/
    rm -f org/hsqldb/util/*Swing.java
}

# Main Compile
"$jdkhome/bin/javac" -O -nowarn -d ../classes -classpath "$cp:../classes" *.java org/hsqldb/*.java org/hsqldb/lib/*.java org/hsqldb/util/*.java

[ "$JDKVER" = 1.1 ] && {
    cp -p ../temp/*Swing.java org/hsqldb/util ||
     Failout "Failed to restore *Swing.java files"
    rm -f ../temp/*Swing.java
}
[ "$JDKVER" = 1.4 ] || {
    cp -p ../temp/jdbcDataSource*.java org/hsqldb ||
     Failout "Failed to restore jdbcDataSource*.java files"
    rm -f ../temp/jdbcDataSource*.java
    rm -r -f ../temp
}

# Build jar
cd ../classes || Failout "Failed to cd to '$dbhome/classes'"
[ -f ../src/org/hsqldb/util/hsqldb.gif ] &&
 [ ../src/org/hsqldb/util/hsqldb.gif -nt org/hsqldb/util/hsqldb.gif ] && {
    cp -p ../src/org/hsqldb/util/hsqldb.gif org/hsqldb/util ||
     Failout "Failed to copy hsqldb.gif to class branch"
}
HSQLDB_GIF=
[ "$JDKVER" != 1.1 ] && [ -f org/hsqldb/util/hsqldb.gif ] &&
 HSQLDB_GIF=org/hsqldb/util/hsqldb.gif
exec "$jdkhome/bin/jar" -cf ../lib/hsqldb.jar *.class org/hsqldb/*.class org/hsqldb/lib/*.class org/hsqldb/util/*.class $HSQLDB_GIF
