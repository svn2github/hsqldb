/* Copyright (c) 2001-2002, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG, 
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb.lib;

import java.lang.reflect.*;
import java.util.Hashtable;

// fredt@users - patch 1.7.2 - added support for Object storage and row removal
// also changes so that no new object is created for each search.
// any Object can be stored but currently only String columns are searchable

/**
 * Provides a reflection-based abstraction of Java array objects, allowing
 * table-like access to and manipulation of both primitive and object array
 * types through a single interface.
 *
 * @author tony_lai@users
 * @version 1.7.2
 * @since 1.7.2
 */
public class UnifiedTable {

    private static Hashtable classCodeMap = new Hashtable(37, 1);

    // Define primitive class code using current hash code, for fast
    // searching for corresponding class comparator using a switch statement.
    static final int PRIM_CLASS_CODE_BYTE   = 101;
    static final int PRIM_CLASS_CODE_CHAR   = 102;
    static final int PRIM_CLASS_CODE_SHORT  = 103;
    static final int PRIM_CLASS_CODE_INT    = 104;
    static final int PRIM_CLASS_CODE_LONG   = 105;
    static final int PRIM_CLASS_CODE_FLOAT  = 106;
    static final int PRIM_CLASS_CODE_DOUBLE = 107;

    // Define Object class code using current hash code, for fast
    // searching for corresponding class comparator using a switch statement.
/*
    static final int OBJ_CLASS_CODE_BYTE = 201;
    static final int OBJ_CLASS_CODE_CHAR   = Character.class.hashCode();
    static final int OBJ_CLASS_CODE_SHORT  = Short.class.hashCode();
    static final int OBJ_CLASS_CODE_INT    = Integer.class.hashCode();
    static final int OBJ_CLASS_CODE_LONG   = Long.class.hashCode();
    static final int OBJ_CLASS_CODE_FLOAT  = Float.class.hashCode();
    static final int OBJ_CLASS_CODE_DOUBLE = Double.class.hashCode();
    static final int OBJ_CLASS_CODE_STRING = String.class.hashCode();
*/

// fredt
    static final int OBJ_CLASS_CODE_OBJECT = Object.class.hashCode();

    static {
        classCodeMap.put(byte.class, new Integer(PRIM_CLASS_CODE_BYTE));
        classCodeMap.put(char.class, new Integer(PRIM_CLASS_CODE_SHORT));
        classCodeMap.put(short.class, new Integer(PRIM_CLASS_CODE_SHORT));
        classCodeMap.put(int.class, new Integer(PRIM_CLASS_CODE_INT));
        classCodeMap.put(long.class, new Integer(PRIM_CLASS_CODE_LONG));
        classCodeMap.put(float.class, new Integer(PRIM_CLASS_CODE_FLOAT));
        classCodeMap.put(double.class, new Integer(PRIM_CLASS_CODE_DOUBLE));
/*
        classCodeMap.put(Byte.class, new Integer(OBJ_CLASS_CODE_BYTE));
        classCodeMap.put(Character.class, new Integer(OBJ_CLASS_CODE_SHORT));
        classCodeMap.put(Short.class, new Integer(OBJ_CLASS_CODE_SHORT));
        classCodeMap.put(Integer.class, new Integer(OBJ_CLASS_CODE_INT));
        classCodeMap.put(Long.class, new Integer(OBJ_CLASS_CODE_LONG));
        classCodeMap.put(Float.class, new Integer(OBJ_CLASS_CODE_FLOAT));
        classCodeMap.put(Double.class, new Integer(OBJ_CLASS_CODE_DOUBLE));
        classCodeMap.put(String.class, new Integer(OBJ_CLASS_CODE_STRING));
*/
        classCodeMap.put(Object.class,
                         new Integer(OBJ_CLASS_CODE_OBJECT));
    }

    protected SingleCellComparator getSingleCellComparator(int targetColumn) {

        switch (cellTypeCode) {

            case PRIM_CLASS_CODE_BYTE :
                return new PrimByteCellComparator(targetColumn);

            case PRIM_CLASS_CODE_CHAR :
                return new PrimCharCellComparator(targetColumn);

            case PRIM_CLASS_CODE_SHORT :
                return new PrimShortCellComparator(targetColumn);

            case PRIM_CLASS_CODE_INT :
                return new PrimIntCellComparator(targetColumn);

            case PRIM_CLASS_CODE_LONG :
                return new PrimLongCellComparator(targetColumn);

            case PRIM_CLASS_CODE_FLOAT :
                return new PrimFloatCellComparator(targetColumn);

            case PRIM_CLASS_CODE_DOUBLE :
                return new PrimDoubleCellComparator(targetColumn);

            default :
                return new PrimStringCellComparator(targetColumn);
        }
    }

