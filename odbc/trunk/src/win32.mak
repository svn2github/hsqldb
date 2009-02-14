#
# $Id$
#
# HyperSQL ODBC Driver
#
# File:         win32.mak
#
# Description:  32-bit Windows Build file for hsqlodbcu Unicode,
#               and hsqlodbca ANSI versionsfile for Win32.
#
# Configurations: Debug, Release
# Build Types:  ALL, CLEAN
# Usage:        NMAKE /f win32.mak CFG=[Release | Debug] [ALL | CLEAN]
#
# Comments:     Created by Dave Page, 2001-02-12
# Copyright (C) 1998          Insight Distribution Systems
#               Significant modifications Copyright 2009 by
#               the HSQL Development Group.  Changes made by the HSQL
#               Development are documented precisely in the public HyperSQL
#               source code repository, available through http://hsqldb.org.
# 
#   This library is free software; you can redistribute it and/or
#   modify it under the terms of the GNU Library General Public
#   License as published by the Free Software Foundation; either
#   version 2 of the License, or (at your option) any later version.
# 
#   This library is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#   Library General Public License for more details.
# 
#   You should have received a copy of the GNU Library General Public
#   License along with this library; if not, write to the
#   Free Foundation, Inc., 51 Franklin Street, Fifth Floor,
#   Boston, MA  02110-1301  USA

!IF "$(ANSI_VERSION)" == "yes"
!MESSAGE Building the HyperSQL ANSI 3.0 Driver for Win32...
!ELSE
!MESSAGE Building the HyperSQL Unicode 3.5 Driver for Win32...
!ENDIF
!MESSAGE
!IF "$(CFG)" == ""
CFG=Release
!MESSAGE No configuration specified. Defaulting to Release.
!MESSAGE
!ENDIF 

USE_LIBPQ=no
USE_SSPI=no
# SSPI off.  When we add SSL capability again, we will probably do it in a 
# more platform-portable way.
# LIBQP off, because our server does not support the Postgresql-specific
# libpq library.

!IF "$(CFG)" != "Release" && "$(CFG)" != "Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f win32.mak CFG=[Release | Debug] [ALL | CLEAN]
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "Release" (Win32 Release DLL)
!MESSAGE "Debug" (Win32 Debug DLL)
!MESSAGE 
!ERROR An invalid configuration was specified.
!ENDIF 

!IF "$(LINKMT)" == ""
LINKMT=MT
!ENDIF
!IF "$(LINKMT)" == "MT"
!MESSAGE Linking static Multithread library
!ELSE
!MESSAGE Linking dynamic Multithread library
!ENDIF

!IF "$(SSL_INC)" == ""
SSL_INC=C:\OpenSSL\include
!MESSAGE Using default OpenSSL Include directory: $(SSL_INC)
!ENDIF

!IF "$(SSL_LIB)" == ""
SSL_LIB=C:\OpenSSL\lib\VC
!MESSAGE Using default OpenSSL Library directory: $(SSL_LIB)
!ENDIF

#SSL_DLL = "SSLEAY32.dll"
#RESET_CRYPTO = yes
#ADD_DEFINES = $(ADD_DEFINES) /D "SSL_DLL=\"$(SSL_DLL)\"" /D USE_SSL

ADD_DEFINES = $(ADD_DEFINES) /D NOT_USE_LIBPQ

!IF "$(ANSI_VERSION)" == "yes"
DTCLIB = pgenlista
!ELSE
DTCLIB = pgenlist
!ENDIF
DTCDLL = $(DTCLIB).dll 
!IF "$(_NMAKE_VER)" == "6.00.9782.0"
MSVC_VERSION=vc60
VC07_DELAY_LOAD=
MSDTC=no
VC_FLAGS=/GX /YX
!ELSE
MSVC_VERSION=vc70
#!IF "$(RESET_CRYPTO)" == "yes"
#VC07_DELAY_LOAD=$(VC07_DELAY_LOAD) /DelayLoad:libeay32.dll
#ADD_DEFINES=$(ADD_DEFINES) /D RESET_CRYPTO_CALLBACKS
#!ENDIF
VC07_DELAY_LOAD=$(VC07_DELAY_LOAD) /delayLoad:$(DTCDLL) /DELAY:UNLOAD
VC_FLAGS=/EHsc
!ENDIF
ADD_DEFINES = $(ADD_DEFINES) /D "DYNAMIC_LOAD"

