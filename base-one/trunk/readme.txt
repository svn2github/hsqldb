Readme File

2002.10.30

HSQLDB 1.7.2 ALPHA_G

CVS hsqldb-dev module tag: HEAD data: 2002.10.30

Strictly for developers. This is alpha software featuring developments
leading to version 1.7.2.

The codebase is based on 1.7.1 Release code plus new experimental code.
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