    private Class         cellType;
    private int           cellTypeCode;
    private int           columns;
    private int           initRows;
    private int           growth;
    private Object        tableData;
    private int           rowAvailable = 0;
    private int           rowCount     = 0;
    private RowComparator rowComparator;
    private boolean       ascending;

    public UnifiedTable(Class cellType, int columns) {
        this(cellType, columns, 128);
    }

    public UnifiedTable(Class cellType, int columns, int initRows) {
        this(cellType, columns, initRows, 128);
    }

    public UnifiedTable(Class cellType, int columns, int initRows,
                        int growth) {

        this.cellType = cellType;
        cellTypeCode  = ((Integer) classCodeMap.get(cellType)).intValue();
        this.columns  = columns;
        this.growth   = growth;
        tableData     = Array.newInstance(cellType, initRows * columns);
        rowAvailable  = initRows;
    }

    public void addRow(Object rowData) {

        int dataIndex = makeRoom(rowCount, 1);

        System.arraycopy(rowData, 0, tableData, dataIndex, columns);
    }

// fredt@users - 20030109 - new method
    public void removeRow(int rowIndex) {
        makeRoom(rowIndex, -1);
    }

    public void clear() {
        rowCount = 0;
    }

    public void setCount(int count){
        rowCount = count;
    }

    public void setCell(int rowIndex, int colIndex, Object cellData) {
        Array.set(tableData, rowIndex * columns + colIndex, cellData);
    }

    public void setRow(int rowIndex, Object rowData) {
        System.arraycopy(rowData, 0, tableData, rowIndex * columns, columns);
    }

    public void moveRows(int fromIndex, int toIndex, int rows) {
        System.arraycopy(tableData, fromIndex * columns, tableData,
                         toIndex * columns, rows * columns);
    }

    public Object getRow(int rowIndex) {

        Object row = Array.newInstance(cellType, columns);

        System.arraycopy(tableData, rowIndex * columns, row, 0, columns);

        return row;
    }

    public void sort(int targetColumn, boolean ascending) {

        rowComparator  = getSingleCellComparator(targetColumn);
        this.ascending = ascending;

        fastQuickSort();
    }

    public void sort(int[] targetColumns, boolean ascending) {

        rowComparator  = new MultiCellsComparator(targetColumns);
        this.ascending = ascending;

        fastQuickSort();
    }

    public int search(byte value) {

        if (rowComparator == null) {
            throw new IllegalArgumentException("Table is not sorted");
        }

        try {
            ((PrimByteCellComparator) rowComparator).setSearchTarget(value);
        } catch (ClassCastException ccx) {
            throw new IllegalArgumentException("Invalid search target: "
                                               + value);
        }

        return binarySearch();
    }

    public int search(char value) {

        if (rowComparator == null) {
            throw new IllegalArgumentException("Table is not sorted");
        }

        try {
            ((PrimCharCellComparator) rowComparator).setSearchTarget(value);
        } catch (ClassCastException ccx) {
            throw new IllegalArgumentException("Invalid search target: "
                                               + value);
        }

        return binarySearch();
    }

    public int search(short value) {

        if (rowComparator == null) {
            throw new IllegalArgumentException("Table is not sorted");
        }

        try {
            ((PrimShortCellComparator) rowComparator).setSearchTarget(value);
        } catch (ClassCastException ccx) {
            throw new IllegalArgumentException("Invalid search target: "
                                               + value);
        }

        return binarySearch();
    }

    public int search(int value) {

        if (rowComparator == null) {
            throw new IllegalArgumentException("Table is not sorted");
        }

        try {
            ((PrimIntCellComparator) rowComparator).setSearchTarget(value);
        } catch (ClassCastException ccx) {
            throw new IllegalArgumentException("Invalid search target: "
                                               + value);
        }

        return binarySearch();
    }