!IF "$(MSDTC)" != "no"
ADD_DEFINES = $(ADD_DEFINES) /D "_HANDLE_ENLIST_IN_DTC_"
!ENDIF
!IF "$(MEMORY_DEBUG)" == "yes"
ADD_DEFINES = $(ADD_DEFINES) /D "_MEMORY_DEBUG_" /GS
!ELSE
ADD_DEFINES = $(ADD_DEFINES) /GS
!ENDIF
!IF "$(ANSI_VERSION)" == "yes"
ADD_DEFINES = $(ADD_DEFINES) /D "DBMS_NAME=\"HyperSQL ANSI\"" /D "ODBCVER=0x0350"
!ELSE
ADD_DEFINES = $(ADD_DEFINES) /D "UNICODE_SUPPORT" /D "ODBCVER=0x0351"
RSC_DEFINES = $(RSC_DEFINES) /D "UNICODE_SUPPORT"
!ENDIF
!IF "$(PORTCHECK_64BIT)" == "yes"
# ADD_DEFINES = $(ADD_DEFINES) /Wp64
ADD_DEFINES = $(ADD_DEFINES) /D _WIN64
!ENDIF

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF

!IF "$(ANSI_VERSION)" == "yes"
MAINLIB = hsqlodbca
!ELSE
MAINLIB = hsqlodbcu
!ENDIF
MAINDLL = $(MAINLIB).dll 
XALIB = pgxalib 
XADLL = $(XALIB).dll 

!IF  "$(CFG)" == "Release"
!IF  "$(ANSI_VERSION)" == "yes"
OUTDIR=.\MultibyteRelease
OUTDIRBIN=.\MultibyteRelease
INTDIR=.\MultibyteRelease
!ELSE
OUTDIR=.\Release
OUTDIRBIN=.\Release
INTDIR=.\Release
!ENDIF
!ELSEIF  "$(CFG)" == "Debug"
!IF  "$(ANSI_VERSION)" == "yes"
OUTDIR=.\MultibyteDebug
OUTDIRBIN=.\MultibyteDebug
INTDIR=.\MultibyteDebug
!ELSE
OUTDIR=.\Debug
OUTDIRBIN=.\Debug
INTDIR=.\Debug
!ENDIF
!ENDIF
!IF "$(LINKMT)" != "MT"
OUTDIR = $(OUTDIR)$(LINKMT)
OUTDIRBIN = $(OUTDIRBIN)$(LINKMT)
INTDIR = $(INTDIR)$(LINKMT)
!ENDIF

ALLDLL  = "$(INTDIR)"
!IF "$(OUTDIR)" != "$(INTDIR)"
ALLDLL = $(ALLDLL) "$(OUTDIR)"
!ENDIF
ALLDLL  = $(ALLDLL) "$(OUTDIR)\$(MAINDLL)"

!IF  "$(MSDTC)" != "no"
ALLDLL = $(ALLDLL) "$(OUTDIR)\$(XADLL)" "$(OUTDIR)\$(DTCDLL)"
!ENDIF

ALL : $(ALLDLL)

CLEAN :
	-@erase "$(INTDIR)\*.obj"
	-@erase "$(INTDIR)\*.res"
	-@erase "$(OUTDIR)\*.lib"
	-@erase "$(OUTDIR)\*.exp"
	-@erase "$(INTDIR)\*.pch"
	-@erase "$(OUTDIR)\$(MAINDLL)"
