/*
    $Id: special-q.sql,v 1.2 2004/06/17 02:50:25 unsaved Exp $

    See readme.txt in this directory for how to unit test SqlTool.

    Tests:  Special commands \dX.
    Right now it only tests \d*, \dt, \dv, and even these only partially.

    HARNESS_METADATA        BEGIN         
    arg                 --noAutoFile
    rejectStdoutRegex   (?s)\bNEW_\b.*\bMARK A\b
    rejectStdoutRegex   (?s)\bMARK B.*\bNEW_VIEW.*\bMARK C\b
    requireStdoutRegex  (?s)\bMARK B\b.*\bNEW_TBL\b.*\bMARK C
    requireStdoutRegex  (?s)\bMARK C\b.*\bNEW_VW\b
    rejectStdoutRegex   (?s)\bMARK C\b.*\bNEW_TBL\b
    arg                 mem 
    HARNESS_METADATA        END       
*/

\d*
\dt
\dv
\p MARK A

CREATE TABLE NEW_TBL (vc VARCHAR);
CREATE VIEW NEW_VW AS SELECT vc FROM NEW_TBL;

\p MARK B
\dt
\p MARK C
\dv
