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
