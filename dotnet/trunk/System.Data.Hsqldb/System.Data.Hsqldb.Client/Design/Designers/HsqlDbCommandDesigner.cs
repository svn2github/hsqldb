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
using System.ComponentModel.Design;
using System.Data.Common;
using System.Data;
#endregion

namespace System.Data.Hsqldb.Client.Design.Designer
{

    /// <summary>
    /// Provides a designer for an <c>HsqlCommand</c>.
    /// </summary>
    /// <remarks>
    /// The <c>CommandDesignTimeVisible</c> property is provided because
    /// certain MS designer components look for it and fail if its not there.
    /// </remarks>
    [ProvideProperty("CommandDesignTimeVisible", typeof(IDbCommand))]
    public sealed class HsqlCommandDesigner : ComponentDesigner, IExtenderProvider
    {
        #region HsqlCommandDesigner()
        /// <summary>
        /// Constructs a new <c>HsqlCommandDesigner</c> instance.
        /// </summary>
        public HsqlCommandDesigner()
        {
        } 
        #endregion

        #region Initialize(IComponent)
        /// <summary>
        /// Initialize the instance with the given SQLiteCommand component
        /// </summary>
        /// <param name="component"></param>
        public override void Initialize(IComponent component)
        {
            base.Initialize(component);
        } 
        #endregion

        #region PreFilterAttributes(IDictionary)
        /// <summary>
        /// Adds the DesignTimeVisible attribute to the attributes for the item
        /// </summary>
        /// <param name="attributes"></param>
        protected override void PreFilterAttributes(IDictionary attributes)
        {
            base.PreFilterAttributes(attributes);

            DesignTimeVisibleAttribute attribute
                = new DesignTimeVisibleAttribute(((DbCommand)Component).DesignTimeVisible);

            attributes[attribute.TypeId] = attribute;
        } 
        #endregion

        #region GetCommandDesignTimeVisible(IDbCommand)
        /// <summary>
        /// Provides a getter for the <c>CommandDesignTimeVisible</c> property
        /// </summary>
        /// <param name="command">
        /// The <c>HsqlCommand</c> for which design is being provided.
        /// </param>
        /// <returns>
        /// The value of the <c>CommandDesignTimeVisible</c> property.
        /// </returns>
        [Browsable(false)]
        [DesignOnly(true)]
        [DefaultValue(true)]
        public bool GetCommandDesignTimeVisible(IDbCommand command)
        {
            return ((DbCommand)command).DesignTimeVisible;
        } 
        #endregion

        #region SetCommandDesignTimeVisible(IDbCommand, bool)
        /// <summary>
        /// Provides a setter for the <c>CommandDesignTimeVisible</c> property
        /// </summary>
        /// <param name="command">
        /// The HsqlCommand to set
        /// </param>
        /// <param name="visible">
        /// The <c>CommandDesignTimeVisible</c> property value to assign to the command.
        /// </param>
        public void SetCommandDesignTimeVisible(IDbCommand command, bool visible)
        {
            ((DbCommand)command).DesignTimeVisible = visible;
        } 
        #endregion

        #region IExtenderProvider Members

        #region CanExtend(object)
        /// <summary>
        /// Specifies whether this object can provide its extender properties
        /// to the specified object.
        /// </summary>
        /// <param name="extendee">
        /// The object to receive the extender properties.</param>
        /// <returns>
        /// true if this object can provide extender properties to the specified object; 
        /// otherwise, false.
        /// </returns>
        public bool CanExtend(object extendee)
        {
            return (extendee is DbCommand);
        } 
        #endregion

        #endregion
    }
}
