Readme File

2002.10.18

HSQLDB 1.7.2 ALPHA_F

CVS hsqldb-dev module tag: hsqldb_1_7_2_ALPHA_F

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

Fred Toussi (fredt@users)
