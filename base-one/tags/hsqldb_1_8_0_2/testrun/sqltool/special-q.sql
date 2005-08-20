/*
    $Id: special-q.sql,v 1.2 2004/06/17 02:50:25 unsaved Exp $

    See readme.txt in this directory for how to unit test SqlTool.

    Tests:  Special command \q with no arg.

    HARNESS_METADATA        BEGIN         
    arg                 --noAutoFile
    requireStdoutRegex  PRE-QUIT
    rejectStdoutRegex   POST-QUIT
    arg                 mem 
    HARNESS_METADATA        END       
*/

\p PRE-QUIT

\q

\p POST-QUIT
