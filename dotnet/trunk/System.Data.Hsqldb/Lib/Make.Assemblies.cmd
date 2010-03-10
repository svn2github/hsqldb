rem @echo off
set CD=%~dp0
set ANT_HOME=%CD%Etc\ant
set ANT=%ANT_HOME%\bin\ant.bat
set HSQLDB_HOME=%CD%..\..\..\..\base-one\trunk
set HSQLDB_GLUE_HOME=%CD%Etc\hsqldb-glue
call %ANT% -f "%HSQLDB_HOME%\build\build.xml" clean hsqldb
call %ANT% -f "%CD%Etc\hsqldb-glue\build.xml" clean jar
call %ANT% -f "%CD%Etc\restest\resdb\build.xml" clean jar
rename "%CD%Org.Hsqldb.dll" Org.Hsqldb.dll~
"%CD%ikvmc" -assembly:Org.Hsqldb "-keyfile:%CD%../Org.Hsqldb.snk" -target:library -version:1.8.1.2 "%HSQLDB_HOME%\lib\hsqldb.jar" "%HSQLDB_HOME%\lib\servlet.jar" "%HSQLDB_GLUE_HOME%\dist\hsqldb-glue.jar" 
del /Q "%CD%Org.Hsqldb.dll~"
"%CD%ikvmc" -assembly:resdb "-keyfile:%CD%../Org.Hsqldb.snk" -target:library -version:1.8.1.2 "%CD%Etc\restest\resdb\dist\resdb.jar"
