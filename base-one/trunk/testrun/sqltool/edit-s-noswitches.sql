/*
    $Id: hist-recall-19.sql,v 1.2 2004/06/16 19:37:10 unsaved Exp $

    See readme.txt in this directory for how to unit test SqlTool.

    Tests:  Command-line editing with switch command.  No subst. switches.

    HARNESS_METADATA        BEGIN         
    requireStdoutRegex  (?m)\sMARK A\n.*Current Buffer:\nalphREPLbeta gamma delta$
    requireStdoutRegex  (?m)\sMARK B\n.*Current Buffer:\nalphREPLbeta g delta$
    arg mem 
    HARNESS_METADATA        END       
*/

/* The blank line after each command moves the command to history without
   executing it. */

alpha beta gamma delta

\p MARK A
:s/a /REPL/
\p MARK B
:s/amma//
