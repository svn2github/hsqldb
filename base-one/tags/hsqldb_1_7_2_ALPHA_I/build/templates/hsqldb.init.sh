#!/bin/sh

# $Id: hsqldb.init.sh,v 1.1 2002/10/12 01:15:46 unsaved Exp $

# Since this script handles multiple databases, the return value indicates
# a negative with any of the database.  I.e., if you have five databases
# and ANY of them fail, this script will return error status (since it is
# only possible to return one status).
# Messages to stderr and log files will identify problems more specifically.

# SECURITY:  We set a secure and portable (but not optimized) path above.
# When we get to the uname case below, we will set a good OS-specific path.
# Please do not think that you are improving this script by changing the
# program invocations to absolute paths.
# Using a secure path is far more flexible and portable than hardcoding
# paths-- that's the whole purpose of search paths.
# So I repeat, unless there's a damned good reason, don't ugly this script 
# up by hardcoding the paths.

# Only stdout and stderr from the Java process, and a single startup attempt
# message get written to the database-specific log hsqldb_DBNAME.log.
# All other messages go to the global log hsqldb.log (the database is
# identified in the individual messages where appropriate).

# Algorithm for where to use "java" from is:
#  (1) If $JAVA_HOME is set in the config file, then use that.  Otherwise...
#  (2) If there's an executable "java" in any of the traditional OS-specific
#      bin directories, use that.
# This is accomplished by making a search path of
# $JAVA_HOME/bin + traditional OS path

set +u  # It is not an error to evaluate unset variables
export PATH

# Take care of OS-specifics right up front
# Liberal but safe path needed for the OS block.  The path will be
# perfected very shortly
PATH=/sbin:/usr/sbin:/bin:/usr/bin
# N.b.:  when adding support for new OSes, you must define all of hte
#        variables and paths that are set in the SunOS block (incl. $PATH).
# Postnote:  May have to add a function for su-ing here.  su args aren't
#        even portable across Solaris versions.
case "`uname`" in
    SunOS)
	PATH=/usr/bin
	nawk -- '' /dev/null > /dev/null 2>&1 || {
	    echo 'Aborting.  No nawk in following path' 1>&2
	    echo "  $PATH" 1>&2
	    exit 10
	}
	goodawk() {
	    nawk "$@"
	}
	echonoterm() {	# Echo without trailing newline
	    echo "$@\c"
	}
    ;;
    *)
    	echo 'Aborting.  Your OS is not supported yet.  If you seriously' 1>&2
	echo "want support for it, email ${AUTHOR_EMAILADDR}."
	exit 10
    ;;
esac

CFGFILE="%CFGFILE%"
INITSCRIPT="%INITSCRIPT%"
AUTHOR_EMAILADDR=blaine.simpson@admc.com

