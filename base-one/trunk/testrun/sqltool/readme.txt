$Id: readme.txt,v 1.1 2004/06/16 17:00:34 unsaved Exp $

SqlTool UNIT TESTING


To do anything at all with SqlTool unit testing, you need

    The HSQLDB test classes, built with Java 1.4.

        This is most commonly accomplished by running "build jartest" with
        a Java 1.4 SDK (as documented in the Build chapter of the HSQLB
        User Guide).  (If you are actively developing, you could alternatively
        run "build test" and work with the /classes branch instead of a jar).

    Set your search path so that a Java 1.4 "java" executable gets run.

        The tests will not work if you give a path to "java" to run
        the test programs, because the test program itself invokes java
        using just "java".  This won't work:

            /usr/java/j2sdk1.4.2_02/bin/java org.hsqldb.test.SqlTool...

    Set (and export if your shell supports that) the shell environmental
    variable "CLASSPATH" to include hsqsldbtest.jar (or the HSQLDB 
    "classes" subdirectory).  The tests will not work if you supply a
    java classpath switch, for the exact same reason described for the
    previous item.

    Run the tests from this directory.  This is just so that simple
    relative paths can be used for the various files that live in this
    directory.


To run the JUnit test suite for SqlTool.

    Graphical.

        java org.hsqldb.util.TestSqlTool -gui

    Non-graphical

        java org.hsqldb.util.TestSqlTool


To run tests of specific SQL files against SqlTool without JUnit.

        java [-v] org.hsqldb.util.SqlToolHarness file1.sql [file2.sql...]

    The -v switch is to debug your test.
    It will print out, among other things, all of the harness metadata values.


To make a new SQL test file.

    Look at the appropriate annotated example file, annotated-*.sql.
    This explains how to code the metadata to describe exactly how 
    to run SqlTool for the test.  *Do not use the annotated examples
    as a template for your own SQL test files!* (see next item about
    that).

    Find a *.sql file in this directory closest to what you intend
    to do.  Use that file as a template by copying it to your new
    file name and editing it.  We don't use the annotated examples
    as templates because the annotations therein are purposefully
    verbose.  We don't want the same information duplicated in a 
    zillion files (what if we need to update an explanation!), plus,
    I like the real test files to be nice and concise.