    public int search(long value) {

        if (rowComparator == null) {
            throw new IllegalArgumentException("Table is not sorted");
        }

        try {
            ((PrimLongCellComparator) rowComparator).setSearchTarget(value);
        } catch (ClassCastException ccx) {
            throw new IllegalArgumentException("Invalid search target: "
                                               + value);
        }

        return binarySearch();
    }

    public int search(float value) {

        if (rowComparator == null) {
            throw new IllegalArgumentException("Table is not sorted");
        }

        try {
            ((PrimFloatCellComparator) rowComparator).setSearchTarget(value);
        } catch (ClassCastException ccx) {
            throw new IllegalArgumentException("Invalid search target: "
                                               + value);
        }

        return binarySearch();
    }

    public int search(double value) {

        if (rowComparator == null) {
            throw new IllegalArgumentException("Table is not sorted");
        }

        try {
            ((PrimDoubleCellComparator) rowComparator).setSearchTarget(value);
        } catch (ClassCastException ccx) {
            throw new IllegalArgumentException("Invalid search target: "
                                               + value);
        }

        return binarySearch();
    }

    // in JAVA 2 argument can be any Comparable object
    public int search(String value) {

        if (rowComparator == null) {
            throw new IllegalArgumentException("Table is not sorted");
        }

        try {
            rowComparator.setSearchTarget(value);
        } catch (ClassCastException ccx) {
            throw new IllegalArgumentException("Invalid search target: "
                                               + value);
        }

        return binarySearch();
    }

    /**
     * Swaps the values for row indexed i and j.
     */
    public void swap(int i, int j) {

        Object rowI = getRow(i);

        System.arraycopy(tableData, j * columns, tableData, i * columns,
                         columns);
        System.arraycopy(rowI, 0, tableData, j * columns, columns);
    }

    public Object getCell(int rowIndex, int colIndex) {
        return Array.get(tableData, rowIndex * columns + colIndex);
    }

    public byte getByteCell(int rowIndex, int colIndex) {
        return Array.getByte(tableData, rowIndex * columns + colIndex);
    }

    public char getCharCell(int rowIndex, int colIndex) {
        return Array.getChar(tableData, rowIndex * columns + colIndex);
    }

    public short getShortCell(int rowIndex, int colIndex) {
        return Array.getShort(tableData, rowIndex * columns + colIndex);
    }

    public int getIntCell(int rowIndex, int colIndex) {
        return Array.getInt(tableData, rowIndex * columns + colIndex);
    }

    public long getLongCell(int rowIndex, int colIndex) {
        return Array.getInt(tableData, rowIndex * columns + colIndex);
    }

    public float getFloatCell(int rowIndex, int colIndex) {
        return Array.getFloat(tableData, rowIndex * columns + colIndex);
    }

    public double getDoubleCell(int rowIndex, int colIndex) {
        return Array.getDouble(tableData, rowIndex * columns + colIndex);
    }

    public int size() {
        return rowCount;
    }

// fredt - support for removal as well as addition

    /**
     * Handles both addition and removal of rows
     */
    protected int makeRoom(int rowIndex, int rows) {

        int    newCount = rowCount + rows;
        Object data     = tableData;

        if (newCount > rowAvailable) {
            rowAvailable += growth;
            data = Array.newInstance(cellType, rowAvailable * columns);

            System.arraycopy(tableData, 0, data, 0, rowIndex * columns);
        }

        if (rowIndex < rowCount) {
            int source;
            int target;
            int size;

            if (rows >= 0) {
                source = rowIndex * columns;
                target = (rowIndex + rows) * columns;
                size   = (rowCount - rowIndex) * columns;
            } else {
                source = (rowIndex - rows) * columns;
                target = rowIndex * columns;
                size   = (rowCount - rowIndex + rows) * columns;
            }

            if (size > 0) {
                System.arraycopy(tableData, source, data, target, size);
            }

            // after removing rows, leave the phantom rows at the end
        }

        tableData = data;
        rowCount  = newCount;

        return rowIndex * columns;
    }

// fredt - patched - this actually compared the table[rowCount] column
    private int binarySearch() {

        int low  = 0;
        int high = rowCount;
        int mid  = 0;

// fredt - patched - changed from while (low <= high)
        while (low < high) {
            mid = (low + high) / 2;

            if (rowComparator.greaterThan(mid)) {

// fredt - patched - changed from high = mid -1
                high = mid;
            } else {
                if (rowComparator.lessThan(mid)) {
                    low = mid + 1;
                } else {
                    return mid;    // found
                }
            }
        }

        return -1;
    }

