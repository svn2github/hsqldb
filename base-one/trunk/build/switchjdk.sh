#!/bin/sh +u
# -----------------------------------------------------
# Run with -help for usage.
# If $JAVA_HOME is set, editing this script should not be required.
# Send any questions to fchoong@user.sourceforge.net
# -----------------------------------------------------

# the value set here will override the value passed by $JAVA_HOME
# Note that if user exported jdkhome in their env, it cleared here.
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

VERBOSE=
NEWVER=
while [ $# -gt 0 ]; do
    case "$1" in -*)	# Switch
    	    case "$1" in *v*) VERBOSE=1;; esac
	;;
	*)	# Version
	    case "$1" in
	    	1.4|1.2|1.1) NEWVER="$1";;
		1.3) NEWVER=1.2;;
	    esac
	;;
    esac
    shift
done
[ -n "$NEWVER" ] || {
    echo "SYNTAX:  $progname [-v] {1.1|1.2|1.3|1.4}" 1>&2
    exit 2
}

Failout() {
    [ "$#" -gt 0 ] || Failout "There is a bad Failout invocation in $progname"
    echo "$progname fatal error:  $@" 1>&2
    exit 1
}

# Note that this includes all source files that have the tags, regardless
# of our new target version.  That's because, depending on what version
# we are coming from, we may need to clean up stuff from other tagged files.
TARGET_SRCFILES='
    jdbcStubs.java
    jdbcConnection.java
    jdbcDatabaseMetaData.java
    jdbcPreparedStatement.java
    jdbcResultSet.java
    jdbcStatement.java
'
[ -n "$TARGET_SRCFILES" ] || exit 0   # Nothing to do
CS_LABELS=
case "$NEWVER" in
    # N.b.  At this time, all target source files reside directly in
    # $dbhome/src/org/hsqldb.  If we ever need to Switch a file that 
    # resides elsewhere, then just change this script to use paths
    # relative to $dbhome instead of to $dbhome/src/org/hsqldb.
    1.1) CS_LABELS='-JAVA2 -JDBC3' ;;
    1.2) CS_LABELS='+JAVA2 -JDBC3' ;;
    1.4) CS_LABELS='+JAVA2 +JDBC3' ;;
esac
[ -n "$CS_LABELS" ] || Failout "Internal error.  "'$CS_LABELS is not set.'

# ../ will lead us to the home
dbhome="$progdir/.."

# absolutize dbhome

dbhome=`cd ${dbhome}; pwd`

# Just macros so commands below will fit onto one line
hsrcdir=$dbhome/src/org/hsqldb
hclsdir=$dbhome/classes/org/hsqldb

# Validate Source files
[ -f "$hsrcdir/util/CodeSwitcher.java" ] || Failout  \
 "CodeSwitcher source file '$hsrcdir/util/CodeSwitcher.java' is not a file"
for file in $TARGET_SRCFILES; do
    [ -f "$hsrcdir/$file" ] || Failout "Target '$hsrcdir/$file' is not a file"
    [ -w "$hsrcdir/$file" ] || Failout "Target '$hsrcdir/$file' is not writable"
done

# Generic initialization for $CLASSPATH, etc.
. ${dbhome}/lib/functions
pre_main


###################   MAIN   ###############################

[ -n "$VERBOSE" ] && set -x

[ -d $dbhome/classes ] || {
    mkdir $dbhome/classes ||
     Failout "Failed to create directory '$dbhome/classes'"
}

cd $hsrcdir || Failout "Failed to cd to '$dbhome/src/org/hsqldb'"

# Build CodeSwitcher.class if it needs to be rebuilt
# Note that this test succeeds if the class file does not exist.
[ util/CodeSwitcher.java -nt $hclsdir/util/CodeSwitcher.class ] && {
    "$jdkhome/bin/javac" -d $dbhome/classes util/CodeSwitcher.java ||
     Failout "Failed to rebuild CodeSwitcher"
}

# Don't put $TARGET_SRCFILES nor $CS_LABELS into quotes, because the both
# may consist of multiple argument tokens.
# This script set those values above, so you don't have to worry about
# spaces inside the individual tokens (there aren't any).
"$jdkhome/bin/java" -classpath "$dbhome/classes:$cp"  \
 org.hsqldb.util.CodeSwitcher $TARGET_SRCFILES $CS_LABELS
