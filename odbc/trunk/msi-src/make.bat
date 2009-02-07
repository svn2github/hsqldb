@echo off

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

REM Values to change include VERSION and SUBLOC, both below.

REM The subdirectory to install into
SET SUBLOC="0803"

if NOT "%1"=="" SET VERSION="%1"
if NOT "%1"=="" GOTO GOT_VERSION

REM The full version number of the build in XXXX.XX.XX format
SET VERSION="08.03.0400"

echo.
echo Version not specified - defaulting to %VERSION%
echo.

:GOT_VERSION

echo.
echo Building hsqlodbc merge module...

candle -nologo -dVERSION=%VERSION% -dPROGRAMFILES="%ProgramFiles%" -dSYSTEM32DIR="%SystemRoot%/system32" psqlodbcm.wxs
IF ERRORLEVEL 1 GOTO ERR_HANDLER

light -nologo -out psqlodbc.msm psqlodbcm.wixobj
IF ERRORLEVEL 1 GOTO ERR_HANDLER

echo.
echo Building hsqlodbc MSI database...

candle -nologo -dVERSION=%VERSION% -dPROGRAMFILES="%ProgramFiles%" -dPROGRAMCOM="%ProgramFiles%/Common Files/Merge Modules" psqlodbc.wxs
IF ERRORLEVEL 1 GOTO ERR_HANDLER

light -nologo -ext WixUIExtension -cultures:en-us -sw1076 -sw1055 -sw1056 psqlodbc.wixobj
IF ERRORLEVEL 1 GOTO ERR_HANDLER

echo.
echo Done!
GOTO EXIT

:ERR_HANDLER
echo.
echo Aborting build!
GOTO EXIT

:EXIT