    private void fastQuickSort() {
        quickSort(0, size() - 1);
        insertionSort(0, size() - 1);
    }

    private void quickSort(int l, int r) {

        int M = 4;
        int i;
        int j;
        int v;

        if ((r - l) > M) {
            i = (r + l) / 2;

            if (lessThan(i, l)) {
                swap(l, i);    // Tri-Median Methode!
            }

            if (lessThan(r, l)) {
                swap(l, r);
            }

            if (lessThan(r, i)) {
                swap(i, r);
            }

            j = r - 1;

            swap(i, j);

            i = l;
            v = j;

            for (;;) {
                while (lessThan(++i, v));

                while (lessThan(v, --j));

                if (j < i) {
                    break;
                }

                swap(i, j);
            }

            swap(i, r - 1);
            quickSort(l, j);
            quickSort(i + 1, r);
        }
    }

    private void insertionSort(int lo0, int hi0) {

        int i;
        int j;

        for (i = lo0 + 1; i <= hi0; i++) {
            j = i;

            while ((j > lo0) && lessThan(i, j - 1)) {
                j--;
            }

            if (i != j) {
                Object row = getRow(i);

                moveRows(j, j + 1, i - j);
                setRow(j, row);
            }
        }
    }

    /**
     * Check if row indexed i is less than row indexed j
     */
    private boolean lessThan(int i, int j) {
        return ascending ? rowComparator.lessThan(i, j)
                         : rowComparator.lessThan(j, i);
    }

    /**
     * Check if targeted column value in the row indexed i is less than the
     * search target object.
     */
    private boolean lessThan(int i) {
        return ascending ? rowComparator.lessThan(i)
                         : rowComparator.greaterThan(i);
    }

    /**
     * Check if targeted column value in the row indexed i is greater than the
     * search target object.
     * @see setSearchTarget(Object)
     */
    private boolean greaterThan(int i) {
        return ascending ? rowComparator.greaterThan(i)
                         : rowComparator.lessThan(i);
    }

    interface RowComparator {

        /**
         * Check if row indexed i is less than row indexed j.
         */
        abstract boolean lessThan(int i, int j);

        /**
         * Check if targeted column value in the row indexed i is less than
         * the search target object.
         * @see setSearchTarget(Object)
         */
        abstract boolean lessThan(int i);

        /**
         * Check if targeted column value in the row indexed i is greater than
         * the search target object.
         * @see setSearchTarget(Object)
         */
        abstract boolean greaterThan(int i);

        /**
         * Sets the target object in a search operation.
         */
        abstract void setSearchTarget(Object target);
    }


    abstract class SingleCellComparator implements RowComparator {

        protected int targetColumn;

        SingleCellComparator(int targetColumn) {
            this.targetColumn = targetColumn;
        }
    }

    class PrimByteCellComparator extends SingleCellComparator {

        private byte[] myTableData;
        private byte   mySearchTarget;

        PrimByteCellComparator(int targetColumn) {

            super(targetColumn);

            myTableData = (byte[]) tableData;
        }

        /**
         * Check if row indexed i is less than row indexed j
         */
        public boolean lessThan(int i, int j) {
            return myTableData[i * columns + targetColumn]
                   < myTableData[j * columns + targetColumn];
        }

        /**
         * Check if targeted column value in the row indexed i is less than
         * the search target object.
         * @see setSearchTarget(Object)
         */
        public boolean lessThan(int i) {
            return myTableData[i * columns + targetColumn] < mySearchTarget;
        }

        /**
         * Check if targeted column value in the row indexed i is greater
         * than the search target object.
         * @see setSearchTarget(Object)
         */
        public boolean greaterThan(int i) {
            return myTableData[i * columns + targetColumn] > mySearchTarget;
        }

        /**
         * Sets the target object in a search operation.
         */
        public void setSearchTarget(Object target) {
            mySearchTarget = ((Number) target).byteValue();
        }

        /**
         * Sets the target object in a search operation.
         */
        public void setSearchTarget(byte target) {
            mySearchTarget = target;
        }
    }

