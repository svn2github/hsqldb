#!/bin/sh

# Captive shell for running hsqldb daemons
# $Id$

# Cannot use a "restricted" shell because we need to "cd"
# This is not intended to be user-friendly or safe.
# It is intended to "attempt" to start a database.  If the startup
# fails, then troubleshoot from an interactive shell.
# You are responsible for error-checking.  This script does not check,
# for example, if a DB is already running with the given data set name.
# It also implements only startup, not shutdown.

# SYTAX:  su - thisuser dbname /path/to/hsqldb.jar Web|''
# Examples:  "su - hsqldb db1 /usr/hsqldb/lib/hsqldb.jar Web"
#            "su - tom test /usr/hsqldb1.7.1/lib/hsqldb_j1.4.1.jar ''"

#set -x
set +u

PATH=/bin:/usr/bin
export PATH

# We do not want to inherit env variables (e.g. from somebody "su"ing
# instead of "su -"in
#LOGDIR=
#PIDDIR=
#OWNER=
#GROUP=
#HSQLDB_HOME=
#MAXSTARTTIME=
#MAXSTOPTIME=
#JAVA_HOME=

CFGFILE="%CFGFILE%"

Failout() {
    # Syntax:  Failout 3 "string"  (where 3 is the exit value)
    # (Args not validated, so just use it correctly!)
    Val=$1; shift
    echo "Aborting.  $@" 1>&2
    exit $Val
}

[ -r "$CFGFILE" ] || Failout 9 "Cannot read config file '$CFGFILE'"
sh -n "$CFGFILE" > /dev/null 2>&1 ||
 Failout 11 "Syntax error(s) in config file '$CFGFILE'"
. $CFGFILE

[ -n "$USER" ] || USER="$LOGNAME"   # Cover all UNIXes

[ $# -eq 3 ] ||
 Failout 2 "SYNTAX:  su - $USER dbname /path/to/hsqldb.jar Web|''"

[ -d "$HSQLDB_HOME" ] ||
 Failout 3 '$HSQLDB_HOME '"'$HSQLDB_HOME' not a directory"
[ -d "$PIDDIR" ] ||
 Failout 4 '$PIDDIR '"'$PIDDIR' not a directory"
[ -d "$LOGDIR" ] ||
 Failout 5 '$LOGDIR '"'$LOGDIR' not a directory"
[ -n "$JAVA_HOME" ] && {
    [ -d "$JAVA_HOME" ] || Failout 12 "Java home '$JAVA_HOME' not a directory"
    PATH="$JAVA_HOME/bin:$PATH"
}

echo $$ >> "$PIDDIR/hsqldb_${1}.pid" ||
 Failout 7 "Failed to write pid to '$PIDDIR/hsqldb_${1}.pid'"

cd "$HSQLDB_HOME/data/$1" 2> /dev/null ||
 Failout 6 "Failed to cd to '$HSQLDB_HOME/data/$1'"

exec java -cp "$2" org.hsqldb.${3}Server -database "$1" >> "$LOGDIR/hsqldb_${1}.log" 2>&1

Failout 8 "Failed to exec '$4'"
