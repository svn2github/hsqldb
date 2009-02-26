@echo off
setlocal

::  $Id$
::
::  Clean script for the Wix MSI generator for hsqlodbc,
::
::
::  HyperSQL ODBC Driver
::
::  Copyright (C) 2009 Blaine Simpson and the HSQL Development Group
::  Significant modifications Copyright 2009 by the HSQL Development Group.
::  Changes made by the HSQL Development are documented precisely in the
::  public HyperSQL source code repository, available through http://hsqldb.org.
::
::  This library is free software; you can redistribute it and/or
::  modify it under the terms of the GNU Library General Public
::  License as published by the Free Software Foundation; either
::  version 2 of the License, or (at your option) any later version.
::
::  This library is distributed in the hope that it will be useful,
::  but WITHOUT ANY WARRANTY; without even the implied warranty of
::  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
::  Library General Public License for more details.
::
::  You should have received a copy of the GNU Library General Public
::  License along with this library; if not, write to the
::  Free Foundation, Inc., 51 Franklin Street, Fifth Floor,
::  Boston, MA  02110-1301  USA

if not exist "..\src\setpversion.cmd" (
    echo 'setpversion.cmd' not present in source directory
    exit /b 1
)
if not exist "../src/MultibyteRelease/hsqlodbca.dll" (
    echo 'hsqlodbca.dll' not present in MultibyteRelease directory
    exit /b 1
)
if not exist "../src/Release/hsqlodbcu.dll" (
    echo 'hsqlodbcu.dll' not present in Release directory
    exit /b 1
)

java -version > nul
if errorlevel 1 (
    echo You must have Java in your search path
    exit /b 1
)

if not (%1)==() (
    echo This script does not take any command-line arguments
    exit /b 1
)

for /F "usebackq" %%x in (`java -jar ../bin/guidgen.jar`) do set NEW_GUID=%%x

call ..\src\setpversion.cmd
if errorlevel 1 exit /b 1

echo Building hsqlodbc merge module v. %PACKAGE_VERSION%...

candle -nologo -dVERSION=%PACKAGE_VERSION% -dPROGRAMFILES="%ProgramFiles%" -dSYSTEM32DIR="%SystemRoot%/system32" -dNEW_GUID="%NEW_GUID%" hsqlodbc-mm.wxs
if errorlevel 1 (
    echo Candle execution for Merge Module failed
    exit /b 1
)

light -nologo -o ..\dist\hsqlodbc-%PACKAGE_VERSION%.msm hsqlodbc-mm.wixobj
if errorlevel 1 (
    echo Light execution for Merge Module failed
    exit /b 1
)

if not exist "../doc/hsqlodbc/hsqlodbc.html" (
    echo You must build the User Guide with Ant before building the MSI
    exit /b 1
)
echo Building hsqlodbc MSI database v. %PACKAGE_VERSION% for the MM...

candle -nologo -dVERSION=%PACKAGE_VERSION% -dPROGRAMFILES="%ProgramFiles%" -dPROGRAMCOM="%ProgramFiles%/Common Files/Merge Modules" hsqlodbc.wxs
if errorlevel 1 (
    echo Candle execution for MSI failed
    exit /b 1
)

light -nologo -o ..\dist\hsqlodbc-%PACKAGE_VERSION%.msi -ext WixUIExtension -cultures:en-us -sw1076 -sw1055 -sw1056 hsqlodbc.wixobj
if errorlevel 1 (
    echo Light execution for MSI failed
    exit /b 1
)

exit /b 0
