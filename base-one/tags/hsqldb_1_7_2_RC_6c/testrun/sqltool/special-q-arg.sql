/*
    $Id: special-q-arg.sql,v 1.2 2004/06/17 02:50:25 unsaved Exp $

    See readme.txt in this directory for how to unit test SqlTool.

    Tests:  Special command \s with arg.

    HARNESS_METADATA        BEGIN         
    arg                 --noAutoFile
    requireStdoutRegex  PRE-QUIT
    requireErroutRegex  Abort message here
    rejectStdoutRegex   POST-QUIT
    arg                 mem 
    exitValue           2
    HARNESS_METADATA        END       
*/

\p PRE-QUIT

\q Abort message here

\p POST-QUIT
