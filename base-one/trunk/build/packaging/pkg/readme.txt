README FOR THE SOLARIS HSQLDB PACKAGE

$Id$

This is badly out of date and inaccurate now.


DEVELOPERS:  build/packaging/pkg/pkgbuild is the main script to build
a Solaris package.

Currently supports Java runtimes for Java 2.x, 3.x, 4.0.x, 4.1.x.  If
other JDK/JRE's are available for Solaris, it's easy to add support for 
them too.  (We need the JDK's to produce this package, you only need the 
runtimes to use it).


This package depends on your Java being installed in normal Solaris fashion.
You should have sym-links to the normal end-user programs in /usr/bin,
like "java", "jar", etc.  (Normally these links point indirectly to files 
under the version-specific java homes like /usr/java/j2sdk1_3_1_05).

hsqldb.jar files were built with 2.2, 3.1, 4.0, 4.1.  The package
install script will link "hsqldb.jar" to the version appropriate for
your current default java binary.  (This link is used by the scripts
in /usr/hsqldb/bin;  however, it is not used by the init script, which
determines the Java automatically each time it is run.).  You can see 
what version hsqldb.jar is set to by running

    ls -l /usr/hsqldb/lib/hsqldb.jar

If you want to chang to the jar file for a different Java version (like
because you have upgraded your Java to a newer version), then change
the sym-link manually.

    cd /usr/hsqldb/lib
    ls hsqldb*.jar  #  To see what's available
    ln -s hsqldb_j123.456.jar hsldb.jar  # where the 1st .jar file is
					 # the one you want to use

(Note that these paths will be different if the HSQLhsqldb package was
installed with an non-default base directory).

Main config file is /etc/hsqldb.conf.

Databases will automatically start and stop, as long as you have a file
/usr/hsqldb/data/$DBNAME/server.properties or
/usr/hsqldb/data/$DBNAME/webserver.properties, for each data set name
$DBNAME.  To use all default settings, just touch the file.

In general, we recommend against it, but if you want to run your servers 
as root, just change OWNER to root in /etc/hsqldb.conf and skip the rest 
of this file.

The rest of this file assumes that you are not running the daemons as
user 'root'.

By default, the daemons run as user 'hsqldb' (but you invoke the init
scripts as root).

You can not run a WebServer on the default port of 80 (since 80 is a 
privileged port).  See /usr/hsqldb/doc/hsqlAdvancedGuide.html.

To make a new database, you must make the directory 
/usr/hsqldb/data/$DBNAME and the file 
/usr/hsqldb/data/$DBNAME/server.properties or
/usr/hsqldb/data/$DBNAME/webserver.properties writable by the user
hsqldb.



Tue Oct  8 14:45:50 EDT 2002
Blaine
blaine.simpson@admc.com
unsaved at Sourceforge.net
