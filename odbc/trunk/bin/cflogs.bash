#!/bin/bash -p
PROGNAME="${0##*/}"

# $Id$

# Textually or graphically displays the difference between two (specified)
# driver log files, excluding most unique identifiers and non-textual
# characters.
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

DIFFPROG=diff
while [ $# -gt 0 ]; do
    case "$1" in
        -*)
            case "$1" in *g*) DIFFPROG=gvimdiff;; esac
            shift;;
        *) break;;
    esac
done

[ $# -eq 2 ] || {
    echo "SYNTAX:  $PROGNAME path/one.log path/two.log
Defaults to now." 1>&2
    exit 3:;
}
FILE1="$1"; shift
FILE2="$1"; shift

Failout() {
    echo "Aborting $PROGNAME:  $*" 1>&2
    exit 1
}

[ -n "$TMPDIR" ] || TMPDIR=/tmp

type perl > /dev/null || Failout "'perl' must be in your search path"
type $DIFFPROG > /dev/null || Failout "'$DIFFPROG' must be in your search path"

perl -pwe '
    s/[^\n -~]//g;
    s:^\[\d+\]::;
    s:\[\d+\]:[_DIGITS_]:g;
    s:\b0x[0-9a-f]{5,}:0x_HEXDIGITS_:g;
    s:\b[0-9a-f]{5,}:_HEXDIGITS_:g;
' "$FILE1" > "$TMPDIR/left.txt" ||
Failout "Failed to preprocess '$FILE1'"
perl -pwe '
    s/[^\n -~]//g;
    s:^\[\d+\]::;
    s:\[\d+\]:[_DIGITS_]:g;
    s:\b0x[0-9a-f]{5,}:0x_HEXDIGITS_:g;
    s:\b[0-9a-f]{5,}:_HEXDIGITS_:g;
' "$FILE2" > "$TMPDIR/right.txt" ||
Failout "Failed to preprocess '$FILE2'"

$DIFFPROG $TMPDIR/left.txt $TMPDIR/right.txt
