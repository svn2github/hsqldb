#-------------------------------------------------------------------------
#
# Makefile.am for hsqlodbcu and hsqlodbca(HyperSQL ODBC driver)
#
# $Id$
#
#-------------------------------------------------------------------------

AUTOMAKE_OPTIONS = 1.8 foreign

ACLOCAL_AMFLAGS = -I config

DEFS = -DNOT_USE_LIBPQ -DHAVE_CONFIG_H

if enable_unicode
lib_LTLIBRARIES = hsqlodbcu.la
else
lib_LTLIBRARIES = hsqlodbca.la
endif

if GCC
AM_CFLAGS = -Wall
endif

AM_LDFLAGS = -module -no-undefined -avoid-version

hsqlodbca_la_SOURCES = \
    info.c bind.c columninfo.c connection.c convert.c drvconn.c \
    environ.c execute.c lobj.c win_md5.c misc.c options.c \
    pgtypes.c psqlodbc.c qresult.c results.c socket.c parse.c \
    statement.c tuple.c dlg_specific.c loadlib.c \
    multibyte.c odbcapi.c descriptor.c \
    odbcapi30.c pgapi30.c info30.c mylog.c \
\
    bind.h catfunc.h columninfo.h connection.h convert.h \
    descriptor.h dlg_specific.h environ.h \
    lobj.h md5.h misc.h multibyte.h pgapifunc.h pgtypes.h \
    psqlodbc.h qresult.h resource.h socket.h statement.h tuple.h \
    version.h loadlib.h pgenlist.h

hsqlodbcu_la_SOURCES = $(hsqlodbca_la_SOURCES) \
    odbcapi30w.c odbcapiw.c win_unicode.c

EXTRA_hsqlodbca_la_SOURCES = md5.c
EXTRA_hsqlodbcu_la_SOURCES = md5.c

EXTRA_DIST = license.txt readme.txt \
    libtool.m4 hsqlodbcu.def hsqlodbca.def \
    pgenlist.def  pgenlista.def \
    dlg_wingui.c inouealc.c win_setup.h \
        setup.c win_unicode.c hsqlodbc.rc win64.mak \
        win32.mak hsqlodbc.dsp hsqlodbc.vcproj \
        hsqlodbc.sln msdtc_enlist.cpp pgxalib.cpp xalibname.c \
        pgxalib.def odbc.sql odbc-drop.sql \
    odbcapi25w.c \
\
    ../docs/index.html ../docs/config.html ../docs/config-opt.html \
    ../docs/faq.html ../docs/howto-accesslo.html \
    ../docs/howto-accessvba.html ../docs/howto-bo.html \
    ../docs/howto-ch.html ../docs/howto-csharp.html \
    ../docs/howto-vb.html ../docs/howto-vblo.html \
    ../docs/unix-compilation.html ../docs/win32-compilation.html \
    ../docs/release.html \
\
    ../msi-src/banner.bmp ../msi-src/lgpl.rtf \
    ../msi-src/Make.bat ../msi-src/upgrade.bat \
    ../msi-src/hsqlodbc.wxs ../msi-src/hsqlodbc-mm.wxs \
    ../msi-src/README.txt ../msi-src/background.bmp


MAINTAINERCLEANFILES = \
    Makefile.in config/config.guess config.h.in config/config.sub configure \
    config/install-sh config/missing aclocal.m4 config/ltmain.sh \
    config/depcomp
