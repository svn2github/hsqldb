/*
    $Id$

    See readme.txt in this directory for how to unit test SqlTool.

    Tests:  Special command \s with no arg.

    HARNESS_METADATA        BEGIN         
    requireStdoutRegex      PRE-QUIT
    rejectStdoutRegex       POST-QUIT
    arg mem 
    HARNESS_METADATA        END       
*/

\p PRE-QUIT

\q

\p POST-QUIT
