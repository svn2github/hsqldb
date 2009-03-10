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

#region Using

using System;
using System.Collections;
using System.Collections.Generic;
using System.Data;
using System.Data.Common;
using System.Globalization;
using System.ComponentModel;

#endregion

namespace System.Data.Hsqldb.Client
{
    /// <summary>
    /// <para>
    /// The HSQLDB <see cref="DbParameterCollection">DbParameterCollection</see>
    /// implementation.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Client.HsqlParameterCollection.png"
    ///      alt="HsqlParameterCollection Class Diagram"/>
    /// </summary>
    /// <seealso cref="HsqlParameter"/>
    /// <seealso cref="HsqlCommand"/>
    [Serializable]
    [Editor("Microsoft.VSDesigner.Data.Design.DBParametersEditor, Microsoft.VSDesigner, Version=8.0.0.0, Culture=neutral, PublicKeyToken=b03f5f7f11d50a3a", "System.Drawing.Design.UITypeEditor, System.Drawing, Version=2.0.0.0, Culture=neutral, PublicKeyToken=b03f5f7f11d50a3a")]
    [ListBindable(false)]
    public sealed class HsqlParameterCollection
        : DbParameterCollection, 
        ICloneable,
        IEnumerable<HsqlParameter>,
        ICollection<HsqlParameter>,
        IList<HsqlParameter>
    {

        #region Fields

        private static readonly Type ItemType;
        private List<HsqlParameter> m_parameters;        
        internal bool m_isDirty;

        #endregion

        #region Constructors

        /// <summary>
        /// Initializes the <see cref="HsqlParameterCollection"/> class.
        /// </summary>
        static HsqlParameterCollection()
        {
            ItemType = typeof(HsqlParameter);
        }

        #region HsqlParameterCollection()

        /// <summary>
        /// Initializes a new, initially empty instance of the
        /// <see cref="HsqlParameterCollection"/> class.
        /// </summary>
        internal HsqlParameterCollection()
        {
            m_parameters = new List<HsqlParameter>();
        }

        #endregion

        #region HsqlParameterCollection(HsqlParameterCollection)

        /// <summary>
        /// Initializes a new instance of the
        /// <see cref="HsqlParameterCollection"/> class that is a copy
        /// of the given collection.
        /// </summary>
        /// <param name="from">The collection to copy</param>
        internal HsqlParameterCollection(HsqlParameterCollection from)
            : this()
        {
            int count = from.Count;
            m_parameters = new List<HsqlParameter>(count);

            for(int i = 0; i < count; i++)
            {
                SetParameter(i, from[i].Clone());
            }

        }

        #endregion

        #endregion

        #region DbParameterCollection Member Overrides

        #region SetParameter(int,DbParameter)

        /// <summary>
        /// Sets the <see cref="HsqlParameter"/>
        /// object at the specified index to a new value.
        /// </summary>
        /// <param name="index">
        /// The index where the <see cref="HsqlParameter"/>
        /// object is located.</param>
        /// <param name="value">
        /// The new <see cref="HsqlParameter"/> value.
        /// </param>
        protected override void SetParameter(int index, DbParameter value)
        {
            ReplaceParameter(index, value);
        }

        #endregion

        #region SetParameter(string,DbParameter)

        /// <summary>
        /// Sets the <see cref="HsqlParameter" />
        /// object with the specified name to a new value.
        /// </summary>
        /// <param name="parameterName">
        /// The name of the <see cref="HsqlParameter" />
        /// object in the collection.
        /// </param>
        /// <param name="value">
        /// The new <see cref="HsqlParameter" />
        /// value.</param>
        protected override void SetParameter(
            string parameterName,
            DbParameter value)
        {
            ReplaceParameter(IndexOf(parameterName), value);
        }

        #endregion

        #region RemoveAt(int)

        /// <summary>
        /// Removes the <see cref="HsqlParameter"/>
        /// object at the specified <c>index</c> from this collection.
        /// </summary>
        /// <param name="index">
        /// The index where the <see cref="DbParameter"/> object is located.
        /// </param>
        public override void RemoveAt(int index)
        {
            RemoveIndex(index);

            m_isDirty = true;
        }

        #endregion

        #region RemoveAt(string)

        /// <summary>
        /// Removes the <see cref="DbParameter"/> object with the
        /// specified name from this collection.
        /// </summary>
        /// <param name="parameterName">
        /// The name of the <see cref="DbParameter"/>
        /// object to remove.
        /// </param>
        public override void RemoveAt(string parameterName)
        {
            RemoveIndex(IndexOf(parameterName));

            m_isDirty = true;
        }

        #endregion

        #region Remove(object)

        /// <summary>
        /// Removes the specified <see cref="DbParameter"/>
        /// object from this collection.
        /// </summary>
        /// <param name="value">
        /// The <see cref="DbParameter"/> object to remove.</param>
        public override void Remove(object value)
        {
            int index = IndexOf(value);

            if (index >= 0)
            {
                RemoveIndex(index);
            }
            else if (this != ((HsqlParameter)value)
                .CompareExchangeParent(null, this))
            {
                throw new ArgumentException(
                    "Attempt to remove parameter that is not"
                    + " contained in this collection",
                    "value");
            }

            m_isDirty = true;
        }

        #endregion

        #region Insert(int, object)

        /// <summary>
        /// Inserts the specified <see cref="DbParameter"/>
        /// object into thi collection at the specified index.
        /// </summary>
        /// <param name="index">
        /// The index at which to insert the
        /// <see cref="DbParameter"/> object.
        /// </param>
        /// <param name="value">
        /// The <see cref="DbParameter"/> object
        /// to insert into the collection.
        /// </param>
        public override void Insert(int index, object value)
        {
            ValidateParameter(-1, value);

            m_parameters.Insert(index, (HsqlParameter)value);

            m_isDirty = true;
        }

        #endregion

        #region IndexOf(string)

        /// <summary>
        /// Returns the index of the <see cref="DbParameter"/>
        /// object with the specified name.
        /// </summary>
        /// <param name="parameterName">
        /// The name of the <see cref="DbParameter"/>
        /// object in the collection.
        /// </param>
        /// <returns>
        /// The index of the <see cref="DbParameter"/>
        /// object with the specified name.
        /// </returns>
        public override int IndexOf(string parameterName)
        {
            int count = m_parameters.Count;

            if (count == 0)
            {
                return -1;
            }

            CompareInfo ci = CultureInfo.CurrentCulture.CompareInfo;
            CompareOptions co = CompareOptions.IgnoreWidth
                                | CompareOptions.IgnoreKanaType
                                | CompareOptions.IgnoreCase;

            for (int i = 0; i < count; i++)
            {
                string targetName = m_parameters[i].ParameterName;

                if ((targetName == parameterName)
                    || (0 == ci.Compare(targetName, parameterName, co)))
                {
                    return i;
                }
            }

            return -1;
        }

        #endregion

        #region IndexOf(object)

        /// <summary>
        /// Returns the index of the given <see cref="HsqlParameter"/>.
        /// </summary>
        /// <param name="value">
        /// The <see cref="HsqlParameter"/> for which to search.
        /// </param>
        /// <returns>
        /// The index of the <see cref="HsqlParameter"/>.
        /// </returns>
        public override int IndexOf(object value)
        {
            HsqlParameter parameter = value as HsqlParameter;

            return (parameter == null)
                ? -1
                : m_parameters.IndexOf(parameter);
        }

        #endregion

        #region GetParameter(int)

        /// <summary>
        /// Returns the <see cref="DbParameter"/>
        /// object at the specified index in the collection.
        /// </summary>
        /// <param name="index">
        /// The index of the <see cref="DbParameter"/>
        /// in the collection.
        /// </param>
        /// <returns>
        /// The <see cref="DbParameter"/>
        /// object at the specified index in the collection.
        /// </returns>
        protected override DbParameter GetParameter(int index)
        {
            return m_parameters[index];
        }

        #endregion
        
        #region GetParameter(string)

        /// <summary>
        /// Returns the <see cref="HsqlParameter"/> with the specified name.
        /// </summary>
        /// <param name="parameterName">
        /// The name of the <see cref="HsqlParameter"/>.
        /// </param>
        /// <returns>
        /// The <see cref="HsqlParameter"/> with the specified name.
        /// </returns>
        protected override DbParameter GetParameter(string parameterName)
        {
            int index = IndexOf(parameterName);

            return (index >= 0)
                ? GetParameter(index)
                : null;
        }

        #endregion

        #region GetEnumerator()

        /// <summary>
        /// Exposes the <see cref="IEnumerable.GetEnumerator"/> method,
        /// which supports a simple iteration over this parameter collection.
        /// </summary>
        /// <returns>
        /// An <see cref="IEnumerator"/> that can be used to iterate
        /// through this collection.
        /// </returns>
        public override IEnumerator GetEnumerator()
        {
            return m_parameters.GetEnumerator();
        }

        IEnumerator<HsqlParameter> IEnumerable<HsqlParameter>.GetEnumerator()
        {
            return m_parameters.GetEnumerator();
        }

        #endregion

        #region Clear()

        // avoid the 
        private static void ResetParent(HsqlParameter p)
        {
            p.m_parent = null;
        }

        /// <summary>
        /// Removes all <see cref="DbParameter"/>
        /// values from this <see cref="DbParameterCollection"/>.
        /// </summary>
        public override void Clear()
        {
            List<HsqlParameter> parameters = m_parameters;

            if (parameters.Count == 0)
            {
                return;
            }

            parameters.ForEach(ResetParent);
            parameters.Clear();

            m_isDirty = true;
        }

        #endregion

        #region CopyTo(Array,int)

        /// <summary>
        /// Copies the elements of this <see cref="DbParameterCollection"/>
        /// to the specified one-dimensional <see cref="System.Array"/>,
        /// starting at the specified destination index.
        /// </summary>
        /// <param name="array">
        /// The one-dimensional <see cref="System.Array"/> that is
        /// the destination of the elements copied from this
        /// <see cref="DbParameterCollection"/>
        /// </param>
        /// <param name="index">
        /// The starting index into the destination
        /// <see cref="System.Array"/>.
        /// </param>
        public override void CopyTo(Array array, int index)
        {
            m_parameters.CopyTo((HsqlParameter[])array, index);
        }

        #endregion

        #region Contains(object)

        /// <summary>
        /// Determines whether the specified object
        /// exists in this collection.
        /// </summary>
        /// <returns>
        /// <c>true</c> if the collection contains the specified object;
        /// otherwise, <c>false</c>.
        /// </returns>
        /// <param name="value">
        /// The object to find.
        /// </param>
        public override bool Contains(object value)
        {
            return (IndexOf(value) >= 0);
        }

        #endregion

        #region Contains(string)

        /// <summary>
        /// Indicates whether a <see cref="DbParameter"/> with
        /// the specified name exists in this collection.
        /// </summary>
        /// <param name="value">
        /// The name of the <see cref="DbParameter"/>
        /// to look for in this collection.</param>
        /// <returns>
        /// <c>true</c> if the <see cref="DbParameter"></see>
        /// is in this collection; otherwise <c>false</c>.
        /// </returns>
        public override bool Contains(string value)
        {
            return IndexOf(value) >= 0;
        }

        #endregion

        #region AddRange(Array)

        /// <summary>
        /// Adds an array of <see cref="HsqlParameter"/> values to
        /// this <see cref="HsqlParameterCollection"/>.
        /// </summary>
        /// <param name="values">
        /// An array <see cref="HsqlParameter"/> values to
        /// add to this collection.
        /// </param>
        /// <exception cref="InvalidCastException">
        /// When the specified array contains at least one object
        /// that is not an <see cref="HsqlParameter"/>.
        /// </exception>
        /// <exception cref="ArgumentNullException">
        /// When the specified array contains at least one object
        /// that is <c>null</c>.
        /// </exception>
        /// <exception cref="ArgumentException">
        /// When the specified array contains at least one object that
        /// already belongs to this or  another 
        /// <see cref="HsqlParameterCollection"/>.
        /// </exception>
        public override void AddRange(Array values)
        {
            if (values == null)
            {
                throw new ArgumentNullException(
                    "values");
            }

            foreach (object obj in values)
            {
                ValidateParameterType(obj);
            }

            int count = 0;

            foreach (HsqlParameter value in values)
            {
                ValidateParameter(-1, value);

                m_parameters.Add(value);

                count++;
            }

            m_isDirty = (count > 0);
        }

        #endregion

        #region AddRange(HsqlParameter[])
        /// <summary>
        /// Adds an array of <see cref="HsqlParameter"/> objects to
        /// this <see cref="HsqlParameterCollection"/>.
        /// </summary>
        /// <param name="values">
        /// The objects to add.
        /// </param>
        /// <exception cref="ArgumentNullException">
        /// When the specified array contains at least one object
        /// that is <c>null</c>.
        /// </exception>
        /// <exception cref="ArgumentException">
        /// When the specified array contains at least one object that
        /// already belongs to this or  another 
        /// <see cref="HsqlParameterCollection"/>.
        /// </exception>
        public void AddRange(HsqlParameter[] values)
        {
            if (values == null)
            {
                throw new ArgumentNullException(
                    "values");
            }

            int count = 0;

            foreach (HsqlParameter value in values)
            {
                ValidateParameter(-1, value);

                m_parameters.Add(value);

                count++;
            }

            m_isDirty = (count > 0);
        } 
        #endregion

        #region Add(object)
        /// <summary>
        /// Adds the specified object to this collection.
        /// </summary>
        /// <param name="value">
        /// The object to add to this collection.
        /// </param>
        /// <returns>
        /// The index of the object in this collection.
        /// </returns>
        /// <exception cref="InvalidCastException">
        /// When the specified object is not an <see cref="HsqlParameter"/>.
        /// </exception>
        /// <exception cref="ArgumentNullException">
        /// When the specified object is <c>null</c>.
        /// </exception>
        /// <exception cref="ArgumentException">
        /// When the specified object already belongs to this or 
        /// another <see cref="HsqlParameterCollection"/>.
        /// </exception> 
        [EditorBrowsable(EditorBrowsableState.Never)]
        public override int Add(object value)
        {
            ValidateParameterType(value);
            ValidateParameter(-1, value);
            m_parameters.Add((HsqlParameter)value);

            m_isDirty = true;

            return (m_parameters.Count - 1);
        } 
        #endregion

        #region Add(HsqlParameter)
        /// <summary>
        /// Adds the specified parameter object to this collection.
        /// </summary>
        /// <returns>
        /// The specified parameter object.
        /// </returns>
        /// <param name="value">
        /// The parameter to add to the collection.
        /// </param>
        /// <exception cref="ArgumentNullException">
        /// When the specified parameter object is <c>null</c>.
        /// </exception>
        /// <exception cref="ArgumentException">
        /// When the specified parameter object already belongs to this or 
        /// another <see cref="HsqlParameterCollection"/>.
        /// </exception> 
        public HsqlParameter Add(HsqlParameter value)
        {
            this.Add((object)value);

            return value;
        } 
        #endregion

        #region SyncRoot

        /// <summary>
        /// Specifies the <see cref="System.Object"/> that can
        /// be used to synchronize access to this collection.
        /// </summary>
        /// <value>
        /// The <see cref="System.Object"/> that can be used to
        /// synchronize access to this
        /// <see cref="HsqlParameterCollection"/>.
        /// </value>
        public override object SyncRoot {
            get { return ((IList)m_parameters).SyncRoot; }
        }

        #endregion

        #region IsSynchronized

        /// <summary>
        /// Specifies whether this collection is synchronized.
        /// </summary>
        /// <value>
        /// <c>true</c> if this collection is synchronized;
        /// otherwise <c>false</c>.
        /// </value>
        public override bool IsSynchronized {
            get { return ((IList)m_parameters).IsSynchronized; }
        }

        #endregion

        #region IsReadOnly

        /// <summary>
        /// Specifies whether this collection is read-only.
        /// </summary>
        /// <value>
        /// <c>true</c> if the collection is read-only;
        /// otherwise <c>false</c>.
        /// </value>
        public override bool IsReadOnly {
            get { return ((IList)m_parameters).IsReadOnly; }
        }

        #endregion

        #region IsFixedSize

        /// <summary>
        /// Specifies whether this collection is of fixed size.
        /// </summary>
        /// <value>
        /// <c>true</c> if the collection is a fixed size;
        /// otherwise <c>false</c>.
        /// </value>
        public override bool IsFixedSize {
            get { return ((IList)m_parameters).IsFixedSize; }
        }

        #endregion

        #region Count

        /// <summary>
        /// Specifies the number of items in this collection.
        /// </summary>
        /// <value>
        /// The number of items in this collection.
        /// </value>
        public override int Count {
            get { return m_parameters.Count; }
        }

        #endregion

        #region this[int]
        /// <summary>
        /// Gets or sets the <see cref="HsqlParameter"/> at the specified index.
        /// </summary>
        /// <value>
        /// The <see cref="HsqlParameter"/> at the specified index
        /// </value>
        public new HsqlParameter this[int index]
        {
            get { return base[index] as HsqlParameter; }
            set { base[index] = value; }
        }
        #endregion

        #region this[string]
        /// <summary>
        /// Gets or sets the <see cref="HsqlParameter"/> with the specified parameter name.
        /// </summary>
        /// <value>
        /// The <see cref="HsqlParameter"/> with the specified parameter name.
        /// </value>
        public new HsqlParameter this[string parameterName]
        {
            get { return base[parameterName] as HsqlParameter; }
            set { base[parameterName] = value; }
        }
        #endregion

        #endregion DbParameterCollection Member Overrides

        #region Private Helper Members

        #region RemoveIndex(int)
        /// <summary>
        /// Removes the parameter object at the given index.
        /// </summary>
        /// <param name="index">The index.</param>
        private void RemoveIndex(int index)
        {
            List<HsqlParameter> parameters = m_parameters;
            HsqlParameter parameter = parameters[index];

            parameters.RemoveAt(index);

            parameter.m_parent = null;
        }
        #endregion

        #region ReplaceParameter(int, object)
        /// <summary>
        /// Replaces the parameter object at the given index
        /// with the given parameter object.
        /// </summary>
        /// <param name="index">The index.</param>
        /// <param name="newValue">The new value.</param>
        private void ReplaceParameter(int index, object newValue)
        {
            ValidateParameter(index, newValue);

            List<HsqlParameter> parameters = m_parameters;
            HsqlParameter oldValue = parameters[index];

            parameters[index] = (HsqlParameter)newValue;
            oldValue.m_parent = null;

            m_isDirty = true;
        }
        #endregion

        #region ValidateParameterType(object)
        /// <summary>
        /// Validates the type of the given parameter object.
        /// </summary>
        /// <param name="value">The parameter object.</param>
        /// <exception cref="InvalidCastException">
        /// When the given parameter object is not an <see cref="HsqlParameter"/>.
        /// </exception>
        /// <exception cref="ArgumentNullException">
        /// When the given parameter object is <c>null</c>.
        /// </exception>       
        private void ValidateParameterType(object value)
        {
            if (value == null)
            {
                throw new ArgumentNullException(
                    "value",
                    "The HsqlParameterCollection only accepts non-null"
                    + " HsqlParameter objects.");
            }

            if (!ItemType.IsInstanceOfType(value))
            {
                throw new InvalidCastException(
                    string.Format(
                    "The HsqlParameterCollection only accepts non-null"
                    + " HsqlParameter objects, not {0} objects.",
                    value.GetType()));
            }
        }
        #endregion

        #region ValidateParameter(int, object)
        /// <summary>
        /// Validates the given parameter object.
        /// </summary>
        /// <param name="index">
        /// The index of the given parameter object in this collection; use <c>-1</c>
        /// to indicate that it does not yet belong.
        /// </param>
        /// <param name="value">
        /// The parameter object to validate.
        /// </param>
        /// <exception cref="ArgumentException">
        /// When the given parameter object already belongs to another collection or
        /// exists in this collection at a different index.
        /// </exception>        
        private void ValidateParameter(int index, object value)
        {
            HsqlParameter parameter = (HsqlParameter)value;

            object parameterParent
                = parameter.CompareExchangeParent(this, null);

            if (parameterParent != null)
            {
                if (parameterParent != this)
                {
                    throw new ArgumentException(
                        "The parameter is already contained by"
                        + "another collection",
                        "value");
                }

                int parameterIndex = IndexOf(parameter);

                if (index != parameterIndex)
                {
                    throw new ArgumentException(
                        "The parameter is already contained by this"
                        + " collection at a different index: "
                        + parameterIndex,
                        "value");
                }
            }

            if (string.IsNullOrEmpty(parameter.ParameterName))
            {
                string candidateName;
                index = 1;

                do
                {
                    candidateName
                        = "@p" + index.ToString(CultureInfo.CurrentCulture);
                    index++;
                }
                while (IndexOf(candidateName) >= 0);

                parameter.ParameterName = candidateName;
            }
        }
        #endregion

        #endregion

        #region ICloneable Members

        /// <summary>
        /// Clones this instance.
        /// </summary>
        /// <returns></returns>
        public HsqlParameterCollection Clone()
        {
            return new HsqlParameterCollection(this);
        }

        /// <summary>
        /// Creates a new object that is a copy of the current instance.
        /// </summary>
        /// <returns>
        /// A new object that is a copy of this instance.
        /// </returns>
        object ICloneable.Clone()
        {
            return this.Clone();
        }

        #endregion

        #region ICollection<HsqlParameter> Members

        void ICollection<HsqlParameter>.Add(HsqlParameter item)
        {
            this.Add(item);
        }

        void ICollection<HsqlParameter>.Clear()
        {
            this.Clear();
        }

        bool ICollection<HsqlParameter>.Contains(HsqlParameter item)
        {
            return this.Contains(item);
        }

        void ICollection<HsqlParameter>.CopyTo(HsqlParameter[] array, int arrayIndex)
        {
            this.CopyTo(array, arrayIndex);
        }

        int ICollection<HsqlParameter>.Count
        {
            get { return this.Count; }
        }

        bool ICollection<HsqlParameter>.IsReadOnly
        {
            get { return this.IsReadOnly; }
        }

        bool ICollection<HsqlParameter>.Remove(HsqlParameter item)
        {
            bool removed = false;

            if (this.Contains(item))
            {
                this.Remove(item);

                removed = true;
            }

            return removed;
        }

        #endregion

        #region IEnumerable Members

        IEnumerator IEnumerable.GetEnumerator()
        {
            return this.GetEnumerator();
        }

        #endregion

        #region IList<HsqlParameter> Members

        int IList<HsqlParameter>.IndexOf(HsqlParameter item)
        {
            return this.IndexOf(item);
        }

        void IList<HsqlParameter>.Insert(int index, HsqlParameter item)
        {
            this.Insert(index, item);
        }

        void IList<HsqlParameter>.RemoveAt(int index)
        {
            this.RemoveAt(index);
        }

        HsqlParameter IList<HsqlParameter>.this[int index]
        {
            get { return this[index] as HsqlParameter; }
            set { this[index] = value; }
        }

        #endregion
    }
}
