@echo off
setlocal

::  $Id$
::
::  Clean script for the Wix MSI generator for hsqlodbc,
::
::
::  HyperSQL ODBC Driver
::
::  Copyright (C) 2009 Blaine Simpson and The HSQL Development Group
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

if not exist "hsqlodbc-mm.wxs" (
    echo This script must be run from the 'msi-src' subdirectory.
    exit /b 1
)

rmdir /s /q ..\dist
del *.wix*

:: Leave this exit in place so that our other scripts can check for
:: successful execution if they need to.
exit /b 0
