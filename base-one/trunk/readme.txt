Readme File

HSQLDB 1.7.2 ALPHA_G

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
