/*
    $Id: special-q.sql,v 1.2 2004/06/17 02:50:25 unsaved Exp $

    See readme.txt in this directory for how to unit test SqlTool.

    Tests:  Blank line within a SQL statement.

    HARNESS_METADATA        BEGIN         
    arg                 --noAutoFile
    arg                 --abortOnErr
    arg                 mem 
    requireStdoutRegex  moved into buffer
    rejectStdoutRegex   row updated
    exitValue           1
    HARNESS_METADATA        END       
*/

CREATE TABLE t

(i int);

insert into t values(4);
