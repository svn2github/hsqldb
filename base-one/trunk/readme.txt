Readme File


leading to HSQLDB 1.7.2 ALPHA_N

2003.08.17

FURTHER IMPROVEMENTS IN PREPARED STATEMENT EXECUTION SPEED

SUPPORT FOR JDBC BATCH EXECUTION AND MULTIPLE-RESULT SET

SUPPORT FOR MULTIPLE ROWS WITH NULL FIELDS UNDER UNIQUE CONSTRAINTS

IMPROVEMENTS TO TestSelf

IMPROVEMENTS TO ON UPDATE CASCADE / SET DEFAULT / SET NULL

2003.08.11

REWRITE OF LEFT OUTER JOIN SUPPORT

- Complete rewrite of join processing abolishes the requirement for 
an index on any joined columns. All expressions with AND are supported
in the join condition (ON ....)

IMPROVEMENTS TO ON DELETE CASCADE / SET DEFAULT / SET NULL

- Self referencing foreign keys and multiple foreign keys now support
ON DELETE CASCADE.

2003.08.02

NIO ACCESS FOR .data FILES

New nio access layer for .data files speeds up most CACHED TABLE related
operations very significantly. 90% speedups in TestCacheSize tests
have been observed. The program must be compiled with JDK 1.4 and run
in a 1.4 JRE to use the new access mode.

IMPROVEMENTS TO UPDATE AND INSERT

Certain types of UPDATES and INSERTS that previously failed due to
blanket application of UNIQUE constraints now work.

Examples include UPDATE ... SET col = col + 1 where col is an identity
column or INSERT a self referencing row under FOREIGN key constraints.

2003.07.30

IMPORTANT FIX IN SELECT QUERY PROCESSING

Problems with OUTER and INNER joins returning incorrect results 
have been reported over the last couple of years. A new fix is 
intended to ensure correct results in all cases.

- When two tables are joined, rows resulting from joining null
values in the joined columns are no longer returned.

- Use of OUTER requires the existence of an index on a joined OUTER column
- There are still limitations on the conditions used in OUTER joins.

AGGREGATES

- enhancements to aggregate functions: aggregates of different numeric
types are now supported

SUM returns a BIGINT for TINYINT, SMALLINT and INTEGER columns. It
returns a DECIMAL for BIGINT columns (scale 0) and DECIMAL columns 
scale equals to that of the column).

AVG returns the same type as the column or the expression in its 
argument.

- aggregates with GROUP BY do not return any rows if table is empty

OLDER ENHANCEMENTS NOT PREVIOUSLY REPORTED

- IDENTITY columns can now be of BIGINT type:

CREATE TABLE <name> (<column> BIGINT IDENTITY, ...)

- With contributed patch, TEXT TABLES encoding of the source file can
now be specified. UTF-8 and other encodings can be used.

- Two new options for databases: files_read_only and files_in_jar
were added based on submitted patches.

FILE READ-ONLY

If the property files_read_only=true is set in the database 
.properties file, no attempt is made to write the changes to data to
file. Default, memory tables can be read/write but TEXT and CACHED
tables are treated as read-only.

FILES IN JAR

This option allows database files to be distributed
in the application jar. We have changed the original contribution so
that a special URL is used for this mode in the form of:

jdbc:hsqldb:res:<path in jar>

The URL type 'res' determines that the path that follows is a path
into the JAR.

The database can be readonly or files_read_only, depending on the
value set in .properties file.

2003.07.09

SCRIPT FORMAT

- change to command previously named SET LOGTYPE (discussed below for
earlier alphas) new form is:
SET SCRIPTFORMAT {TEXT | BINARY | COMPRESSED }

The new binary and compressed formats are not compatible with previous
ones, so you should change any old 1.7.2 ALPHA_? database to text
mode with SET LOGTYPE 0 before openning with the new version.

'OTHER' DATA TYPE

- change to handling of OTHER columns. It is no longer required that
the classes for objects stored in OTHER columns to be available on
the path of an HSQLDB engine running as a server. Classes must be
available on the JDBC client's path.