    class PrimCharCellComparator extends SingleCellComparator {

        private char[] myTableData;
        private char   mySearchTarget;

        PrimCharCellComparator(int targetColumn) {

            super(targetColumn);

            myTableData = (char[]) tableData;
        }

        /**
         * Check if row indexed i is less than row indexed j
         */
        public boolean lessThan(int i, int j) {
            return myTableData[i * columns + targetColumn]
                   < myTableData[j * columns + targetColumn];
        }

        /**
         * Check if targeted column value in the row indexed i is less than
         * the search target object.
         * @see setSearchTarget(Object)
         */
        public boolean lessThan(int i) {
            return myTableData[i * columns + targetColumn] < mySearchTarget;
        }

        /**
         * Check if targeted column value in the row indexed i is greater
         * than the search target object.
         * @see setSearchTarget(Object)
         */
        public boolean greaterThan(int i) {
            return myTableData[i * columns + targetColumn] > mySearchTarget;
        }

        /**
         * Sets the target object in a search operation.
         */
        public void setSearchTarget(Object target) {
            mySearchTarget = (char) (((Number) target).intValue()
                                     & Character.MAX_VALUE);
        }

        /**
         * Sets the target object in a search operation.
         */
        public void setSearchTarget(char target) {
            mySearchTarget = target;
        }
    }

    class PrimShortCellComparator extends SingleCellComparator {

        private short[] myTableData;
        private short   mySearchTarget;

        PrimShortCellComparator(int targetColumn) {

            super(targetColumn);

            myTableData = (short[]) tableData;
        }

        /**
         * Check if row indexed i is less than row indexed j
         */
        public boolean lessThan(int i, int j) {
            return myTableData[i * columns + targetColumn]
                   < myTableData[j * columns + targetColumn];
        }

        /**
         * Check if targeted column value in the row indexed i is less than
         * the search target object.
         * @see setSearchTarget(Object)
         */
        public boolean lessThan(int i) {
            return myTableData[i * columns + targetColumn] < mySearchTarget;
        }

        /**
         * Check if targeted column value in the row indexed i is greater than
         * the search target object.
         * @see setSearchTarget(Object)
         */
        public boolean greaterThan(int i) {
            return myTableData[i * columns + targetColumn] > mySearchTarget;
        }

        /**
         * Sets the target object in a search operation.
         */
        public void setSearchTarget(Object target) {
            mySearchTarget = ((Number) target).shortValue();
        }

        /**
         * Sets the target object in a search operation.
         */
        public void setSearchTarget(short target) {
            mySearchTarget = target;
        }
    }

    class PrimIntCellComparator extends SingleCellComparator {

        private int[] myTableData;
        private int   mySearchTarget;

        PrimIntCellComparator(int targetColumn) {

            super(targetColumn);

            myTableData = (int[]) tableData;
        }

        /**
         * Check if row indexed i is less than row indexed j
         */
        public boolean lessThan(int i, int j) {
            return myTableData[i * columns + targetColumn]
                   < myTableData[j * columns + targetColumn];
        }

        /**
         * Check if targeted column value in the row indexed i is less than
         * the search target object.
         * @see setSearchTarget(Object)
         */
        public boolean lessThan(int i) {
            return myTableData[i * columns + targetColumn] < mySearchTarget;
        }

        /**
         * Check if targeted column value in the row indexed i is greater than
         * the search target object.
         * @see setSearchTarget(Object)
         */
        public boolean greaterThan(int i) {
            return myTableData[i * columns + targetColumn] > mySearchTarget;
        }

        /**
         * Sets the target object in a search operation.
         */
        public void setSearchTarget(Object target) {
            mySearchTarget = ((Number) target).intValue();
        }

        /**
         * Sets the target object in a search operation.
         */
        public void setSearchTarget(int target) {
            mySearchTarget = target;
        }
    }

    class PrimLongCellComparator extends SingleCellComparator {

        private long[] myTableData;
        private long   mySearchTarget;

        PrimLongCellComparator(int targetColumn) {

            super(targetColumn);

            myTableData = (long[]) tableData;
        }

        /**
         * Check if row indexed i is less than row indexed j
         */
        public boolean lessThan(int i, int j) {
            return myTableData[i * columns + targetColumn]
                   < myTableData[j * columns + targetColumn];
        }

