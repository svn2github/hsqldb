README FOR THE SOLARIS HSQLDB PACKAGE

$Id: readme.txt,v 1.1 2002/10/12 01:15:46 unsaved Exp $


JAVA SUPPORT

This package uses whatever Java is found in the OS'es traditional
search path, unless you set $JAVA_HOME in the config file 
(/etc/hsqldb.conf on Solaris).  If java's not found in the normal
search path and $JAVA_HOME's not set, then things won't work.

Currently supports Java runtimes for Java 2.x, 3.x, 4.x.  I need to
build hsqldb.lib for 1.x.



DEVELOPERS

build/packaging/pkg/pkgbuild is the main script to build a Solaris 
package.



IF YOU UPGRADE JAVA

If you change your Java version, do recreate the sym-link
.../lib/hsqldb.jar to pint to the proper one for your JRE.  JRE-to-
file mapping is as follows.

    JRE Version 1.2.x => hsqldb_j1.3.a.jar
    JRE Version 1.3.x => hsqldb_j1.3.b.jar
    JRE Version 1.4.x => hsqldb_j1.4.c.jar

(List the directory to see what a, b, and c actually are).  This
link is not used by the init scripts at all, but it is used by
runUtil.sh (and users may expect it to be there).  To see what
it's linked to currently, run something like

    ls -l /usr/hsqldb/lib/hsqldb.jar

Example

    cd /usr/hsqldb/lib
    ls hsqldb*.jar  #  To see what's available
    ln -s hsqldb_j123.456.jar hsldb.jar  # where the 1st .jar file is
					 # the one you want to use

(Note that these paths will be different if the HSQLhsqldb package was
installed with an non-default base directory).


CONFIGURATION

Main config file is /etc/hsqldb.conf on Solaris.

You can have multiple versions of hsqldb installed, and you can
have them installed to the same or different install bases (like /usr 
and /usr/local).   To keep these different baselines straight, the
hsqldb homes have a version-number in their name.  The last instance
installed gets a sym-link called "hsqldb" (i.e., no version in it)
right at the install base.  So, to use the default (last) instance
at any install base, just access "hsqldb".  Example

    /usr/hsqldb1.7.1   (default location on Solaris)
    /usr/hsqldb -> hsqldb1.7.1   (sym-link to default hsqldb instance)

(when I talk about "instance" here, I mean hsqldb "system" instances,
not database data-set instances).

Databases will automatically start and stop, as long as you have a file
/usr/hsqldb/data/$DBNAME/server.properties or
/usr/hsqldb/data/$DBNAME/webserver.properties, for each data set name
$DBNAME.  To use all default settings, just touch the file.
(See the section below "HOW TO CREATE A NEW DATABASE").

In general, we recommend against it, but if you want to run your servers 
as root, just change OWNER to root in /etc/hsqldb.conf and skip the rest 
of this file.

The rest of this file assumes that you are not running the daemons as
user 'root'.

By default, the daemons run as user 'hsqldb' (but you invoke the init
scripts as root).

You can not run a WebServer on the default port of 80 (since 80 is a 
privileged port).  See /usr/hsqldb/doc/hsqlAdvancedGuide.html.



HOW TO CREATE A NEW DATABASE

To creat a new Server instance (and a new data set), just make a
subdirectory off of hsqldb*/data, touch a server or webserver
properties file, and fix the ownership.

Example, to make and run a database server named db2...

    mkdir /usr/hsqldb/data/db2
    > /usr/hsqldb/data/db2/server.properties
    chown -R hsqldb:hsqldb /usr/hsqldb/data/db2
    /etc/init.d/hsqldb start

To run a hsqldb http server instead, do the same thing, but you
have to change the port if your $OWNER is not root (see the
configuration section above).

    mkdir /usr/hsqldb/data/db2
    print server.port=9009 > /usr/hsqldb/data/db2/webserver.properties
    chown -R hsqldb:hsqldb /usr/hsqldb/data/db2
    /etc/init.d/hsqldb start


Blaine
blaine.simpson@admc.com
unsaved at Sourceforge.net
