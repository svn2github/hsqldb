/*
    $Id: special-q.sql,v 1.2 2004/06/17 02:50:25 unsaved Exp $

    See readme.txt in this directory for how to unit test SqlTool.

    Tests:  --noinput AND --sql args

    HARNESS_METADATA        BEGIN         
    arg                 --noAutoFile
    arg                 --noinput
    arg                 --sql
    arg                 \p See this
    requireStdoutRegex   See this
    rejectStdoutRegex   invisible
    arg                 mem 
    HARNESS_METADATA        END       
*/

\p invisible