!IF "$(MSDTC)" != "no"
	-@erase "$(OUTDIR)\$(DTCDLL)"
	-@erase "$(OUTDIR)\$(XADLL)"
!ENDIF

"$(INTDIR)" :
    if not exist "$(INTDIR)/$(NULL)" mkdir "$(INTDIR)"
!IF "$(OUTDIR)" != "$(INTDIR)"
"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"
!ENDIF

!IF  "$(MSDTC)" != "no"
"$(OUTDIR)\$(MAINDLL)" : "$(OUTDIR)\$(DTCLIB).lib"
!ENDIF

$(INTDIR)\connection.obj $(INTDIR)\psqlodbc.res: config.h

CPP=cl.exe
!IF  "$(CFG)" == "Release"
CPP_PROJ=/nologo /$(LINKMT) /O2 /D "NDEBUG"
!ELSEIF  "$(CFG)" == "Debug"
CPP_PROJ=/nologo /$(LINKMT)d /Gm /ZI /Od /RTC1 /D "_DEBUG"
!ENDIF
CPP_PROJ=$(CPP_PROJ) /W3 $(VC_FLAGS) /I "$(SSL_INC)" /D "WIN32" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "_CRT_SECURE_NO_DEPRECATE" /D "PSQLODBC_EXPORTS" /D "WIN_MULTITHREAD_SUPPORT" $(ADD_DEFINES) /Fp"$(INTDIR)\hsqlodbc.pch" /Fo"$(INTDIR)"\ /Fd"$(INTDIR)"\ /FD
!MESSAGE CPP_PROJ=$(CPP_PROJ)
.c{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) /c $< 
<<

.cpp{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) /c $< 
<<

.cxx{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) /c $< 
<<

.c{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) /c $< 
<<

.cpp{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) /c $< 
<<

.cxx{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) /c $< 
<<

MTL=midl.exe
RSC=rc.exe
BSC32=bscmake.exe
MTL_PROJ=/nologo /mktyplib203 /win32 
RSC_PROJ=/l 0x809 /d "MULTIBYTE" 
BSC32_FLAGS=/nologo /o"$(OUTDIR)\hsqlodbc.bsc" 
!IF  "$(CFG)" == "Release"
MTL_PROJ=$(MTL_PROC) /D "NDEBUG" 
RSC_PROJ=$(RSC_PROJ) /d "NDEBUG"
!ELSE
MTL_PROJ=$(MTL_PROJ) /D "_DEBUG" 
RSC_PROJ=$(RSC_PROJ) /d "_DEBUG" 
!ENDIF
BSC32_SBRS= \
	
LINK32=link.exe
LIB32=lib.exe
!IF "$(MSDTC)" != "no"
LINK32_FLAGS=$(OUTDIR)\$(DTCLIB).lib ws2_32.lib
!ENDIF
LINK32_FLAGS=$(LINK32_FLAGS) kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib wsock32.lib winmm.lib /nologo /dll /machine:I386 /def:$(DEF_FILE)
!IF  "$(ANSI_VERSION)" == "yes"
DEF_FILE= "hsqlodbca.def"
!ELSE
DEF_FILE= "hsqlodbcu.def"
!ENDIF
!IF  "$(CFG)" == "Release"
LINK32_FLAGS=$(LINK32_FLAGS) /incremental:no
!ELSE
LINK32_FLAGS=$(LINK32_FLAGS) /incremental:yes /debug
!ENDIF
LINK32_FLAGS=$(LINK32_FLAGS) $(VC07_DELAY_LOAD) /libpath:"$(SSL_LIB)"

