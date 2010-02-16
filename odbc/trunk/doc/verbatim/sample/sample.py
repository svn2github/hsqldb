#!/usr/bin/python

# $Id$

# Sample Python script accessing HyperSQL through the Python pyodbc module.

# This test HyperSQL client uses the ODBC DSN "tstdsn-a" to connect up to a
# HyperSQL server.  Just configure your own DSN to use the HyperSQL ODBC
# driver, specifying the HyperSQL server host name, database name, user,
# password, etc.

# N.b. there is some dependency or bug which requires pyodbc to use the
# ANSI variant of the HyperSQL ODBC Driver.  Using the normal Unicode
# variant will generate the following error message when you try to connect:
#    pyodbc.Error: ('0', '[0] [unixODBC]c (202) (SQLDriverConnectW)')

# Author:  Blaine Simpson  (blaine dot simpson at admc dot com)

import pyodbc

# Get a connection handle.
# In addition to the DSN name, you can override or supply additional DSN
# settings, such as "Uid" and "Pwd"; or define the DSN from scratch, starting
# with Driver.  These settings are delimited with "; ".  See pyodbc docs.
conn = pyodbc.connect("DSN=tstdsn-a")
try:
    conn.autocommit = 0

    cursor = conn.cursor();

    cursor.execute("DROP TABLE tsttbl IF EXISTS");

    cursor.execute(
        "CREATE TABLE tsttbl(\n"
        + "    id BIGINT generated BY DEFAULT AS IDENTITY,\n"
        + "    vc VARCHAR(20),\n"
        + "    entrytime TIMESTAMP DEFAULT current_timestamp NOT NULL\n"
        + ")");

    # First a simple/non-parameterized Insertion
    retval = cursor.execute("INSERT INTO tsttbl (id, vc) values (1, 'one')");
    if retval != 1:
        raise Exception(('1st insertion inserted ' + repr(retval)
            + ' rows instead of 1'))
    # Now parameterized.  Unfortunately, the Python DB API and pyodbc API do
    # not allow re-use of a parsed statement.  Cursor must be reparsed for
    # each usage.
    retval = cursor.execute("INSERT INTO tsttbl (id, vc) values (?, ?)",
            2, 'two');
    if retval != 1:
        raise Exception(('2nd insertion inserted ' + repr(retval)
            + ' rows instead of 2'))
    retval = cursor.execute("INSERT INTO tsttbl (id, vc) values (?, ?)",
            3, 'three');
    if retval != 1:
        raise Exception(('3rd insertion inserted ' + repr(retval)
            + ' rows instead of 3'))
    retval = cursor.execute("INSERT INTO tsttbl (id, vc) values (?, ?)",
            4, 'four');
    if retval != 1:
        raise Exception(('4th insertion inserted ' + repr(retval)
            + ' rows instead of 4'))
    retval = cursor.execute("INSERT INTO tsttbl (id, vc) values (?, ?)",
            5, 'five');
    if retval != 1:
        raise Exception(('5th insertion inserted ' + repr(retval)
            + ' rows instead of 5'))
    conn.commit();

    # Non-parameterized query
    for row in cursor.execute(
            "SELECT * FROM tsttbl WHERE id < 3"):
        print row

    # Non-parameterized query.  As noted above, can't re-use parsed cursor.
    for row in cursor.execute(
            "SELECT * FROM tsttbl WHERE id > ?", 3):
        # For variety, we format the files ourselves this time
        print repr(row.ID) + '|' + row.VC + '|' + repr(row.ENTRYTIME)

except Exception as e:
    conn.rollback();
    raise e

finally:
    conn.close();
