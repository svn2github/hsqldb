#!/bin/sh

#  N.b.:   NOT FUNCTIONAL YET!!!!!

NOSWING=   # I don't know when we want this set


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
export VERBOSE
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

SWITCHERVERBOSE=
[ -n "$VERBOSE" ] && SWITCHERVERBOSE=-v
$dbhome/build/switchjdk.sh $SWITCHERVERBOSE $JDKVER || exit $?

# Generic initialization for $CLASSPATH, etc.
. ${dbhome}/lib/functions
pre_main

#############################   Main   ###########################

[ -n "$VERBOSE" ] && set -x

cd $dbhome || Failout "Failed to cd to '$dbhome'"
echo 'Wiping classes branch...'
rm -r -f classes
cd src || Failout "Failed to cd to '$dbhome/src'"
mkdir $dbhome/classes || Failout "Failed to create directory '$dbhome/classes'"

echo 'Generating source file list...'
LISTFILE=/tmp/list.$$
rm -f $LISTFILE
[ -f $LISTFILE ] && Failout "Failed to remove temp list file '$LISTFILE'"
touch $LISTFILE || Failout "Failed to create temporary list file '$LISTFILE'"
find * -name '*.java' -print | while read file; do case "$file" in
    org/hsqldb/lib/*) echo $file; continue;;
    org/hsqldb/*/*) continue;;  # Nothing else from this deep in tree
    org/hsqldb/util/*Swing.java) [ "$NOSWING" ] || echo $file; continue;;
    org/hsqldb/util/*) echo $file; continue;;
    org/hsqldb/jdbcStubs.java) continue;;  # Why unnecessary??
    org/hsqldb/jdbcDataSource*.java)
        [ "$JDKVER" = 1.4 ] && echo $file; continue;;
    org/hsqldb/*) echo $file; continue;;
    */*) Failout "File '$file' is at an unexpected location";;
    *) echo $file; continue;;  # This is at top level: src/X.java
esac; done > $LISTFILE

# TODO:  Use NewerThan() on the $LISTFILE records to exclude the java
#        files that are not newer than the main corresponding class file.

# Main Compile
echo 'Compiling...'
"$jdkhome/bin/javac" -O -nowarn -d ../classes -classpath "$cp:../classes" `cat $LISTFILE`

# Build jar
cd ../classes || Failout "Failed to cd to '$dbhome/classes'"
[ -f ../src/org/hsqldb/util/hsqldb.gif ] &&
  NewerThan ../src/org/hsqldb/util/hsqldb.gif org/hsqldb/util/hsqldb.gif && {
    [ -d  org/hsqldb/util ] || mkdir -p org/hsqldb/util
    cp -p ../src/org/hsqldb/util/hsqldb.gif org/hsqldb/util/hsqldb.gif ||
     Failout "Failed to copy hsqldb.gif to class branch"
}
HSQLDB_GIF=
[ "$JDKVER" != 1.1 ] && [ -f org/hsqldb/util/hsqldb.gif ] &&
 HSQLDB_GIF=org/hsqldb/util/hsqldb.gif

echo 'Generating jar content file list...'
find * -name '*.class' -print | while read file; do case "$file" in
    org/hsqldb/lib/*) echo $file; continue;;
    org/hsqldb/*/*) continue;;  # Nothing else from this deep in tree
    org/hsqldb/util/*Swing.java) [ "$NOSWING" ] || echo $file; continue;;
    org/hsqldb/util/*) echo $file; continue;;
    org/hsqldb/jdbcStubs*.class) continue;;  # Why unnecessary??
    org/hsqldb/jdbcDataSource*.class)
        [ "$JDKVER" = 1.4 ] && echo $file; continue;;
    org/hsqldb/Array*.class) continue;;
    org/hsqldb/Blob*.class) continue;;
    org/hsqldb/Clob*.class) continue;;
    org/hsqldb/Ref*.class) continue;;
    org/hsqldb/Map*.class) continue;;
    org/hsqldb/*.class) echo $file; continue;;
    */*) Failout "File '$file' is at an unexpected location";;
    *) echo $file; continue;;  # This is at top level: src/X.java
esac; done > $LISTFILE

echo 'Assembling jar file...'
exec "$jdkhome/bin/jar" -cf ../lib/hsqldb.jar `cat $LISTFILE` $HSQLDB_GIF
