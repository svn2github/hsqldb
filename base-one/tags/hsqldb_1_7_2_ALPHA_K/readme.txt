Readme File

HSQLDB 1.7.2 ALPHA_K

2003.01.10

various patches and fixes

-OUTER with multiple column joins is now supported while OR is
disallowed in the join expression. It is now possible to specify
a range or equality condition on the OUTER columns returned.

-submitted patch for exclusion of NULL values from results of
range queries has been applied. e.g. 
WHERE columnvalue < 3
will exclude from the result all rows with NULL in columnvalue.

-a number of small enhancements and bug fixes.

further enhancements to logging. 
-The *.script file now contains only the DDL and data that is 
written at CHECKPOINT or SHUTDOWN.
The statements logged while the engine is running are stored
in a *.log file.
-The format of the *.script file can now be one of TEXT,
BINARY or COMPRESSED. The SET LOGTYPE {0|1|3} will reset the 
format. A checkpoint is performed at this point if the format
is different from the existing one.
The COMPRESSED format has the side benefit of hiding the DDL
and the admin password.
-The behaviour of SET WRITE_DELAY has changed with the
introduction of the sync() method to force the log to be
written out completely to disk at given intervals.
SET WRITE_DELAY {TRUE | FALSE} is interpreted as synch every
60 seconds or 1 second. SET WRITE_DELAY <n> where n is an integer
is interpreted as synch every n seconds. The current default is
60 seconds which seems to provide the right balance. The
performance impact of SET WRITE_DELAY 1 is probably about 15% over that
of SET WRITE_DELAY 300.

-The recovery from crash has been modified so that any line in the log
that is not properly written (and causes an error) ends the redo process.
A message is reported to the user, instead of stopping engine operation.

HSQLDB 1.7.2 ALPHA_J

2002.12.24
CVS hsqldb-dev module tag: HSQLDB_1_7_2_ALPHA_J

-More work on text tables. See hsqlTextTables.html.
-Some refactoring of Table.java and Parser.java


HSQLDB 1.7.2 ALPHA_I

2002.12.16
CVS hsqldb-dev module tag: HSQLDB_1_7_2_ALPHA_I

More work on core classes to consolidate previous changes
-correction of reported bug with text tables and primary keys
-reducing data file writes esp. with big rows
-preventing Cache related error in ON DELETE CASCADE 


HSQLDB 1.7.2 ALPHA_H

2002.12.11
CVS hsqldb-dev module tag: HSQLDB_1_7_2_ALPHA_H


Reduction in JDK / JRE dependencies (see readmebuild.txt)
Extensive refactoring of some of the larger classes.
More changes to core classes Node, Row, Index, Cache, Log ...
Some minor bug fixes and enhancements.
Enhancements to text table support, including binary columns.
Bug fixes to User.java and UserManager.java.
Support for ON UPDATE / DELETE SET NULL / SET DEFAULT
SSL support for Server mode operation.

2002.10.30

HSQLDB 1.7.2 ALPHA_G

CVS hsqldb-dev module tag: HEAD data: 2002.10.30

New features include:

Tony Lai's enhancements to aggratates and support for HAVING.

Tony's new UnifiedTable class (provisional name) which he kindly wrote
at my request and provides sorted arrays with minimal storage
requirements.

My refactoring work on DatabaseRowInput/Output subclasses and Cache.java.

This is still ongoing and has several features aimed at a more robust 
database engine, including:
- reduction in object creation;
- possibility of resizing the cache while the engine is running;
- possibility of exporting and backing up the DB files while the engine is
  running.

I have implemented the new feature, CHECKPOINT DEFRAG to defragment a
*.data file without shutting down the engine, in two different versions,
taking advantage of the above improvements.

I have implemented binary logging of MEMORY table inserts in the *.script 
file, resulting in much smaller startup and shutdown times with large 
memory tables. Use SET LOGTYPE {0|1} to set the log type to text (0) or
binary (1). This performs a checkpoint if the type needs changing.

Also further performance optimisations resulting in faster CACHED table
operations have been made.

Sebastian has implemented ON UPDATE CASCADE support for foreign keys.

Fred Toussi (fredt@users)
