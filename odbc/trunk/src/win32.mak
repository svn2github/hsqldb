#
# $Id$
#
# HyperSQL ODBC Driver
#
# File:         win32.mak
#
# Description:  32-bit Windows Build file for hsqlodbcu Unicode driver,
#               and hsqlodbca ANSI driver for Win32.
#
# Run "nmake /f win32.mak HELP" to display the available targets and settings.
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

!IF "$(DRIVER_VARIANT)" == "ansi"
MAINLIB = hsqlodbca
!ELSE
MAINLIB = hsqlodbcu
!ENDIF
MAINDLL = $(MAINLIB).dll 
XALIB = pgxalib 
XADLL = $(XALIB).dll 

!IF "$(LINKMT)" == ""
LINKMT=MT
!ENDIF

!IF  "$(TARGETENV)" == "debug"
OUTDIR=..\build\$(DRIVER_VARIANT)g_$(CPU)
!ELSE
OUTDIR=..\build\$(DRIVER_VARIANT)_$(CPU)
!ENDIF
!IF "$(LINKMT)" != "MT"
OUTDIR = $(OUTDIR)$(LINKMT)
!ENDIF
OUTDIRBIN=$(OUTDIR)
INTDIR=$(OUTDIR)

ALLDLL  = "$(INTDIR)"
!IF "$(OUTDIR)" != "$(INTDIR)"
ALLDLL = $(ALLDLL) "$(OUTDIR)"
!ENDIF
ALLDLL  = $(ALLDLL) "$(OUTDIR)\$(MAINDLL)"

!IF "$(MSDTC)" != "no"
MSDTC=yes
!ENDIF
!IF  "$(MSDTC)" == "yes"
ALLDLL = $(ALLDLL) "$(OUTDIR)\$(XADLL)" "$(OUTDIR)\$(DTCDLL)"
!ENDIF

!IF "$(CPU)" == ""
CPU=win32
!ENDIF
!IF "$(TARGETENV)" == ""
TARGETENV=prod
!ENDIF 
!IF "$(DRIVER_VARIANT)" == "ansi"
ODBC_VERSION_MSG=ANSI driver is ODBC-3.0 compliant
!ELSE
ODBC_VERSION_MSG=Unicode driver is ODBC-3.5 compliant
DRIVER_VARIANT=unicode
!ENDIF

!IF "$(DRIVER_VARIANT)" == "ansi"
DTCLIB = pgenlista
!ELSE
DTCLIB = pgenlist
!ENDIF
DTCDLL = $(DTCLIB).dll 


# FIRST and therefore DEFAULT target:
ALL : BUILDMSGS GEN_CONFIG_H $(ALLDLL)

BUILDMSGS:
	@echo $(ODBC_VERSION_MSG)
	@echo Building $(DRIVER_VARIANT) driver for $(TARGETENV) $(CPU) w/multi-thread option '$(LINKMT)'.

HELP :
	@type win_syntaxmsg.txt

USE_LIBPQ=no
USE_SSPI=no
# SSPI off.  When we add SSL capability again, we will probably do it in a 
# more platform-portable way.
# LIBQP off, because our server does not support the Postgresql-specific
# libpq library.


#!IF "$(SSL_INC)" == ""
#SSL_INC=C:\OpenSSL\include
#!MESSAGE Using default OpenSSL Include directory: $(SSL_INC)
#!ENDIF

#!IF "$(SSL_LIB)" == ""
#SSL_LIB=C:\OpenSSL\lib\VC
#!MESSAGE Using default OpenSSL Library directory: $(SSL_LIB)
#!ENDIF

#SSL_DLL = "SSLEAY32.dll"
#RESET_CRYPTO = yes
#ADD_DEFINES = $(ADD_DEFINES) /D "SSL_DLL=\"$(SSL_DLL)\"" /D USE_SSL

ADD_DEFINES = $(ADD_DEFINES) /D NOT_USE_LIBPQ

# Seems to me to be very bad design to hard-code a product version when
# we have no clue about what the real product version is.  Commenting out
# in hopes that this variable is not used at all.  - blaine
#MSVC_VERSION=vc70

