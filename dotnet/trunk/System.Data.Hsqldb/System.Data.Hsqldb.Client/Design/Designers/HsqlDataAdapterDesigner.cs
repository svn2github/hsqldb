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
using System.ComponentModel;
using System.ComponentModel.Design;
using System.Data.Common;
using System.Collections;
using System.Reflection;
using System.Data.Hsqldb.Client.Design.Toolbox;
#endregion

namespace System.Data.Hsqldb.Client.Design.Designer
{

    /// <summary>
    /// The purpose of this class is to provide context menus and event
    /// support when designing an HSQLDB DataSet.  Most of the functionality
    /// is implemented by MS's VSDesigner object which is instantiated through
    /// reflection because a design-time reference to the object is not otherwise
    /// provided and because many of the objects in VSDesigner are internal.
    /// </summary>
    internal sealed class HsqlDataAdapterDesigner : ComponentDesigner, IExtenderProvider
    {
        private ComponentDesigner m_designer = null;

        /// <summary>
        /// Default constructor
        /// </summary>
        public HsqlDataAdapterDesigner()
        {
        }

        /// <summary>
        /// Initialize the designer by creating an SqlDataAdapterDesigner and delegating most
        /// functionality to it.
        /// </summary>
        /// <param name="component"></param>
        public override void Initialize(IComponent component)
        {
            base.Initialize(component);

            // Initialize a SqlDataAdapterDesigner through reflection and set it up to work on our behalf
            if (HsqlDataAdapterToolboxItem.m_vsDesigner != null)
            {
                Type type = HsqlDataAdapterToolboxItem.m_vsDesigner.GetType("Microsoft.VSDesigner.Data.VS.SqlDataAdapterDesigner");
                
                if (type != null)
                {
                    m_designer = (ComponentDesigner)Activator.CreateInstance(type);
                    m_designer.Initialize(component);
                }
            }
        }

        protected override void Dispose(bool disposing)
        {
            if (m_designer != null && disposing)
                ((IDisposable)m_designer).Dispose();

            base.Dispose(disposing);
        }

        /// <summary>
        /// Forwards to the SqlDataAdapterDesigner object
        /// </summary>
        public override DesignerVerbCollection Verbs
        {
            get
            {
                return (m_designer != null) ? m_designer.Verbs : null;
            }
        }

        /// <summary>
        /// Forwards to the SqlDataAdapterDesigner object
        /// </summary>
        public override ICollection AssociatedComponents
        {
            get
            {
                return (m_designer != null) ? m_designer.AssociatedComponents : null;
            }
        }

        #region IExtenderProvider Members
        /// <summary>
        /// We extend support for DbDataAdapter-derived objects
        /// </summary>
        /// <param name="extendee">The object wanting to be extended</param>
        /// <returns>Whether or not we extend that object</returns>
        public bool CanExtend(object extendee)
        {
            return (extendee is DbDataAdapter);
        }

        #endregion
    }
}
