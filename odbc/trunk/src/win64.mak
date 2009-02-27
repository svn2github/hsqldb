#
# File:         win64.mak
#
# Description:  64-bit Windows Build file for hsqlodbcu Unicode driver,
#               and hsqlodbca ANSI driver for Win64.
#               (can be built using platform SDK's buildfarm) 
#
# Run "nmake /f win32.mak HELP" to display the available targets and settings.
#
# Comments:     Created by Hiroshi Inoue, 2006-10-31
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
#

!IF "$(CPU)" == ""
!ERROR You must set the CPU environment variable to distinguish your OS.
!ENDIF

!IF "$(DRIVER_VARIANT)" == "ansi"
MAINLIB = hsqlodbca
!ELSE
MAINLIB = hsqlodbcu
!ENDIF
MAINDLL = $(MAINLIB).dll 
XALIB = pgxalib 
XADLL = $(XALIB).dll 

!IF  "$(TARGETENV)" == "debug"
OUTDIR=..\build\$(DRIVER_VARIANT)g_$(CPU)
!ELSE
OUTDIR=..\build\$(DRIVER_VARIANT)_$(CPU)
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
	@echo Building $(DRIVER_VARIANT) driver for $(TARGETENV) $(CPU).

HELP :
	@type win_syntaxmsg.txt

ADD_DEFINES=/D _WIN64

#!IF "$(SSL_INC)" == ""
#SSL_INC=C:\OpenSSL\include
#!MESSAGE Using default OpenSSL Include directory: $(SSL_INC)
#!ENDIF

#!IF "$(SSL_LIB)" == ""
#SSL_LIB="C:\develop\lib\$(CPU)"
#!MESSAGE Using default OpenSSL Library directory: $(SSL_LIB)
#!ENDIF

#SSL_DLL = "SSLEAY32.dll"
#RESET_CRYPTO = yes
#ADD_DEFINES = $(ADD_DEFINES) /D "SSL_DLL=\"$(SSL_DLL)\"" /D USE_SSL
ADD_DEFINES = $(ADD_DEFINES) /D NOT_USE_LIBPQ

#!IF "$(RESET_CRYPTO)" == "yes"
#VC07_DELAY_LOAD=$(VC07_DELAY_LOAD) /DelayLoad:libeay32.dll
#ADD_DEFINES=$(ADD_DEFINES) /D RESET_CRYPTO_CALLBACKS
#!ENDIF
VC07_DELAY_LOAD="$(VC07_DELAY_LOAD) /DelayLoad:$(DTCDLL) /DELAY:UNLOAD"
ADD_DEFINES = $(ADD_DEFINES) /D "DYNAMIC_LOAD"

!IF "$(MSDTC)" == "yes"
ADD_DEFINES = $(ADD_DEFINES) /D "_HANDLE_ENLIST_IN_DTC_"
!ENDIF
!IF "$(MEMORY_DEBUG)" == "yes"
ADD_DEFINES = $(ADD_DEFINES) /D "_MEMORY_DEBUG_" /GS
!ENDIF
!IF "$(DRIVER_VARIANT)" == "ansi"
ADD_DEFINES = $(ADD_DEFINES) /D "DBMS_NAME=\"HyperSQL $(CPU)A\"" /D "ODBCVER=0x0300"
!ELSE
ADD_DEFINES = $(ADD_DEFINES) /D "DBMS_NAME=\"HyperSQL $(CPU)W\"" /D "ODBCVER=0x0351" /D "UNICODE_SUPPORT"
RSC_DEFINES = $(RSC_DEFINES) /D "UNICODE_SUPPORT"
!ENDIF

!IF "$(PORT_CHECK)" == "yes"
ADD_DEFINES = $(ADD_DEFINES) /Wp64
!ENDIF

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF

CLEAN :
	-@erase "$(INTDIR)\*.obj"
	-@erase "$(INTDIR)\*.res"
	-@erase "$(OUTDIR)\*.lib"
	-@erase "$(OUTDIR)\*.exp"
	-@erase "$(INTDIR)\*.pch"
	-@erase "$(OUTDIR)\$(MAINDLL)"
!IF "$(MSDTC)" == "yes"
	-@erase "$(OUTDIR)\$(DTCDLL)"
	-@erase "$(OUTDIR)\$(XADLL)"
!ENDIF

!IF  "$(MSDTC)" == "yes"
"$(OUTDIR)\$(MAINDLL)": "$(OUTDIR)\$(DTCLIB).lib"
!ENDIF

"$(INTDIR)" :
    if not exist "$(INTDIR)/$(NULL)" mkdir "$(INTDIR)"

!IF "$(OUTDIR)" != "$(INTDIR)"
"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"
!ENDIF

