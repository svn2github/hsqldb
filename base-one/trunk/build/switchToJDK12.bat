cd ..\
md classes
del /s classes\*.class
cd build
cd ..\src\org\hsqldb\util
javac -d ..\..\..\..\classes CodeSwitcher.java
cd ..\..\..\..\build
java -classpath "%classpath%;../classes" org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbcStubs.java +JAVA2 -JDBC3
java -classpath "%classpath%;../classes" org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbcCallableStatement.java +JAVA2 -JDBC3
java -classpath "%classpath%;../classes" org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbcConnection.java +JAVA2 -JDBC3
java -classpath "%classpath%;../classes" org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbcDatabaseMetaData.java +JAVA2 -JDBC3
java -classpath "%classpath%;../classes" org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbcPreparedStatement.java +JAVA2 -JDBC3
java -classpath "%classpath%;../classes" org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbcResultSet.java +JAVA2 -JDBC3
java -classpath "%classpath%;../classes" org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbcStatement.java +JAVA2 -JDBC3
java -classpath "%classpath%;../classes" org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/HsqlDateTime.java +JAVA2 -JDBC3
