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
using System.Data.Hsqldb.Client;
using System.Collections.Generic;
using System.ComponentModel;
using System.ComponentModel.Design.Serialization;
using System.Globalization;
using System.Text; 
#endregion

namespace System.Data.Hsqldb.Client.Design.Converter
{
    #region HsqlConnectionStringBuilderConverter
    /// <summary>
    /// An <c>ExpandableObjectConverter</c> that can convert an 
    /// <c>HsqlConnectionStringBuilder</c> to an 
    /// <see cref="InstanceDescriptor"/>.
    /// </summary>
    public sealed class HsqlConnectionStringBuilderConverter
        : ExpandableObjectConverter
    {
        private static readonly Type[] types = new Type[] { typeof(string) };

        #region CanConvertTo(ITypeDescriptorContext,Type)
        /// <summary>
        /// Returns whether this converter can convert the object to the specified type, 
        /// using the specified context.
        /// </summary>
        /// <param name="context">
        /// An <see cref="ITypeDescriptorContext"/> that provides a 
        /// format context.
        /// </param>
        /// <param name="destinationType">
        /// represents the type you want to convert to.
        /// </param>
        /// <returns>
        /// <c>true</c> if this converter can perform the conversion; 
        /// otherwise, <c>false</c>.
        /// </returns>
        public override bool CanConvertTo(
            ITypeDescriptorContext context,
            Type destinationType)
        {
            return (destinationType == typeof(InstanceDescriptor)
                || base.CanConvertTo(context, destinationType));
        }
        #endregion

        #region ConvertTo(ITypeDescriptorContext,CultureInfo,object,Type)
        /// <summary>
        /// Converts the given value object to the specified type, using 
        /// the specified context and culture information.
        /// </summary>
        /// <param name="context">
        /// An <see cref="ITypeDescriptorContext"/> that provides a format context.
        /// </param>
        /// <param name="culture">
        /// A <see cref="CultureInfo"/>. If null is passed, the current culture is assumed.
        /// </param>
        /// <param name="value">
        /// The <see cref="Object"></see> to convert.
        /// </param>
        /// <param name="destinationType">
        /// The <see cref="Type"></see> to which to convert the value parameter.
        /// </param>
        /// <returns>
        /// An object that represents the converted value.
        /// </returns>
        /// <exception cref="NotSupportedException">
        /// When the conversion cannot be performed.
        /// </exception>
        /// <exception cref="ArgumentNullException">
        /// When the <c>destinationType</c> parameter is <c>null</c>.
        /// </exception>
        public override object ConvertTo(
            ITypeDescriptorContext context,
            CultureInfo culture,
            object value,
            Type destinationType)
        {
            if (destinationType == null)
            {
                throw new ArgumentNullException("destinationType");
            }

            if (typeof(InstanceDescriptor) == destinationType)
            {
                HsqlConnectionStringBuilder builder = value as HsqlConnectionStringBuilder;

                if (builder != null)
                {
                    return ConvertToInstanceDescriptor(builder);
                }
            }

            return base.ConvertTo(context, culture, value, destinationType);
        }
        #endregion

        #region ConvertToInstanceDescriptor(HsqlConnectionStringBuilder)
        /// <summary>
        /// Converts the given <c>HsqlConnectionStringBuilder</c>
        /// to an instance descriptor.
        /// </summary>
        /// <param name="builder">The builder.</param>
        /// <returns>
        /// An instance descriptor representing the given 
        /// <c>HsqlConnectionStringBuilder</c>.
        /// </returns>
        private InstanceDescriptor ConvertToInstanceDescriptor(
            HsqlConnectionStringBuilder builder)
        {
            return new InstanceDescriptor(
                typeof(HsqlConnectionStringBuilder).GetConstructor(types),
                new object[] { builder.ConnectionString });
        }
        #endregion
    } 
    #endregion
}


