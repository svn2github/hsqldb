#!/bin/sh

# $Id: buildjar.sh,v 1.20 2002/12/08 23:29:03 unsaved Exp $

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
while [ $# -gt 0 ]; do
    case "$1" in -*)	# Gobble up all Switches.  Leave anything else.
    	    case "$1" in *v*) VERBOSE=1;; esac
	    shift
	;;
	*) break;;  # First non-switch arg encountered
    esac
done
[ $# -eq 0 ] || {
    echo "SYNTAX:  $progname [-v]" 1>&2
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

AWK=awk
[ "`uname`" = SunOS ] && AWK=nawk

# Just macros so commands below will fit onto one line
hsrcdir=$dbhome/src/org/hsqldb
hclsdir=$dbhome/classes/org/hsqldb

# Validate Source files
[ -f "$hsrcdir/util/EchoProperty.java" ] || Failout  \
 "EchoProperty source file '$hsrcdir/util/EchoProperty.java' is not a file"

# Generic initialization for $CLASSPATH, etc.
. ${dbhome}/lib/functions
pre_main

#############################   Main   ###########################

[ -n "$VERBOSE" ] && set -x

[ -d $dbhome/classes ] || {
    mkdir $dbhome/classes ||
     Failout "Failed to create directory '$dbhome/classes'"
}

cd $hsrcdir || Failout "Failed to cd to '$dbhome/src/org/hsqldb'"

# Build EchoProperty.class if it needs to be rebuilt
# Note that this test succeeds if the class file does not exist.
NewerThan util/EchoProperty.java $hclsdir/util/EchoProperty.class && {
    echo 'Rebuilding EchoProperty.class...'
  "$jdkhome/bin/javac" -target 1.1 -d $dbhome/classes util/EchoProperty.java ||
     Failout "Failed to rebuild EchoProperty"
}
RAWVER=
RAWVER=`exec "$jdkhome/bin/java" -classpath "$dbhome/classes:$cp"  \
 org.hsqldb.util.EchoProperty java.version` ||
  Failout "Failed to determine Java version with EchoProperty"
JDKVER=
case "$RAWVER" in
    1.1.*) JDKVER=1.1;;
    1.[23].*) JDKVER=pre1.4;;
    1.4.*) JDKVER=1.4;;
    *) Failout "Unrecognized Java version '$RAWVER'";;
esac

echo "Building for target '$JDKVER'"
case "$JDKVER" in
    1.1) echo "    (Resultant jar can only be used with Java JRE < 1.2)";;
    pre1.4) echo "    (Resultant jar can be used with any versoin Java JRE)";;
    1.4) echo "    (Resultant jar can only be used with Java JREs >= 1.4)";;
esac

NOSWING=
[ "$JDKVER" = 1.1 ] && NOSWING=1
SWITCHERVERBOSE=
[ -n "$VERBOSE" ] && SWITCHERVERBOSE=-v
echo 'Running CodeSwitcher...'
$dbhome/build/settargetjre.sh $SWITCHERVERBOSE $JDKVER || exit $?

cd $dbhome || Failout "Failed to cd to '$dbhome'"
cd src || Failout "Failed to cd to '$dbhome/src'"

echo 'Generating source file list...'
LISTFILE=/tmp/list.$$
rm -f $LISTFILE
[ -f $LISTFILE ] && Failout "Failed to remove temp list file '$LISTFILE'"
touch $LISTFILE || Failout "Failed to create temporary list file '$LISTFILE'"
find * -name '*.java' -print | while read file; do case "$file" in
    org/hsqldb/lib/*) echo $file; continue;;
    org/hsqldb/resources/*.java) echo $file; continue;;
    org/hsqldb/util/*Swing.java) [ -n "$NOSWING" ] || echo $file; continue;;
    org/hsqldb/util/Zaurus*.java) continue;;
    org/hsqldb/util/*) echo $file; continue;;
    org/hsqldb/*/*) continue;;  # Nothing else from this deep in tree
    org/hsqldb/jdbcStubs.java) continue;;  # Why unnecessary??
    org/hsqldb/jdbcDataSource*.java)
        [ "$JDKVER" = 1.4 ] && echo $file; continue;;
    org/hsqldb/*) echo $file; continue;;
    */*) Failout "File '$file' is at an unexpected location";;
    *) echo $file; continue;;  # This is at top level: src/X.java
esac; done |
$AWK '{
    x = $0; sub(/\.java$/, ".class", x); printf ("%s ../classes/%s\n", $0, x);
}' | while read src cls; do
    # cls = The primary class file for each java file
    [ -f "$src" ] ||
     Failout "Choked on file '$src' from source file list file '$LISTFILE'"
    NewerThan "$src" "$cls" && echo "$src"
done > $LISTFILE
NUMFILES=`$AWK 'END { print NR;}' $LISTFILE`
# TODO:  Use NewerThan() on the $LISTFILE records to exclude the java
#        files that are not newer than the main corresponding class file.

# Put .properties files into place
[ -d $dbhome/classes/org/hsqldb/resources ] || {
    mkdir -p $dbhome/classes/org/hsqldb/resources ||
     Failout "Failed to create directory '$dbhome/classes/org/hsqldb/resources'"
}
for file in org/hsqldb/resources/*.properties; do
    NewerThan $file ../classes/$file && cp -p $file $dbhome/classes/$file
done

# Main Compile
if [ "$NUMFILES" = 0 ]; then
    echo 'Skipping compile because no source files have been modified.'
else
    echo "Compiling $NUMFILES source file(s) ..."
    "$jdkhome/bin/javac" -target 1.1 -O -nowarn -d ../classes  \
     -classpath "$cp:../classes" `cat $LISTFILE` || exit $?
fi

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
    org/hsqldb/resources/*) echo $file; continue;;
    org/hsqldb/util/Zaurus*.class) continue;;
    org/hsqldb/util/*Swing.class) [ -n "$NOSWING" ] || echo $file; continue;;
    org/hsqldb/util/*Swing\$*.class) [ -n "$NOSWING" ] || echo $file; continue;;
    org/hsqldb/util/*) echo $file; continue;;
    org/hsqldb/*/*) continue;;  # Nothing else from this deep in tree
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
NUMFILES=`$AWK 'END { print NR;}' $LISTFILE`

echo "Assembling $NUMFILES class files into new jar file..."
"$jdkhome/bin/jar" -cf ../lib/hsqldb.jar `cat $LISTFILE` $HSQLDB_GIF
rm $LISTFILE