        /**
         * Check if targeted column value in the row indexed i is less than
         * the search target object.
         * @see setSearchTarget(Object)
         */
        public boolean lessThan(int i) {
            return myTableData[i * columns + targetColumn] < mySearchTarget;
        }

        /**
         * Check if targeted column value in the row indexed i is greater than
         * the search target object.
         * @see setSearchTarget(Object)
         */
        public boolean greaterThan(int i) {
            return myTableData[i * columns + targetColumn] > mySearchTarget;
        }

        /**
         * Sets the target object in a search operation.
         */
        public void setSearchTarget(Object target) {
            mySearchTarget = ((Number) target).longValue();
        }

        /**
         * Sets the target object in a search operation.
         */
        public void setSearchTarget(long target) {
            mySearchTarget = target;
        }
    }

    class PrimFloatCellComparator extends SingleCellComparator {

        private float[] myTableData;
        private float   mySearchTarget;

        PrimFloatCellComparator(int targetColumn) {

            super(targetColumn);

            myTableData = (float[]) tableData;
        }

        /**
         * Check if row indexed i is less than row indexed j
         */
        public boolean lessThan(int i, int j) {
            return myTableData[i * columns + targetColumn]
                   < myTableData[j * columns + targetColumn];
        }

        /**
         * Check if targeted column value in the row indexed i is less than
         * the search target object.
         * @see setSearchTarget(Object)
         */
        public boolean lessThan(int i) {
            return myTableData[i * columns + targetColumn] < mySearchTarget;
        }

        /**
         * Check if targeted column value in the row indexed i is greater than
         * the search target object.
         * @see setSearchTarget(Object)
         */
        public boolean greaterThan(int i) {
            return myTableData[i * columns + targetColumn] > mySearchTarget;
        }

        /**
         * Sets the target object in a search operation.
         */
        public void setSearchTarget(Object target) {
            mySearchTarget = ((Number) target).floatValue();
        }

        /**
         * Sets the target object in a search operation.
         */
        public void setSearchTarget(float target) {
            mySearchTarget = target;
        }
    }

    class PrimDoubleCellComparator extends SingleCellComparator {

        private double[] myTableData;
        private double   mySearchTarget;

        PrimDoubleCellComparator(int targetColumn) {

            super(targetColumn);

            myTableData = (double[]) tableData;
        }

        /**
         * Check if row indexed i is less than row indexed j
         */
        public boolean lessThan(int i, int j) {
            return myTableData[i * columns + targetColumn]
                   < myTableData[j * columns + targetColumn];
        }

        /**
         * Check if targeted column value in the row indexed i is less than
         * the search target object.
         * @see setSearchTarget(Object)
         */
        public boolean lessThan(int i) {
            return myTableData[i * columns + targetColumn] < mySearchTarget;
        }

        /**
         * Check if targeted column value in the row indexed i is greater than
         * the search target object.
         * @see setSearchTarget(Object)
         */
        public boolean greaterThan(int i) {
            return myTableData[i * columns + targetColumn] > mySearchTarget;
        }

        /**
         * Sets the target object in a search operation.
         */
        public void setSearchTarget(Object target) {
            mySearchTarget = ((Number) target).doubleValue();
        }

        /**
         * Sets the target object in a search operation.
         */
        public void setSearchTarget(double target) {
            mySearchTarget = target;
        }
    }

    class PrimStringCellComparator extends SingleCellComparator {

        private Object[]   myTableData;
        private String mySearchTarget;

        PrimStringCellComparator(int targetColumn) {

            super(targetColumn);

            myTableData = (Object[]) tableData;
        }

        /**
         * Check if row indexed i is less than row indexed j
         */
        public boolean lessThan(int i, int j) {
            return compare(
                (String) myTableData[i * columns + targetColumn], (String) myTableData[j * columns + targetColumn]) < 0;
        }

        /**
         * Check if targeted column value in the row indexed i is less than
         * the search target object.
         * @see setSearchTarget(Object)
         */
        public boolean lessThan(int i) {
            return compare(
                (String) myTableData[i * columns + targetColumn], mySearchTarget) < 0;
        }

        /**
         * Check if targeted column value in the row indexed i is greater
         * than the search target object.
         * @see setSearchTarget(Object)
         */
        public boolean greaterThan(int i) {
            return compare(
                (String) myTableData[i * columns + targetColumn], mySearchTarget) > 0;
        }

