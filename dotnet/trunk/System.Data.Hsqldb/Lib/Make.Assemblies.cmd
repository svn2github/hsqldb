@echo off
set CD=%~dp0
set ANT_HOME=%CD%Etc\ant
set ANT=%ANT_HOME%\bin\ant.bat
call %ANT% -f "%CD%Etc\hsqldb_1_8_0_10_patched\build\build.xml" clean hsqldb
call %ANT% -f "%CD%Etc\hsqldb-glue\build.xml" clean jar
call %ANT% -f "%CD%Etc\restest\resdb\build.xml" clean jar
rename "%CD%Org.Hsqldb.dll" Org.Hsqldb.dll~
%CD%ikvmc -assembly:Org.Hsqldb -keyfile:%CD%../Org.Hsqldb.snk -target:library -version:1.8.0.10 %CD%Etc\hsqldb_1_8_0_10_patched\lib\hsqldb.jar %CD%Etc\hsqldb-glue\dist\hsqldb-glue.jar %CD%Etc\hsqldb_1_8_0_10_patched\lib\servlet.jar
del /Q %CD%Org.Hsqldb.dll~
%CD%ikvmc -assembly:resdb -keyfile:%CD%../Org.Hsqldb.snk -target:library -version:1.8.0.10 %CD%Etc\restest\resdb\dist\resdb.jar