OBJECT POOLING

- the new Object pool has been incorporated. This reduces memory
usage to varying degrees depending on the contents of database
tables and speeds up the database in most cases. Currently the
size of the pool is hard-coded but it will be user adjustable in the
release version.

CONNECTION PROPERTY

- a new property, ifexists={true|false} can be specified for connections.
It has an effect only on connections to in-process databases. The default
is false and corresponds to current behaviour. 

If set true, the connection is opened only if
the database files have already been created -- otherwise no new database
is created and the connection attemp will fail. Example:

jdbc:hsqldb:hsql:mydb;ifexists=true

This property is intended to reduce problems resulting from wrong URL's 
which get translated to unintended new database files. It is recommended
to use this property for troubleshooting.

2003.07.04

PREPARED STATEMENTS

- support for real PreparedStatements - major speedup. Current limitations
include lack of support for parameters in IN() predicates of queries.

TRANSACTIONS VIA WEB SERVER

- uniform support for transactions via HSQL and HTTP (WebServer and Servlet)
protocols

MULTIPLE IN-MEMORY AND SERVERS DATABASES

- support for multiple memory-only databases within the same JVM

- support for simultaneous multiple servers, multiple internal
connections and multiple databases within the same JVM


NEW CONVENTIONS FOR URL'S AND .properties FILES

Each HSQLDB server or webserver can now serve up to 10 different databases.

The server.properties and webserver.properties method for defining the
databases has changed. The following properties should be used:

server.database.0   path_of_the_first_database
server.dbname.0 alias_for_the_first_database

Up to 10 databases can be defined but they must start from 0 

The same applies to command line arguments for Server and WebServer.

The urls for connecting to servers should have the alias of the database
at the end.

For example, to connect to the HSQL protocol server on the localhost use:

jdbc:hsqldb:hsql://localhost/alias_for_the_database

where alias_for_the_database is the same string as defined in
server.properties as server.dbnam.n

The default for server.dbname.0 is "" (empty string) so that
old URL types continue to work.

Multiple memory-only database are supported by the use of:

jdbc:hsqldb:mem:alias_for_the_first_database
jdbc:hsqldb:mem:alias_for_the_second_database

Examples: jdbc:hsqldb:mem:db1 jdbc:hsqldb:mem:mydb

The conneciton type, 'file', can be used for file database
connections. example below:

jdbc:hsqldb:hsql:file:mydb;ifexists=true


The URL for connecting to a Servlet HTTP server must have a 
forward-slash at the end. Servlet serves only one database.

jdbc:hsqldb:hsql://localhost:8080/servlet/HsqlServlet/



2003.03.10

DATABASE METADATA

-system table support and java.sql.DatabaseMetadate results have been
overhauled by Campbell.

STRICT FOREIGN KEYS

-strict treatment of foreign key index requirements is now enforced.
A foreign key declaration _requires_ a unique constraint or index to exist
on the columns of the referenced table. This applies both to old and
new databases. Duplicate foreign keys (with exactly the same column sets)
are now disallowed.

TEXT TABLES

-further improvements to TEXT table support. Smaller cache sizes are
now the default and the default properties can be specified in the 
*.properties file.



HSQLDB 1.7.2 ALPHA_M

2003.01.23

-fixed reported bugs in SHUTDOWN COMPACT
-fixed reported bugs in GRANT statements on system tables
-fixed bug that prevented UPDATE in some circumstances

some enhancements that appeared in previous versions but not reported:

-enhanced handling of DATE objects - comparability and normalisation
-support for CLOB methods in ResultSet
-fixed bug in afterLast() followed by previous() in ResultSet


HSQLDB 1.7.2 ALPHA_L

2003.01.16

various patches and fixes

-fixes new issues reported with ALPHA_K
-fixes old issues related to uncommitted transactions in
abnormal shutdown
-fixes old issues with SAVEPOINT names
-enhanced TEXT table handling and reporting of errors in
CSV (source) files


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
