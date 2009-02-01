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
using System.ComponentModel;
using System.Collections.Generic;
using System.Reflection;
#endregion

namespace System.Data.Hsqldb.Common.Converter
{
    #region LexographicEnumConverter
    /// <summary>
    /// An <c>EnumConverter</c> that sorts the members of its standard values
    /// collection using lexographic comparison of the names corresponding to
    /// the constant values of an enumeration.
    /// </summary>
    public class LexographicEnumConverter : EnumConverter
    {
        #region LexographicComparer
        /// <summary>
        /// The <c>IComparer</c> implementation for the enclosing 
        /// <c>LexographicEnumConverter</c>.
        /// </summary>
        public class LexographicComparer : IComparer
        {
            #region IComparer Members

            #region Compare(object,object)
            /// <summary>
            /// Compares the string values of the given objects and
            /// returns a value indicating whether one is lexographically
            /// less than, equal to, or greater than the other.
            /// </summary>
            /// <param name="x">The first object to compare.</param>
            /// <param name="y">The second object to compare.</param>
            /// <returns>
            /// Less than zero when Convert.ToString(x) is less than Convert.ToString(y);
            /// zero when Convert.ToString(x) equals Convert.ToString(y); 
            /// greater than zero Convert.ToString(x) is greater than Convert.ToString(y);
            /// </returns>
            public int Compare(object x, object y)
            {
                return Convert.ToString(x).CompareTo(Convert.ToString(y));
            }
            #endregion

            #endregion
        }
        #endregion

        #region Fields
        private static readonly LexographicComparer ComparerInstance;
        #endregion

        #region Static Initializer
        /// <summary>
        /// Initializes the <see cref="LexographicEnumConverter"/> class.
        /// </summary>
        static LexographicEnumConverter()
        {
            ComparerInstance = new LexographicComparer();
        }
        #endregion

        #region Constructor

        #region LexographicEnumConverter(Type)
        /// <summary>
        /// Constructs a new <c>LexographicEnumConverter</c> 
        /// instance with the given type.
        /// </summary>
        /// <param name="type">
        /// A <see cref="Type"></see> that represents the type of 
        /// enumeration to associated with this enumeration converter.
        /// </param>
        public LexographicEnumConverter(Type type) : base(type) { }
        #endregion

        #endregion

        #region Property Overrides

        #region Comparer
        /// <summary>
        /// Gets the <see cref="IComparer"/> to be used to
        /// sort the values of the enumeration.
        /// </summary>
        /// <value>
        /// The <see cref="IComparer"/> used for sorting 
        /// the enumeration values.
        /// </value>
        protected override IComparer Comparer
        {
            get { return ComparerInstance; }
        }
        #endregion


        /// <summary>
        /// Gets a value indicating whether this object supports a standard set of values that can be picked from a list using the specified context.
        /// </summary>
        /// <param name="context">An <see cref="T:System.ComponentModel.ITypeDescriptorContext"></see> that provides a format context.</param>
        /// <returns>
        /// true because <see cref="M:System.ComponentModel.TypeConverter.GetStandardValues"></see> should be called to find a common set of values the object supports. This method never returns false.
        /// </returns>
        public override bool GetStandardValuesSupported(ITypeDescriptorContext context)
        {
            return true;
        }

        /// <summary>
        /// Gets a collection of standard values for the data type this validator is designed for.
        /// </summary>
        /// <param name="context">An <see cref="T:System.ComponentModel.ITypeDescriptorContext"></see> that provides a format context.</param>
        /// <returns>
        /// A <see cref="T:System.ComponentModel.TypeConverter.StandardValuesCollection"></see> that holds a standard set of valid values, or null if the data type does not support a standard set of values.
        /// </returns>
        public override StandardValuesCollection GetStandardValues(ITypeDescriptorContext context)
        {
            
            ArrayList list = new ArrayList();

            foreach (object value in Enum.GetValues(base.EnumType))
            {
                object[] attrs = base.EnumType.GetField(value.ToString()).GetCustomAttributes(typeof(EditorBrowsableAttribute),false);
                
                bool add = true;
                foreach(EditorBrowsableAttribute attr in attrs)
                {
                    if(attr.State != EditorBrowsableState.Always)
                    {
                        add = false;
                        break;
                    }
                }

                if (add)
                {
                    list.Add(value);
                }
            }

            list.Sort(Comparer);

            return new StandardValuesCollection(list);
        }

        #endregion
    }
    #endregion
}
