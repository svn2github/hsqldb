HOW TO CREATE AND WORK WITH DOCBOOK DOCUMENTS FOR HSQLDB

At some point, this document itself should be converted to DocBook format.


Our DocBook strategy is still subject to change.  For now...


TO CREATE A NEW DOCBOOK BOOK

Think up a base filename for your document.
It should be nice and short, without funny characters (but hyphens, other
than leading hypends, are ok).  Example:  sqltool.
Hereafter, I'll refer to this as your "book name".

Create a subdirectory of "docsrc" with name of your book name.
I'll refer to this directory (docsrc + book name) as "your book directory".

Inside your book directory, create your main DocBook source file with name
of your book name + ".xml", e.g. "sqltool.xml".

Your DocBook document may reference or include shared files in the
main 'docsrc' directory as well as any files which you put into your
book directory.
You may want to include sample .java files, screen shots, or component
DocBook source files.
Usually you will just copy these files right into your book directory.

For examples of just about everything, see .../docsrc/sqltool/sqltool.xml.
Notice that sqltool.xml pulls in a document section from the main docsrc
directory.


HOW TO REFERENCE OR INCLUDE OTHER FILES IN YOUR DOCBOOK SOURCE FILE(s).

To link to outside documents (which you supply or not), you'll usually
use the DocBook <ulink> element.

To "import" other documents, just use the unparsed entity mechanism.
This is a basic DTD-style XML feature where you use macros like
&entityname;.  Either find an XML reference or look around our existing
DocBook source files for an example to follow.

One tricky point is how to include external files verbatim.  If you
just read in external files as unparsed entities, they will be parsed
as DocBook source (and therefore they must consist of, at least, legal
XML).
But often you will want to import a real, working file (like a 
configuration file, sql file, Java source file), and you won't want to
hack it up just so you can import it.
(For one thing, you shouldn't have to; for another, you may want to 
provide a link to the file for download, so you wouldn't want people
to download a hacked-up version).
It would be nice if you could CDATA, then include the entity, but that
won't work since the &...; inclusion directive would thereby be escaped.
If you don't know what the hell CDATA is, just follow the instructions
in the next paragraph.

To import a document verbatim, define an unparsed entity to the file
../../docwork/BOOKNAME/cdata/file.name, where BOOKNAME is your book
name and file.name is the name of the file to be imported (which
resides in the current directory.
If you want to know, what will happen is, the Ant build will copy the 
file-to-be-imported to the directory .../docwork/BOOKNAME/cdata and will 
sandwich it in a CDATA directive.
If you want to provide a link to the document, you just ulink to 
the document in the current directory, not to the one in the cdata
