#region licence

/* Copyright (c) 2001-2009, The HSQL Development Group
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

#endregion

using System;
using System.Collections.Generic;
using System.Text;
using System.Windows.Forms;
using System.Collections;

namespace System.Data.Hsqldb.Client.Design
{
    /// <summary>
    /// 
    /// </summary>
    public static class PropertyGridHelper
    {
        /// <summary>
        /// Causes the property grid item with the
        /// given label (if found) to become selected.
        /// </summary>
        /// <param name="grid">The grid.</param>
        /// <param name="label">The label.</param>
        /// <returns>
        /// <c>true</c> if a property with the given label
        /// was found; else <c>false</c>
        /// </returns>
        public static bool SelectItem(
            PropertyGrid grid,
            string label)
        {
            GridItem item = FindItem(grid, label);

            bool found = (item != null);

            if (found)
            {
                grid.SelectedGridItem = item;
            }

            return found;
        }

        /// <summary>
        /// Finds an item having the given label.
        /// </summary>
        /// <param name="grid">The grid.</param>
        /// <param name="label">The label.</param>
        /// <returns>
        /// an item having the given label;
        /// <c>null</c> if no such item was found
        /// </returns>
        public static GridItem FindItem(
            PropertyGrid grid, 
            string label)
        {
            if (grid == null || label == null)
            {
                return null;
            }

            GridItem root = grid.SelectedGridItem;

            if (root == null)
            {
                return null;
            }

            while (root.Parent != null)
            {
                root = root.Parent;
            }

            Queue<GridItem> items = new Queue<GridItem>();

            items.Enqueue(root);

            while (items.Count > 0)
            {
                GridItem current = items.Dequeue();

                if (current.Label == label)
                {
                    return current;
                }

                foreach (GridItem item in current.GridItems)
                {
                    items.Enqueue(item);
                }
            }

            return null;
        }
    }
}
