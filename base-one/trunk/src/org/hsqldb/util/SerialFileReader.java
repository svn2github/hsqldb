package org.hsqldb.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SerialFileReader extends BufferedReader {
    private BufferedReader cur = null;
    private int curIndex = -1;
    private int curLine;
    private BufferedReader[] brArray = null;
    private File[] fileArray = { null };

    public static void main(String[] sa) {
        System.err.println("Hi Blaine");
        SerialFileReader sfr = null;
        String s;
        try {
            File[] fa = null;
            if (sa.length > 0) {
                fa = new File[sa.length];
                for (int j = 0; j < sa.length; j++) fa[j] = new File(sa[j]);
            }
            sfr = new SerialFileReader(fa);
            int i = 0;
            while ((s = sfr.readLine()) != null)
                System.out.println(Integer.toString(++i) + " ("
                        + s + ')');
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("CAUGHT a " + e.getClass().getName());
            System.err.println(
                    " at " + sfr.getCurFileName() + '/' + sfr.getCurLine()
                    + ":  " + e);
            System.exit(1);
        }
        System.exit(0);
    }

    public int getCurLine() { 
        return curLine;
    }
    public String getCurFileName() { 
System.err.println("FA=" + fileArray);
        return ((curIndex < 0 || curIndex >= fileArray.length)
                ? null
                : ((fileArray[curIndex] == null) ? "<STDIN>"
                        : (fileArray[curIndex].toString())));
    }

    /**
     * Initializes an array of BufferedReaders.
     * We instantiate all of the BufferedReaders up front.  We'll catch
     * many accessibility errors early this way, and it shouldn't be
     * much of a performance hit since we're not reading anything at all
     * now.
     *
     * @param inFileArray  Array of input files OR null for stdin.
     */
    public SerialFileReader(File[] inFileArray) throws FileNotFoundException {
        super(new java.io.StringReader(""));
        fileArray = inFileArray;
        curIndex = 0;
        curLine = 0;
        if (fileArray == null) {
            brArray = new BufferedReader[1];
            brArray[0] = new BufferedReader(new InputStreamReader(
                    System.in));
            cur = brArray[0];
            return;
        }
        brArray = new BufferedReader[fileArray.length];
        for (int i = 0; i < fileArray.length; i++) {
            brArray[i] = new BufferedReader(new FileReader(fileArray[i]));
        }
        cur = brArray[0];
    }

    private boolean next() throws IOException {
        cur.close();
        curIndex++;
        cur = ((curIndex == brArray.length) ? null : brArray[curIndex]);
        return (cur != null);
    }

    public int read() throws IOException {
        int i;
        while (true) if (((i = cur.read()) >= 0) || (!next())) return i;
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        int i;
        while (true) if (((i = cur.read(cbuf, off, len)) >= 0)
                || (!next())) return i;
    }

    public String readLine() throws IOException {
        String s;
        while (true) if (((s = cur.readLine()) != null) || (!next())) break;
        curLine++;
        return s;
    }

    public long skip(long n) throws IOException {
        return cur.skip(n);
    }

    public boolean ready() throws IOException {
        return cur.ready();
    }

    public boolean markSupported() {
        return cur.markSupported();
    }

    public void mark(int readAheadLimit) throws IOException {
        cur.mark(readAheadLimit);
    }

    public void reset() throws IOException {
        cur.reset();
    }

    public void close() throws IOException {
        cur.close();
        while (next()) cur.close();
    }
}
