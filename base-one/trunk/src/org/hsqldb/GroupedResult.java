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


package org.hsqldb;

import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HashMap;

/**
 * This class is used for grouping select results, especially for select
 * statements that include group by clause and nested aggregate functions.
 * It is used by the <b>Select</b> class regardless the existence of group by
 * clause.
 * <p>
 * When a group by clause is defined, a <b>ResultGroup</b> is used to hold
 * all column values and <b>AggregatingValue</b>s for each group.  When a group
 * by clause is not defined, one <b>ResultGroup</b> is used to hold all the
 * results.
 * All <b>ResultGroup</b>s are placed in a <b>Hashtable</b>.  Adding a new row
 * will first retrieve the corresponding group from the table, based on the
 * values in the group by columns.  If a group is found, then the row
 * associated with the group will be returned.  Otherwise a new group is
 * created with the new row, and the new row is returned.
 * <p>
 * The <b>Select</b> can then update the values and <b>AggregatingValue</b>s
 * in the returned row, rather than the original row.  This approach enables
 * nested aggregate functions, such as "count(id)+2, 20-count(id),
 * max(id)-min(id)" support.
 *
 * @author  Tony Lai
 * @see     Expression
 * @see     Select
 * @version   1.7.2
 * @since   1.7.2
 */
class GroupedResult {

    Select        select;
    HsqlArrayList results = new HsqlArrayList();
    int           groupBegin;
    int           groupEnd;
    boolean       isGrouped;
    HashMap       groups = new HashMap();
    ResultGroup   currGroup;

    GroupedResult(Select select, Result result) {

        this.select = select;

//        this.result = result;
        groupBegin = select.iResultLen;
        groupEnd   = groupBegin + select.iGroupLen;
        isGrouped  = groupBegin != groupEnd;
    }

    Object[] addRow(Object[] row) {

        if (isGrouped) {
            ResultGroup newGroup = new ResultGroup(row);

            currGroup = (ResultGroup) groups.get(newGroup);

            if (currGroup == null) {
                currGroup = newGroup;

                groups.put(currGroup, currGroup);
                results.add(row);
            }
        } else if (currGroup == null) {
            currGroup = new ResultGroup(row);

            groups.put(currGroup, currGroup);
            results.add(row);
        } else if (!select.isAggregated) {
            results.add(row);

            currGroup.row = row;
        }

        return currGroup.row;
    }

/*
    int getRowCount0() {
        return results.size();
    }
*/
    class ResultGroup {

        Object[] row;
        int      hashCode;

        public ResultGroup(Object[] row) {

            this.row = row;
            hashCode = 0;

            for (int i = groupBegin; i < groupEnd; i++) {
                if (row[i] != null) {
                    hashCode += row[i].hashCode();
                }
            }
        }

        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object obj) {

            if (obj == null) {
                return false;
            }

            if (obj == this) {
                return true;
            }

            ResultGroup group = (ResultGroup) obj;

            for (int i = groupBegin; i < groupEnd; i++) {
                if (!equals(row[i], group.row[i])) {
                    return false;
                }
            }

            return true;
        }

        public Object getResult(int index) {
            return row[index];
        }

        public void setResult(int index, Object result) {
            row[index] = result;
        }

        private boolean equals(Object o1, Object o2) {
            return (o1 == null) ? o2 == null
                                : o1.equals(o2);
        }
    }
}