#!IF "$(RESET_CRYPTO)" == "yes"
#VC07_DELAY_LOAD=$(VC07_DELAY_LOAD) /DelayLoad:libeay32.dll
#ADD_DEFINES=$(ADD_DEFINES) /D RESET_CRYPTO_CALLBACKS
#!ENDIF
VC07_DELAY_LOAD=$(VC07_DELAY_LOAD) /delayLoad:$(DTCDLL) /DELAY:UNLOAD
VC_FLAGS=/EHsc
ADD_DEFINES = $(ADD_DEFINES) /D "DYNAMIC_LOAD"

!IF "$(MSDTC)" == "yes"
ADD_DEFINES = $(ADD_DEFINES) /D "_HANDLE_ENLIST_IN_DTC_"
!ENDIF
!IF "$(MEMORY_DEBUG)" == "yes"
ADD_DEFINES = $(ADD_DEFINES) /D "_MEMORY_DEBUG_" /GS
!ELSE
ADD_DEFINES = $(ADD_DEFINES) /GS
!ENDIF
!IF "$(DRIVER_VARIANT)" == "ansi"
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

GEN_CONFIG_H :
	config_h > config.h

# TODO:  Find out which of OUTDIR/INTDIR *.illk, *.idb, *.pdb get written
#        to, and clean them up too.
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
!IF  "$(TARGETENV)" == "debug"
CPP_PROJ=/nologo /$(LINKMT)d /Gm /ZI /Od /RTC1 /D "_DEBUG"
!ELSE 
CPP_PROJ=/nologo /$(LINKMT) /O2 /D "NDEBUG"
!ENDIF
CPP_PROJ=$(CPP_PROJ) /W3 $(VC_FLAGS) /D "WIN32" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "_CRT_SECURE_NO_DEPRECATE" /D "PSQLODBC_EXPORTS" /D "WIN_MULTITHREAD_SUPPORT" $(ADD_DEFINES) /Fp"$(INTDIR)\hsqlodbc.pch" /Fo"$(INTDIR)"\ /Fd"$(INTDIR)"\ /FD
#CPP_PROJ=$(CPP_PROJ) /W3 $(VC_FLAGS) /I "$(SSL_INC)" /D "WIN32" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "_CRT_SECURE_NO_DEPRECATE" /D "PSQLODBC_EXPORTS" /D "WIN_MULTITHREAD_SUPPORT" $(ADD_DEFINES) /Fp"$(INTDIR)\hsqlodbc.pch" /Fo"$(INTDIR)"\ /Fd"$(INTDIR)"\ /FD
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
!IF  "$(TARGETENV)" == "release"
MTL_PROJ=$(MTL_PROC) /D "NDEBUG" 
RSC_PROJ=$(RSC_PROJ) /d "NDEBUG"
!ELSE
MTL_PROJ=$(MTL_PROJ) /D "_DEBUG" 
RSC_PROJ=$(RSC_PROJ) /d "_DEBUG" 
!ENDIF
BSC32_SBRS= \
	
LINK32=link.exe
LIB32=lib.exe
!IF "$(MSDTC)" == "yes"
LINK32_FLAGS=$(OUTDIR)\$(DTCLIB).lib ws2_32.lib
!ENDIF
LINK32_FLAGS=$(LINK32_FLAGS) kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib wsock32.lib winmm.lib /nologo /dll /machine:I386 /def:$(DEF_FILE)
!IF  "$(DRIVER_VARIANT)" == "ansi"
DEF_FILE= "hsqlodbca.def"
!ELSE
DEF_FILE= "hsqlodbcu.def"
!ENDIF
!IF  "$(TARGETENV)" == "release"
LINK32_FLAGS=$(LINK32_FLAGS) /incremental:no
!ELSE
LINK32_FLAGS=$(LINK32_FLAGS) /incremental:yes /debug
!ENDIF
LINK32_FLAGS=$(LINK32_FLAGS) $(VC07_DELAY_LOAD)
#LINK32_FLAGS=$(LINK32_FLAGS) $(VC07_DELAY_LOAD) /libpath:"$(SSL_LIB)"

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
!IF "$(DRIVER_VARIANT)" == "unicode"
	"$(INTDIR)\win_unicode.obj" \
	"$(INTDIR)\odbcapiw.obj" \
	"$(INTDIR)\odbcapi30w.obj" \
!ENDIF
!IF "$(MSDTC)" == "yes"
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
