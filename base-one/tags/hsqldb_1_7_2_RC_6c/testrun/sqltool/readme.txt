$Id: readme.txt,v 1.8 2004/06/16 18:50:59 unsaved Exp $

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

    IF you are running JUnit tests (iėe., running a JUnit test manually
    or invoking TestSqlTool), then you also need to put the junit.jar 
    file into your classpath.

    Set up a urlid named "mem" in your sqltool.rc file, as documented in
    the SqlTool chapter of the HSQLDB User Guide.  If you started with
    the sample sqltool.rc file then you are all set (because that defines
    a "mem" urlid).

    I expect at some point I or somebody else will make unit tests which
    really need meaty data in the database.  In that case, it may make
    sense to set up another urlid and supply a (non-test) SQL file in
    this directory which will populate that database.  I don't have time
    right now to figure out how to document these dependencies,so just
    set up the "mem" urlid for now.

    Run the tests from this directory.  This is just so that simple
    relative paths can be used for the various files that live in this
    directory.


To run the JUnit test suite for SqlTool.

    Graphical.

        java org.hsqldb.test.TestSqlTool --gui

    Non-graphical

        java org.hsqldb.test.TestSqlTool


To run tests of specific SQL files against SqlTool without JUnit.

        java [-v] org.hsqldb.test.SqlToolHarness file1.sql [file2.sql...]

    The -v switch will tell you exactly which test failed, and gives 
    information to help debug your test file itself (e.g. it echos all of
    the harness metadata values and stdout and stderr in their entirety).


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

    In general, try to use a Memory-only urlid named "mem".  This is
    just simpler because the url is easy to set up, you don't have to
    worry about the state of the database when you connect, and you
    don't need to worry about cleanup.


Regexes

    You can use regular expression values as documented in the 
    JDK 1.4 API Spec for java.util.regex.Pattern.

    This is a very powerful regular expression language very close
    to Perl's.  There are a few limitations (such as look-ahead and
    look-behind strings must be of fixed size), but, like I said,
    they are still extremely powerful.

    Example:
    
        requireStdoutRegex  (?im)\w+\s+something bad\s*$

    This would match lines anywhere in stdout of the SqlTool run
    which end with a word + whitespace + "something bad" (case
    insensitive) + optional whitespace.
