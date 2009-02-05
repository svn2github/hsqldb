#!/bin/bash -p
PROGNAME="${0##*/}"

# $Id$

# Lists changes to psqlodbc baseline between the last confirmed synchronization
# point and now (or the specified time).
#
#  Copyright (C) 2009 Blaine Simpson and The HSQL Development Group
#
#  This library is free software; you can redistribute it and/or
#  modify it under the terms of the GNU Library General Public
#  License as published by the Free Software Foundation; either
#  version 2 of the License, or (at your option) any later version.
#
#  This library is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#  Library General Public License for more details.
#
#  You should have received a copy of the GNU Library General Public
#  License along with this library; if not, write to the
#  Free Foundation, Inc., 51 Franklin Street, Fifth Floor,
#  Boston, MA  02110-1301  USA

set +u
shopt -s xpg_echo

# The %% is for when this script is renamed with an extension like .sh or .bash.
[ -n "$TMPDIR" ] || TMPDIR=tmp
TMPFILE="$TMPDIR/${PROGNAME%.*}-$$.files"

while [ $# -gt 0 ]; do
    case "$1" in
        -*)
            case "$1" in *v*) VERBOSE=1;; esac
            case "$1" in *n*) NORUN=1;; esac
            shift;;
        *) break;;
    esac
done

case $# in
    0) ENDSPEC=-rHEAD;;
    1) ENDSPEC="-D$1";;
    *)
        echo "SYNTAX:  $PROGNAME ['2009/02/01 UTC']
Defaults to now." 1>&2
        exit 3:;
esac

Failout() {
    echo "Aborting $PROGNAME:  $*" 1>&2
    exit 1
}

export CVSROOT=:pserver:anonymous@cvs.pgfoundry.org:/cvsroot/psqlodbc
SYNCH_FILE=config/psqlodbc_syncpoint.txt

[ -f "$SYNCH_FILE" ] ||
Failout 'Synchronization point file is missing:  $SYNCH_FILE'
START_POINT=$(< "$SYNCH_FILE")

case "$START_POINT" in
    '') Failout "'$SYNCH_FILE' file empty?";;
    *UTC);;
    *) Failout "'$SYNCH_FILE' value is not in UTC.  Fix it!";;
esac

[ -n "$VERBOSE" ] &&
exec cvs -q -f rdiff -kk -D "$START_POINT" "$ENDSPEC" psqlodbc
# TODO:  Probably need to generate the file diff list first (below), then
# generate the non -s of the differing files that pass through our filter.  

cvs -q -f rdiff -kk -s -D "$START_POINT" "$ENDSPEC" psqlodbc > "$TMPFILE" ||
Failout "CVS command with '$CVSROOT' failed"
cat "$TMPFILE"
# TODO:  Filter out files that we are not interested in
# TODO:  For special files, if the files differ, then fetch the entire files,
#        filter out parts we want to exclude, and compare the remainders.

rm "$TMPFILE"
exit 0
