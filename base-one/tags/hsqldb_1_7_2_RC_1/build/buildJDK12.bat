@echo HSQLDB build file for jdk 1.2.x and 1.3.x
@echo *** we recommend the use of the ANT build.xml instead of this method
@echo for all jdk's include the path to jdk1.x.x\bin in your system path statement
cd ..\
md classes
del /s classes\*.class
cd src
mkdir ..\temp
copy org\hsqldb\jdbcDataSource*.java ..\temp\
copy org\hsqldb\jdbcSavepoint.java ..\temp\
copy org\hsqldb\jdbcParameterMetaData.java ..\temp\
copy org\hsqldb\NIOLockFile.java ..\temp\
copy org\hsqldb\NIOScaledRAFile.java ..\temp\
del org\hsqldb\jdbcDataSource*.java
del org\hsqldb\jdbcSavepoint.java
del org\hsqldb\jdbcParameterMetaData.java
del org\hsqldb\NIOLockFile.java
del org\hsqldb\NIOScaledRAFile.java
javac -O -nowarn -d ../classes -classpath "%classpath%;../classes;../lib/servlet.jar;." ./*.java org/hsqldb/*.java org/hsqldb/lib/*.java org/hsqldb/util/*.java
copy ..\temp\jdbcDataSource*.java org\hsqldb
copy ..\temp\jdbcSavepoint.java org\hsqldb
copy ..\temp\jdbcParameterMetaData.java org\hsqldb
copy ..\temp\NIOLockFile.java org\hsqldb
copy ..\temp\NIOScaledRAFile.java org\hsqldb
del ..\temp\jdbcDataSource*.java
del ..\temp\jdbcSavepoint.java
del ..\temp\jdbcParameterMetaData.java
del ..\temp\NIOLockFile.java
del ..\temp\NIOScaledRAFile.java
rmdir ..\temp
cd ..\classes
copy ..\src\org\hsqldb\util\hsqldb.gif org\hsqldb\util
jar -cf ../lib/hsqldb.jar *.class org/hsqldb/*.class org/hsqldb/lib/*.class org/hsqldb/util/*.class  org/hsqldb/util/*.gif
cd ..\build
pause