        private int compare(String a, String b) {

            if (a == b) {
                return 0;
            }

            // null==null and smaller than any value
            if (a == null) {
                if (b == null) {
                    return 0;
                }

                return -1;
            }

            if (b == null) {
                return 1;
            }

            return a.compareTo(b);
        }

        /**
         * Sets the target object in a search operation.
         */
        public void setSearchTarget(Object target) {
            mySearchTarget = (String) target;
        }
    }

    class MultiCellsComparator implements RowComparator {

        private SingleCellComparator[] cellComparators;

        MultiCellsComparator(int[] targetColumns) {

            cellComparators = new SingleCellComparator[targetColumns.length];

            for (int i = 0; i < targetColumns.length; i++) {
                cellComparators[i] =
                    getSingleCellComparator(targetColumns[i]);
            }
        }

        /**
         * Check if row indexed i is less than row indexed j
         */
        public boolean lessThan(int i, int j) {

            for (int c = 0; c < cellComparators.length; c++) {
                if (cellComparators[c].lessThan(i, j)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Check if targeted column value in the row indexed i is less than
         * the search target object.
         * @see setSearchTarget(Object)
         */
        public boolean lessThan(int i) {

            for (int c = 0; c < cellComparators.length; c++) {
                if (cellComparators[c].lessThan(i)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Check if targeted column value in the row indexed i is greater than
         * the search target object.
         * @see setSearchTarget(Object)
         */
        public boolean greaterThan(int i) {

            for (int c = 0; c < cellComparators.length; c++) {
                if (cellComparators[c].greaterThan(i)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Sets the target object in a search operation.
         */
        public void setSearchTarget(Object targets) {

            for (int c = 0; c < cellComparators.length; c++) {
                cellComparators[c].setSearchTarget(Array.get(targets, c));
            }
        }
    }

//@@@ Testing methods.
    static int[] outputRow(int index, int[] row) {

//        System.out.println("Row: "+index+" column 1="+row[0]+" column 2="+row[1]);
        return row;
    }

    public static void main(String[] args) {

        StopWatch sw = new StopWatch();

        sw.start();

        UnifiedTable table = new UnifiedTable(int.class, 2, 0x20000, 0x20000);

        for (int i = 0; i < 0x100000; i++) {
            table.addRow(outputRow(i, new int[] {
                (int) (Math.random() * Integer.MAX_VALUE),
                (int) (Math.random() * Integer.MAX_VALUE)
            }));
        }

        System.out.println("Create time: " + sw.elapsedTime());

        int size = table.size();

/*
                for(int i=0; i<size; i++) {
//            outputRow(i, (int[])table.getRow(i));
                        System.out.println("Row: "+i+
                                " column 1="+table.getIntCell(i, 0)+
                                " column 2="+table.getCell(i, 1));
//                " column 2="+table.getCell(i, 1)+" class="+table.getCell(i, 1).getClass());
                }
*/
        sw.zero();
        table.sort(0, true);
        System.out.println("Sort time: " + sw.elapsedTime() + " size: "
                           + size);
        sw.zero();

        for (int i = 1; i < size; i++) {

//            outputRow(i, (int[])table.getRow(i));
            if (table.getIntCell(i - 1, 0) > table.getIntCell(i, 0)) {
                System.out.println("Sort failed on Row: " + i + " column 1="
                                   + table.getIntCell(i, 0) + " column 2="
                                   + table.getCell(i, 1));
            }

//                    " column 2="+table.getCell(i, 1)+" class="+table.getCell(i, 1).getClass());
        }

        System.out.println("Access time: " + sw.elapsedTime() + " size: "
                           + size);
        sw.zero();

        for (int i = 1; i < size; i++) {
            int targetRow   = (int) (Math.random() * (size - 1));
            int targetValue = table.getIntCell(targetRow, 0);
            int resultValue = table.getIntCell(table.search(targetValue), 0);

            if (targetValue != resultValue) {
                System.out.println("Search failed on Row: " + targetRow
                                   + " column 1="
                                   + table.getIntCell(targetRow, 0)
                                   + " column 2="
                                   + table.getCell(targetRow, 1));
            }
        }

        System.out.println("Search time: " + sw.elapsedTime() + " size: "
                           + size);
    }
}
