/*
    $Id$

    See readme.txt in this directory for how to unit test SqlTool.

    Tests:  Special command \s with arg.

    HARNESS_METADATA        BEGIN         
    requireStdoutRegex  PRE-QUIT
    rejectStdoutRegex   POST-QUIT
    arg                 mem 
    exitValue           2
    HARNESS_METADATA        END       
*/

\p PRE-QUIT

\q Abort message here

\p POST-QUIT
