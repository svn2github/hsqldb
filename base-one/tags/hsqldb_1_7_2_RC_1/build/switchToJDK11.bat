@echo HSQLDB source switch file for jdk 1.1.x
@echo *** we recommend the use of the ANT build.xml instead of this method
@echo for all jdk's include the path to jdk1.x.x\bin in your system path statement
@echo for jdk1.1.x also set the system classpath to include the path to
@echo    jdk1.1.x\lib\classes.zip on your system
@echo example: set classpath=c:\jdk1.1.8\lib\classes.zip
cd ..\
md classes
del /s classes\*.class
cd build
cd ..\src\org\hsqldb\util
javac -d ..\..\..\..\classes CodeSwitcher.java
cd ..\..\..\..\build
java -classpath "%classpath%;../classes org.hsqldb.util.CodeSwitcher" ../src/org/hsqldb/jdbcStubs.java -JAVA2 -JDBC3
java -classpath "%classpath%;../classes org.hsqldb.util.CodeSwitcher" ../src/org/hsqldb/jdbcConnection.java +JAVA2 -JDBC3
java -classpath "%classpath%;../classes org.hsqldb.util.CodeSwitcher" ../src/org/hsqldb/jdbcDatabaseMetaData.java +JAVA2 -JDBC3
java -classpath "%classpath%;../classes org.hsqldb.util.CodeSwitcher" ../src/org/hsqldb/jdbcPreparedStatement.java +JAVA2 -JDBC3
java -classpath "%classpath%;../classes org.hsqldb.util.CodeSwitcher" ../src/org/hsqldb/jdbcResultSet.java +JAVA2 -JDBC3
java -classpath "%classpath%;../classes org.hsqldb.util.CodeSwitcher" ../src/org/hsqldb/jdbcStatement.java -JAVA2 -JDBC3
java -classpath "%classpath%;../classes org.hsqldb.util.CodeSwitcher" ../src/org/hsqldb/HsqlDateTime.java +JAVA2 -JDBC3