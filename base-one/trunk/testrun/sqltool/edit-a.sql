/*
    $Id: edit-a.sql,v 1.1 2004/06/16 20:39:24 unsaved Exp $

    See readme.txt in this directory for how to unit test SqlTool.

    Tests:  Command-line editing with append command.

    HARNESS_METADATA        BEGIN         
    arg                 --noAutoFile
    requireStdoutRegex  (?m)\sMARK A\n.*Current Buffer:\nalpha beta$
    requireStdoutRegex  (?m)\sMARK B\n.*Current Buffer:\nalpha beta\n gamma$
    arg                 mem 
    HARNESS_METADATA        END       
*/

/* The blank line after each command moves the command to history without
   executing it. */

alpha

/* Should change nothing because case doesn't match */
:a beta

\p MARK A
:l

:a
 gamma

\p MARK B
:l
