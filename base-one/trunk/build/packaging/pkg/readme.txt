README FOR THE SOLARIS HSQLDB PACKAGE

$Id: readme.txt,v 1.9 2002/11/10 22:30:49 unsaved Exp $


JAVA SUPPORT

This package uses whatever Java is found in the OS'es traditional
search path, unless you set $JAVA_HOME in the config file 
(/etc/hsqldb.conf on Solaris).  If java's not found in the normal
search path and $JAVA_HOME's not set, then things won't work.

Currently supports Java runtimes for Java 1.x, 2.x, 3.x, 4.x.

Be aware that if you don't have Java installed in the standard
path, the hsqldb package install will tell you that you need to
specify the $JAVA_HOME in the hsqldb config file.  For 1.x, even
the standard install location needs to be specified because sym-
links don't work for the 1.x binaries.

Known bug with Java 1.x:  If you run the daemons as root (which is
not the default), the default shutdown method fails and results in
a long wait before shutdown with TERM signal succeeds.  If this
bothers you, then upgrade your java, don't run as root, or set a 
short timeout in the hsqldb config file.


IF YOU UPGRADE JAVA

If you change your Java version, do recreate the sym-link
.../lib/hsqldb.jar to point to the proper one for your JRE.  JRE-to-
file mapping is as follows.

    JRE Version 1.1.x => hsqldb_jre1_1_8.jar
    JRE Version 1.2.x => hsqldb_jre1_3_1.jar
    JRE Version 1.3.x => hsqldb_jre1_3_1.jar
    JRE Version 1.4.x => hsqldb_jre1_4_0.jar

This link is not used by the init scripts at all, but it is used by
runUtil.sh (and users may expect it to be there).  To see what
it's linked to currently, run something like

    ls -l /usr/hsqldb/lib/hsqldb.jar

Example

    cd /usr/hsqldb/lib
    ls hsqldb*.jar  #  To see what's available
    ln -s hsqldb_jre1_2_3.jar hsldb.jar  # where the 1st .jar file is
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

    /usr/hsqldb-1.7.1   (default location on Solaris)
    /usr/hsqldb -> hsqldb-1.7.1   (sym-link to default hsqldb instance)

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

By default, you do get a database named 'db1' that will automatically
start and stop when your Solaris server is booted up and shut down.

To create a new Server instance (and a new data set), just make a
subdirectory off of hsqldb*/data, touch a server or webserver
properties file, and fix the ownership.  The database "name" will
be the name of the subdirectory you created, and the database
files in that directory will (for the most part) be based on that
name (e.g. "/usr/hsqldb-1.7.1/data/dbname.data").

Example, to make and run a database server named db2...

    mkdir /usr/hsqldb/data/db2
    chown hsqldb:hsqldb /usr/hsqldb/data/db2
    touch /usr/hsqldb/data/db2/server.properties  # Remains owned by root
    /etc/init.d/hsqldb restart

To run a hsqldb http server instead, do the same thing, but use
"webserver.properties" instead of "server.properties" and you have
to change the port if your $OWNER is not root (see the configuration 
section above).  Example...

    mkdir /usr/hsqldb/data/db2
    chown hsqldb:hsqldb /usr/hsqldb/data/db2
    echo server.port=9009 > /usr/hsqldb/data/db2/webserver.properties
    /etc/init.d/hsqldb restart



SOLARIS

    To install the Solaris package

	uncompress hsqldb1.2.3.pkg.Z
	pkgadd -n hsqldb1.2.3.pkg HSQLhsqldb

    To install to an install base other than /usr, make an Admin
    file (like copy /var/sadm/install/admin/default) and set
    "basedir" whatever you want, then specify the Admin file to
    pkgadd with -a.

	pkgadd -na file.admin hsqldb1.2.3.pkg HSQLhsqldb


    MULTIPLE INSTANCES

    You can install multiple instances of HSQLhsqldb, as long as
    the version is unique.  If you want more than one copy of the
    same version, then you will have to copy files manually because
    Solaris doesn't permit that.  If pkgadd refuses to let you 
    install an additional package even though the version is 
    unique, then you probably need to set the Admin file variable
    "instance" to "unique".

    If you don't understand what I say about Admin files,  run
    "man pkgadd" and "man -s 4 admin".


HSQLDB DEVELOPERS

Most of the files in .../pkg/cfg are named like HSQLhsqldb.something.
The intention was for the base name to be the entire package name, so
they should be HSQLDBhsqldb.something.  They will probably be ranamed
propertly in some future version.

To build a Solaris package, you need to do a cvs checkout of the
hsqldb-dev module (HEAD or tag hsqldb_1_7_1_EXT, depending on what you
want).  For suggestions of the checkout command, click the CVS tab at
http://sourceforge.net/projects/hsqldb.  You MUST!! put the
hsqldb*.jar files into place before running pkgbuild, or your resultant
package will not contain any hsqldb*.jar files.

.../build/packaging/pkg/pkgbuild is the main script to build a Solaris 
package.  Give the -p switch to rebuild the prototype file 
(definitely need to do that if there were any changes to anything
in the software to be delivered... as opposed to just a version or
package parameter change).

Contents of the Solaris package.  The package will contain exactly what
is listed in the .../build/packaging/pkg/cfg/*.proto file which is 
generated by "pkgbuild -p".  Several files in the checkedout out module
are specifically not included in the Solaris package.  To find out
exactly what files are currently excluded, see the command beginning
with "perl -ni.safe -we" in the pkgbuild script.  At the time I am
writing this, the perl command excludes the following:

    $HSQLDB_HOME/classes/...
    $HSQLDB_HOME/build/packaging
    $HSQLDB_HOME/.../CVS...
    $HSQLDB_HOME/lib/hsqldb.jar


Blaine
blaine.simpson@admc.com
unsaved at Sourceforge.net