# This writes to the global hsqldb.log file and to stderr.
Bitch() {
    # SYNTAX:  Bitch 'dbid' 'message'  (dbid may be null)
    [ $# -lt 2 ] && Failout '' 'Assertion failed in function hsqldb:Bitch()'
    LOGFILE=
    LABEL=
    [ -n "$LOGDIR" ] && LOGFILE=$LOGDIR/hsqldb.log
    [ -n "$1" ] && LABEL="($1) "
    shift
    [ -n "$LOGFILE" ] && {
	date "+%Y/%m/%d %H:%M:%S  ${LABEL}$@" >> $LOGFILE || {
	  echo "CRITICAL ERROR:  Unable to write to hsqldb log '$LOGFILE'" 1>&2
	    exit 2
	}
    }
    echo "${LABEL}$@" 1>&2
}

Failout() {
    # SYNTAX:  Failout 'dbid' 'message'  (dbid may be null)
    [ $# -lt 2 ] &&
     Failout '' '' 'Assertion failed in function hsqldb:Failout()'
    ARG1="$1"; shift
    Bitch "$ARG1" "Aborting hsqldb.  $@"
    exit 1
}

Goodstring() {
    [ $# -ne 1 ] &&
     Failout '' 'Assertion failed in function hsqldb:Goodstring()'
    # Put any other illegal chars inside of the single quotes (1st and 2nd 's).
    # (N.b. the first 2 chars are space and tab.
    # \t would be safer and easier to read, but is not portable.
    [ -n "$@" ] || return 1
    case "$@" in *['     &;><*[]()$?"'"'"]*) return 1;; esac
    return 0
}

IsInteger() {
    [ $# -ne 1 ] &&
     Failout '' 'Assertion failed in function hsqldb:IsInteger()'
    [ -n "$@" ] || return 1
    # Can't do a negative char. class shell in Bourne, like [^0-9] :(
    # Have to list every freaking other character.  Ug.
    case "$@" in *[a-zA-Z"'"' 	`~!@#$%^&*()-_=+\|[]{};:"<>,./?']*) return 1;;
    esac
    return 0
}

safecd() {
    # Failure of the built-in "cd" command can completely kill the shell,
    # thereby aborting this script with no error-handling at all.
    # Therefore, we need to try to see if the cd will work first.
    `cd "$@"` || return $?
    cd "$@"
}

# If you choose strict security over flexibility, then de-comment these
# lines.  That way, the values of these variable MUST come from the
# config file (user's environment will be ignored).

#LOGDIR=
#PIDDIR=
#OWNER=
#GROUP=
#HSQLDB_HOME=
#MAXSTARTTIME=
#MAXSTOPTIME=
#JAVA_HOME=


[ -f "$INITSCRIPT" ] || Failout '' 'Invalid value for $INITSCRIPT.'
[ -f $CFGFILE ] || Failout '' "Config file '$CFGFILE' has not been set up."
sh -n "$CFGFILE" > /dev/null 2>&1 ||
 Failout '' "Config file '$CFGFILE' has has syntax error(s)."
. $CFGFILE

# Validation
[ -n "$LOGDIR" ] || Failout '' '$LOGDIR not set in '$CFGFILE
Goodstring "$LOGDIR" || {
   TMPSTR="$LOGDIR"
   LOGDIR=
   Failout ''  \
   "Log dir. '$TMPSTR' in '$CFGFILE' contains illegal character(s).  Fix that!"
}
[ -d "$LOGDIR" ] || {
   TMPSTR="$LOGDIR"
   LOGDIR=
   Failout '' "Logging directory '$TMPSTR' is not a directory"
}
[ -n "$OWNER" ] || Failout '' '$OWNER not set in '$CFGFILE
[ -n "$GROUP" ] || Failout '' '$GROUP not set in '$CFGFILE
[ -n "$PIDDIR" ] || Failout '' '$PIDDIR not set in '$CFGFILE
[ -n "$HSQLDB_HOME" ] || Failout '' '$HSQLDB_HOME not set in '$CFGFILE
for thing in "$PIDDIR" "$HSQLDB_HOME" "$OWNER" "$GROUP"; do
    Goodstring "$thing" || Failout ''  \
    "Value '$thing' in '$CFGFILE' contains illegal character(s).  Fix that!"
done
[ -d "$HSQLDB_HOME" ] ||
 Failout '' "Hsqldb home directory '$HSQLDB_HOME' is not a directory"
[ -d "$PIDDIR" ] || 
 Failout '' "Pid file directory '$PIDDIR' is not a directory"
IsInteger "$MAXSTARTTIME"||
 Failout '' '$MAXSTARTTIME'" '$MAXSTARTTIME' not an integer"
IsInteger "$MAXSTOPTIME" ||
 Failout '' '$MAXSTOPTIME'" '$MAXSTOPTIME' not an integer"
[ -n "$JAVA_HOME" ] && {
    [ -d "$JAVA_HOME" ] || Failout '' "Java home '$JAVA_HOME' not a directory"
    PATH="$JAVA_HOME/bin:$PATH"
}
JVER=`java -version 2>&1 |
 grep -i '\<java version\>' | sed -e 's:[^"]*"::; s:\".*::;'`
[ -n "$JVER" ] || Failout '' "Failed to obtain version from 'java'"
CPADDN=   # Classpath addition
case "$JVER" in 1.1.*)
    [ -n "$JAVA_HOME" ] ||
     Failout '' 'Due to Java version 1 Classpath requirements, you must set
$JAVA_HOME'" in '$CFGFILE'"
    CPADDN=":$JAVA_HOME/lib/classes.zip"
;; esac

# Construct list of DB names
# AND...
# Check pidfiles.  Remove stales.  Verify contain a digit.  Generate $RUNNINGS.
# I realize that databases could start or stop between now and when the last
# database is processed in the main section, but in general, init scripts
# should not be used at the same time as external agents are starting or 
# stopping daemons.
DBNAMES=
RUNNINGS=	# Running databases
NOTRUNNINGS=	# Non-Running databases
for dir in $HSQLDB_HOME/data/*; do
    [ -d "$dir" ] || continue
    # -a and -e are not portable (e.g. don't work with Solaris Bourne)
    [ ! -f $dir/server.properties ] && [ ! -f $dir/webserver.properties ] &&
     continue
    dbname=`basename $dir`
    Goodstring "$dbname" ||
     Failout "$dbname" "Database name contains llegal character(s).  Fix that!"
    [ -n "$DBNAMES" ] && DBNAMES="$DBNAMES "  # add delimiter
    DBNAMES="${DBNAMES}$dbname"
    PIDFILE=$PIDDIR/hsqldb_${dbname}.pid
    DPID=
    [ -f $PIDFILE ] && [ ! -s $PIDFILE ] && {
	Bitch $dbname "Removing empty pid file '$PIDFILE'"
	rm $PIDFILE
    }
    [ -f $PIDFILE ] && {
	[ -r $PIDFILE ] || Failout $dbname "Unable to read pid file '$PIDFILE'"
	DPID="`cat $PIDFILE`"
    	IsInteger "$DPID" || Failout $dbname "Bad pid file '$PIDFILE'"
    }
    [ -n "$DPID" ] && {
    	kill -0 $DPID 2> /dev/null || {
	    # This is no big deal, so we're not setting $RETVAL.
    	    Bitch $dbname "Pid $DPID is not running.  Removing stale pid file."
	    rm -f $PIDFILE
	    DPID=
    	}
    }
    [ -n "$DPID" ] || {
    	[ -n "$NOTRUNNINGS" ] && NOTRUNNINGS="$NOTRUNNINGS "  # add delimiter
    	NOTRUNNINGS="${NOTRUNNINGS}$dbname"
	continue
    }
    [ -n "$RUNNINGS" ] && RUNNINGS="$RUNNINGS "  # add delimiter
    RUNNINGS="${RUNNINGS}$dbname"
done

[ -n "$VERBOSE" ] && echo "Known databases: ($DBNAMES)"
[ -n "$DBNAMES" ] || Failout ''  \
 'No databases set up.  You must have [web]server.properties files in place.'
unset TMPVAR
RETVAL=0   # Return value is success unless anything fails

case "$1" in
status)
    # We do not use $RUNNINGS, since we want to tell user about every
    # database, whether it is running or not.
    for dbname in $DBNAMES; do
	# $PIDFILE has been validated above.
	# If something else is monkeying with startups/shutdowns, it's 
	# possible that a pidfile could get messed up between then and now.
    	PIDFILE=$PIDDIR/hsqldb_${dbname}.pid
	DPID=
    	[ -r $PIDFILE ] && DPID="`cat $PIDFILE`"
	WEB=
	[ -r $HSQLDB_HOME/data/$dbname/server.properties ] || WEB=Web
    	[ -n "$DPID" ] || {
	    echo "Database '$dbname' ${WEB}Server not running"
	    RETVAL=1
	    continue
	}
	echo "Database '$dbname' ${WEB}Server running with pid $DPID"
    done
    exit $RETVAL
    ;;
stop)
    echo 'Stopping all known running hsqldb daemon(s)'
    export cnt

    for dbname in $RUNNINGS; do
    	PIDFILE=$PIDDIR/hsqldb_${dbname}.pid
	WEB=
	[ -r $HSQLDB_HOME/data/$dbname/server.properties ] || WEB=Web
	DPID=
    	[ -r $PIDFILE ] && DPID="`cat $PIDFILE`"
    	kill -INT $DPID 2> /dev/null || {
	    Bitch $dbname "Kill -INT of ${WEB}Server failed"
	    continue
	}
    	cnt=$MAXSTARTTIME
    	while [ $cnt -gt 0 ]; do
    	    kill -0 $DPID 2> /dev/null || break
	    cnt=`goodawk -- 'BEGIN { print (ENVIRON["cnt"] - 1); }' < /dev/null`
	    [ -n "$VERBOSE" ] && echonoterm .
	    sleep 1
    	done
    	kill -0 $DPID 2> /dev/null && {
    	    echo '   Daemon is really being stubborn.  Sending TERM signal...'
	    kill -TERM $DPID 2> /dev/null
	    sleep 1
	}
    	kill -0 $DPID 2> /dev/null && {
    	    echo "   That didn't work.  Sending KILL signal..."
	    kill -KILL $DPID 2> /dev/null || {
	    	Bitch $dbname "Kill -KILL of ${WEB}Server failed"
		RETVAL=1
		continue
    	    }
    	}
    	cnt=5
    	while [ $cnt -gt 0 ]; do
    	    kill -0 $DPID 2> /dev/null || break
	    cnt=`goodawk -- 'BEGIN { print (ENVIRON["cnt"] - 1); }' < /dev/null`
	    [ -n "$VERBOSE" ] && echonoterm +
	    sleep 1
    	done
    	kill -0 $DPID 2> /dev/null && {
	    Bitch $dbname  \
    "Failed to stop ${WEB}Server within $MAXSTOPTIME.  You're on your own..."
	    RETVAL=1
	    continue
	}
	Bitch $dbname "${WEB}Server is shut down"
    	rm -f $PIDFILE
    done
    exit $RETVAL
    ;;
start)
    echo 'Starting all known hsqldb daemon(s)'
    [ -n "$RUNNINGS" ] && Bitch '' \
     "Start command given but daemon already running for this database(s):
    $RUNNINGS"
    JSYSLIB=
    case "$JVER" in
	# TODO:  Build a jar for Java 1.1.8, per Fred T.
    	1.4.*) HSQLDBLIB=$HSQLDB_HOME/lib/hsqldb_jre1_4_0.jar;;
    	1.2.*|*JDK_1.2.*|1.3.*) HSQLDBLIB=$HSQLDB_HOME/lib/hsqldb_jre1_3_1.jar;;
    	1.1.*) HSQLDBLIB=$HSQLDB_HOME/lib/hsqldb_jre1_1_8.jar;;
	*)
	    Failout ''  \
	    "Sorry, but Java version $JVER is not supported at this time.
If you seriously want support for it, email ${AUTHOR_EMAILADDR}."
	;;
    esac
    [ -r $HSQLDBLIB ] ||
    Failout '' "Assertion failed.  Library '$HSQLDBLIB' is not in place."

    export cnt
    for dbname in $NOTRUNNINGS; do
    	PIDFILE=$PIDDIR/hsqldb_${dbname}.pid
	WEB=
	[ -r $HSQLDB_HOME/data/$dbname/server.properties ] || WEB=Web
    	safecd $HSQLDB_HOME/data/$dbname 2> /dev/null || {
	    Bitch $dbname "Failed to cd to $HSQLDB_HOME/data/$dbname"
	    RETVAL=1
	    continue
	}
	REDIRFILE=$LOGDIR/hsqldb_${dbname}.log
	date '+%Y/%m/%d %H:%M:%S  Attempting startup' >> $REDIRFILE || {
	    Bitch $dbname  \
	     "Failed to log startup attempt to '$REDIRFILE'"
	    RETVAL=1
	    continue
	}
	if [ "$OWNER" = 'root' ]; then
	    [ -n "$VERBOSE" ] && echo \
'exec java -classpath $HSQLDBLIB$CPADDN org.hsqldb.${WEB}Server -database $dbname  \
    	     >> $REDIRFILE 2>&1" > /dev/null 2>&1 &'
    	    nohup sh -c 'echo $$'" > $PIDFILE;
exec java -classpath $HSQLDBLIB$CPADDN org.hsqldb.${WEB}Server -database $dbname  \
    	     >> $REDIRFILE 2>&1" > /dev/null 2>&1 &
	    [ $? = 0 ] || Failout $dbname "Failed to execute 'java'"
	else
	    # Unfortunately, Primary groups don't work right in Solaris
	    # (and I don't want to make it the primary and secondary group for
	    # the user or it will fail pwck checking).
	    #chown root:$GROUP $REDIRFILE
	    #chmod 664 $REDIRFILE
	    chown $OWNER:root $REDIRFILE
	    chmod 644 $REDIRFILE
	    > $PIDFILE
	    #chown root:$GROUP $PIDFILE
	    #chmod 664 $PIDFILE
	    chown $OWNER:root $PIDFILE
	    chmod 644 $PIDFILE
	    # Probably need to functionize the su command to make it portable.
	    [ -n "$VERBOSE" ] && echo \
	  'nohup su - "$OWNER" "$dbname" "$HSQLDBLIB" "$WEB" > /dev/null 2>&1 &'
	    if [ -n "$DEBUG" ]; then
	    	su - "$OWNER" "$dbname" "$HSQLDBLIB" "$WEB"
	    else
	    nohup su - "$OWNER" "$dbname" "$HSQLDBLIB" "$WEB" > /dev/null 2>&1 &
	    [ $? = 0 ] || Failout $dbname "Failed to su to '$OWNER'"
	    fi
	fi
    	# Just enough time for the sh to start up, then write the pidfile:
    	sleep 3
    	DPID="`cat $PIDFILE`"
    	cnt=$MAXSTARTTIME
    	while [ $cnt -gt 0 ]; do
    	    kill -0 $DPID 2> /dev/null || {
		RETVAL=1
		Bitch $dbname "Failed to start ${WEB}Server.  It died"
		continue 2
	    }
	    # Nawk can't do \< like grep/vi nor \b like perl :(
	    goodawk -- '
		BEGIN { retval = 1; }
	  	END { exit(retval); }
	  	/Attempting startup$/ { retval = 1; }
	  	/^HSQLDB .*server .* is running$/ { retval = 0; }
	    ' $REDIRFILE && {
    	    	Bitch $dbname "${WEB}Server is started with Java version $JVER"
	    	continue 2
	    }
	    cnt=`goodawk -- 'BEGIN { print (ENVIRON["cnt"] - 1); }' < /dev/null`
	    [ -n "$VERBOSE" ] && echonoterm .
	    sleep 1
    	done
	RETVAL=1
	Bitch $dbname  \
 "${WEB}Server is stuck.  Still not all the way up after $MAXSTARTTIME seconds"
    done
    exit $RETVAL
    ;;
restart)
    $INITSCRIPT stop && exec $INITSCRIPT start
    Failout ''  \
     "'INITSCRIPT stop' failed, or failed to run '$INITSCRIPT start'"
    ;;
*)  echo "SYNTAX:  $INITSCRIPT {start|stop|restart}" 1>&2; exit 1
    ;;
esac

Failout '' "Assertion failed in init script"