CPP=cl.exe
CPP_PROJ=/nologo /MD /W3 /EHsc /D "WIN32" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "_CRT_SECURE_NO_DEPRECATE" /D "PSQLODBC_EXPORTS" /D "WIN_MULTITHREAD_SUPPORT" $(ADD_DEFINES) /Fp"$(INTDIR)\psqlodbc.pch" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD 
#CPP_PROJ=/nologo /MD /W3 /EHsc /I "$(SSL_INC)" /D "WIN32" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "_CRT_SECURE_NO_DEPRECATE" /D "PSQLODBC_EXPORTS" /D "WIN_MULTITHREAD_SUPPORT" $(ADD_DEFINES) /Fp"$(INTDIR)\psqlodbc.pch" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD 
!IF  "$(CFG)" == "release"
CPP_PROJ=$(CPP_PROJ) /O2 /D "NDEBUG"
!ELSEIF  "$(CFG)" == "debug"
CPP_PROJ=$(CPP_PROJ) /Gm /ZI /Od /D "_DEBUG" /GZ
!ENDIF

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
RSC_PROJ=/l 0x809 /fo"$(INTDIR)\psqlodbc.res" /d "MULTIBUTE" 
BSC32_FLAGS=/nologo /o"$(OUTDIR)\psqlodbc.bsc" 
!IF  "$(CFG)" == "release"
MTL_PROJ=$(MTL_PROJ) /D "NDEBUG"
RSC_PROJ=$(RSC_PROJ) /d "NDEBUG" 
!ELSE
MTL_PROJ=$(MTL_PROJ) /D "_DEBUG" 
RSC_PROJ=$(RSC_PROJ) /d "_DEBUG" 
!ENDIF
BSC32_SBRS= \
	
LINK32=link.exe
LIB32=lib.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib advapi32.lib odbc32.lib odbccp32.lib wsock32.lib XOleHlp.lib winmm.lib "$(OUTDIR)\$(DTCLIB).lib" msvcrt.lib bufferoverflowu.lib /nologo /dll /machine:$(CPU) /def:"$(DEF_FILE)"
!IF  "$(DRIVER_VARIANT)" == "ansi"
DEF_FILE= "hsqlodbca.def"
!ELSE
DEF_FILE= "hsqlodbcu.def"
!ENDIF
!IF  "$(CFG)" == "release"
LINK32_FLAGS=$(LINK32_FLAGS) /incremental:no
!ELSE
LINK32_FLAGS=$(LINK32_FLAGS) /incremental:yes /debug /pdbtype:sept
!ENDIF
LINK32_FLAGS=$(LINK32_FLAGS) "$(VC07_DELAY_LOAD)"
#LINK32_FLAGS=$(LINK32_FLAGS) "$(VC07_DELAY_LOAD)" /libpath:"$(SSL_LIB)"

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
!ENDIF
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
LIB32_DTCLIBFLAGS=/nologo /machine:$(CPU) /def:"$(DTCDEF_FILE)"

LINK32_DTCFLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib uuid.lib wsock32.lib XOleHlp.lib $(OUTDIR)\$(MAINLIB).lib bufferoverflowu.lib Delayimp.lib /DelayLoad:XOLEHLP.DLL /nologo /dll /incremental:no /machine:$(CPU)
LINK32_DTCOBJS= \
        "$(INTDIR)\msdtc_enlist.obj" "$(INTDIR)\xalibname.obj"

XADEF_FILE= "$(XALIB).def"
LINK32_XAFLAGS=/nodefaultlib:libcmt.lib kernel32.lib user32.lib gdi32.lib advapi32.lib odbc32.lib odbccp32.lib wsock32.lib XOleHlp.lib winmm.lib msvcrt.lib bufferoverflowu.lib /nologo /dll /incremental:no /machine:$(CPU) /def:"$(XADEF_FILE)"
LINK32_XAOBJS= \
	"$(INTDIR)\pgxalib.obj" 

"$(OUTDIR)\$(MAINDLL)" : $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS) /pdb:$*.pdb /implib:$*.lib /out:$@
<<

"$(OUTDIR)\$(DTCLIB).lib" : $(DEF_FILE) $(LINK32_DTCOBJS)
    $(LIB32) @<<
  $(LIB32_DTCLIBFLAGS) $(LINK32_DTCOBJS) /out:$@
<<

"$(OUTDIR)\$(DTCDLL)" : $(LINK32_DTCOBJS)
    $(LINK32) @<<
  $(LINK32_DTCFLAGS) $(LINK32_DTCOBJS) $*.exp /pdb:$*.pdb /out:$@
<<

"$(OUTDIR)\$(XADLL)" : $(XADEF_FILE) $(LINK32_XAOBJS)
    $(LINK32) @<<
  $(LINK32_XAFLAGS) $(LINK32_XAOBJS) /pdb:$*.pdb /implib:$*.lib /out:$@
<<


SOURCE=hsqlodbc.rc

"$(INTDIR)\psqlodbc.res" : $(SOURCE) "$(INTDIR)"
	$(RSC) $(RSC_PROJ)  $(RSC_DEFINES) $(SOURCE)