LINK32_OBJS= \
	"$(INTDIR)\bind.obj" \
	"$(INTDIR)\columninfo.obj" \
	"$(INTDIR)\connection.obj" \
	"$(INTDIR)\convert.obj" \
	"$(INTDIR)\dlg_specific.obj" \
	"$(INTDIR)\dlg_wingui.obj" \
	"$(INTDIR)\drvconn.obj" \
	"$(INTDIR)\environ.obj" \
	"$(INTDIR)\execute.obj" \
	"$(INTDIR)\info.obj" \
	"$(INTDIR)\info30.obj" \
	"$(INTDIR)\lobj.obj" \
	"$(INTDIR)\win_md5.obj" \
	"$(INTDIR)\misc.obj" \
	"$(INTDIR)\mylog.obj" \
	"$(INTDIR)\pgapi30.obj" \
	"$(INTDIR)\multibyte.obj" \
	"$(INTDIR)\options.obj" \
	"$(INTDIR)\parse.obj" \
	"$(INTDIR)\pgtypes.obj" \
	"$(INTDIR)\hsqlodbc.obj" \
	"$(INTDIR)\qresult.obj" \
	"$(INTDIR)\results.obj" \
	"$(INTDIR)\setup.obj" \
	"$(INTDIR)\socket.obj" \
	"$(INTDIR)\statement.obj" \
	"$(INTDIR)\tuple.obj" \
	"$(INTDIR)\odbcapi.obj" \
	"$(INTDIR)\odbcapi30.obj" \
	"$(INTDIR)\descriptor.obj" \
	"$(INTDIR)\loadlib.obj" \
!IF "$(ANSI_VERSION)" != "yes"
	"$(INTDIR)\win_unicode.obj" \
	"$(INTDIR)\odbcapiw.obj" \
	"$(INTDIR)\odbcapi30w.obj" \
!ENDIF
!IF "$(MSDTC)" != "no"
	"$(INTDIR)\xalibname.obj" \
!ENDIF
!IF "$(MEMORY_DEBUG)" == "yes"
	"$(INTDIR)\inouealc.obj" \
!ENDIF
	"$(INTDIR)\psqlodbc.res"

DTCDEF_FILE= "$(DTCLIB).def"
LIB_DTCLIBFLAGS=/nologo /machine:I386 /def:$(DTCDEF_FILE)

LINK32_DTCFLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib uuid.lib wsock32.lib XOleHlp.lib $(OUTDIR)\$(MAINLIB).lib Delayimp.lib /DelayLoad:XOLEHLP.DLL /nologo /dll /incremental:no /machine:I386
LINK32_DTCOBJS= \
	"$(INTDIR)\msdtc_enlist.obj" "$(INTDIR)\xalibname.obj"

XADEF_FILE= "$(XALIB).def"
LINK32_XAFLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib odbc32.lib odbccp32.lib uuid.lib wsock32.lib /nologo /dll /incremental:no /machine:I386 /def:$(XADEF_FILE)
LINK32_XAOBJS= \
	"$(INTDIR)\pgxalib.obj" 

"$(OUTDIR)\$(MAINDLL)" : $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS) /pdb:$*.pdb /implib:$*.lib /out:$@
<<

"$(OUTDIR)\$(DTCLIB).lib" : $(DTCDEF_FILE) $(LINK32_DTCOBJS)
    $(LIB32) @<<
  $(LIB_DTCLIBFLAGS) $(LINK32_DTCOBJS) /out:$@
<<

"$(OUTDIR)\$(DTCDLL)" : $(DTCDEF_FILE) $(LINK32_DTCOBJS)
    $(LINK32) @<<
  $(LINK32_DTCFLAGS) $(LINK32_DTCOBJS) $*.exp /pdb:$*.pdb /out:$@ 
<<

"$(OUTDIR)\$(XADLL)" : $(XADEF_FILE) $(LINK32_XAOBJS)
    $(LINK32) @<<
  $(LINK32_XAFLAGS) $(LINK32_XAOBJS) /pdb:$*.pdb /implib:$*.lib /out:$@
<<


SOURCE=hsqlodbc.rc

"$(INTDIR)\psqlodbc.res" : $(SOURCE)
	$(RSC) $(RSC_PROJ) /fo$@ $(RSC_DEFINES) $(SOURCE)
