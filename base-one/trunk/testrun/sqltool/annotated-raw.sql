/*
    See readme.txt in this directory for how to unit test SqlTool.

    This is an annotated example.  Don't use this as a template.  See
    the readme.txt file about that.

    This is an annotated example on how to test SqlTool run in raw mode.
    By raw mode, I mean running SqlTool with a hyphen as the only SQL file
    argument, like
        java org.hsqldb.util.SqlTool mem -
    This causes SqlTool to read stdin, but non-interactively (e.g. 
    command-line editing won't work and there is no login banner, etc.).

    N.b. that there are no annotations between the "HARNESS_METADATA BEGIN"
    line and the "HARNESS_METADATA END" lines below.  This is because only
    harness metadata name/value pairs are permitted between those lines.

    Harness metadata settings:
        arg:                An argument for SqlTool.
                            Unless you are testing bad command-line args, you
                            must give at least a urlid as an arg.
                            The only SqlTool arg that you do not give is, if
                            you set inputAsFile to true (see below), you do not
                            use 'arg' to specify this file name.
        jvmarg:             An argument to come between "java" and 
                            "org.hsqldb.util.SqlTool"
        requireStdoutRegex: Regular expression to require from stdout of SqlTool
        rejectStdoutRegex:  Regular expression to reject from stdout of SqlTool
        requireErroutRegex: Regular expression to require from errout of SqlTool
        rejectErroutRegex:  Regular expression to reject from errout of SqlTool
        exitValue:          Required exit value from the SqlTool run.
        inputAsFile:        "true" or "false" (defaults to "false").
                            If "false", this file you are reading will be
                            passed to SqlTool via stdin.
                            If "true", the filename of the file you are reading
                            will be given to SqlTool as the last command-line
                            argument (do NOT give it with 'arg' or SqlTool
                            will get that argument twice!).
    All settings are optional.  For all but the last 2, you can give as many
    as you want.  You could, for example give 3 args, 2 jvmargs, 20
    requireStdoutRegexes.  The reason for the exception where a filename 
    'arg' is automatically added if inputAsFile is true, is so you don't
    have to maintain hard-coded file names in SQL files, and you don't need
    to be concerned about operating system case sensitivity or case-munging
    problems.

    You can add args for other filenames, to test "SqlTool... file1 file2...",
    just be aware that this current file will be appended in last position
    if inputAsFile is true.


    This specific example emulates the command:
        SqlTool --sql '\p Print message from --sql argument.' -
    Then pipes this entire file into stdin of SqlTool.

    HARNESS_METADATA        BEGIN         
    requireStdoutRegex  SQL file
    arg --noinput
    arg --sql
    arg \p Print message from --sql argument.
    arg mem */
    \p Print message from normal SQL file area.
    /*
    arg -
    HARNESS_METADATA        END       
*/
\p bye
