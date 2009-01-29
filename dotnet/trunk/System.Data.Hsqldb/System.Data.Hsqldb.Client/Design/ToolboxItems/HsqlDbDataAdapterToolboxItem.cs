#region licence

/* Copyright (c) 2001-2008, The HSQL Development Group
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

#if W32DESIGN

#region Using
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.ComponentModel.Design;
using System.Data.Common;
using System.Drawing;
using System.Drawing.Design;
using System.Reflection;
using System.Windows.Forms;
using System.Runtime.Serialization; 
#endregion

namespace System.Data.Hsqldb.Client.Design.Toolbox
{
    #region HsqlDataAdapterToolboxItem

    /// <summary>
    /// Provides a <c>ToolboxItem</c> for an <c></c>.
    /// </summary>
    /// <remarks>
    /// <para>
    /// This class supports pop up of the connection wizard when dropping
    /// an item on a form and creates hidden commands that are assigned
    /// to the data adapter.
    /// </para>
    /// <para>
    /// Runtime hiding of the controls is accomplished here during the creation 
    /// of the components, and in the <c>HsqlCommandDesigner</c> which provides
    /// properties to hide the objects when they're supposed to be hidden.
    /// </para>
    /// <para>
    /// The connection wizard is instantiated in the VSDesigner through reflection.
    /// </para>
    /// </remarks>
    [Serializable]
    [ToolboxItem(typeof(HsqlDataAdapterToolboxItem))]
    public sealed class HsqlDataAdapterToolboxItem : ToolboxItem
    {
        #region Constants
        const string VsDesignerAssemblyName
            = "Microsoft.VSDesigner, Version=8.0.0.0, Culture=neutral, PublicKeyToken=b03f5f7f11d50a3a";
        const string WizardTypeName
            = "Microsoft.VSDesigner.Data.VS.DataAdapterWizard"; 
        #endregion

        #region Fields
        private static Type m_wizard = null;
        internal static Assembly m_vsDesigner = null; 
        #endregion

        #region Static Initializer
        /// <summary>
        /// Initializes the <see cref="HsqlDataAdapterToolboxItem"/> class.
        /// </summary>
        static HsqlDataAdapterToolboxItem()
        {
            m_vsDesigner = Assembly.Load(VsDesignerAssemblyName);
            m_wizard = m_vsDesigner.GetType(WizardTypeName);
        } 
        #endregion
        
        #region Constructors

        #region HsqlDataAdapterToolboxItem(Type)
        /// <summary>
        /// Constructs a new <c>HsqlDataAdapterToolboxItem</c> instance
        /// with the given type.
        /// </summary>
        /// <param name="type">The type.</param>
        public HsqlDataAdapterToolboxItem(Type type)
            : this(type, (Bitmap)null)
        {
        }
        #endregion

        #region HsqlDataAdapterToolboxItem(Type,Bitmap)
        /// <summary>
        /// Constructs a new <c>HsqlDataAdapterToolboxItem</c> instance 
        /// with the given type and bitmap.
        /// </summary>
        /// <param name="type">The type.</param>
        /// <param name="bmp">The bitmap.</param>
        public HsqlDataAdapterToolboxItem(Type type, Bitmap bmp)
            : base(type)
        {
            base.DisplayName = "HsqlDataAdapter";
        }
        #endregion

        #region HsqlDataAdapterToolboxItem(SerializationInfo,StreamingContext)
        /// <summary>
        /// Constructs a new <c>HsqlDataAdapterToolboxItem</c> instance 
        /// with the given serialization info and streaming context.
        /// </summary>
        /// <param name="info">The serialization info.</param>
        /// <param name="context">The streaming context.</param>
        private HsqlDataAdapterToolboxItem(SerializationInfo info, StreamingContext context)
        {
            base.Deserialize(info, context);
        }
        #endregion 
        
        #endregion

        #region ToolboxItem Method Overrides

        #region CreateComponentsCore(IDesignerHost)
        /// <summary>
        /// Creates the necessary components associated with the data adapter instance
        /// </summary>
        /// <param name="host">
        /// The designer host
        /// </param>
        /// <returns>
        /// The components created by this toolbox item
        /// </returns>
        protected override IComponent[] CreateComponentsCore(IDesignerHost host)
        {
            DbProviderFactory dbProviderFactory = DbProviderFactories.GetFactory("System.Data.Hsqldb");

            DbDataAdapter dataAdapter = dbProviderFactory.CreateDataAdapter();
            IContainer container = host.Container;

            using (DbCommand adapterCommand = dbProviderFactory.CreateCommand())
            {
                adapterCommand.DesignTimeVisible = false;

                dataAdapter.SelectCommand = CloneCommand(adapterCommand);
                container.Add(dataAdapter.SelectCommand, CreateUniqueName(container, "SelectCommand"));

                dataAdapter.InsertCommand = CloneCommand(adapterCommand);
                container.Add(dataAdapter.InsertCommand, CreateUniqueName(container, "InsertCommand"));

                dataAdapter.UpdateCommand = CloneCommand(adapterCommand);
                container.Add(dataAdapter.UpdateCommand, CreateUniqueName(container, "UpdateCommand"));

                dataAdapter.DeleteCommand = CloneCommand(adapterCommand);
                container.Add(dataAdapter.DeleteCommand, CreateUniqueName(container, "DeleteCommand"));
            }

            ITypeResolutionService typeResService
                = (ITypeResolutionService)host.GetService(typeof(ITypeResolutionService));

            if (typeResService != null)
            {
                typeResService.ReferenceAssembly(dataAdapter.GetType().Assembly.GetName());
            }

            container.Add(dataAdapter);

            List<IComponent> list = new List<IComponent>();

            list.Add(dataAdapter);

            // Show the connection wizard if we have a type for it
            if (m_wizard != null)
            {
                using (Form wizard = (Form)Activator.CreateInstance(
                    m_wizard,
                    new object[] { host, dataAdapter }))
                {
                    wizard.ShowDialog();
                }
            }

            if (dataAdapter.SelectCommand != null) list.Add(dataAdapter.SelectCommand);
            if (dataAdapter.InsertCommand != null) list.Add(dataAdapter.InsertCommand);
            if (dataAdapter.DeleteCommand != null) list.Add(dataAdapter.DeleteCommand);
            if (dataAdapter.UpdateCommand != null) list.Add(dataAdapter.UpdateCommand);

            return list.ToArray();
        } 
        #endregion

        #endregion

        #region Private Helper Methods

        #region CloneCommand(DbCommand)
        /// <summary>
        /// Clones the given command.
        /// </summary>
        /// <param name="command">
        /// The command.
        /// </param>
        /// <returns>
        /// A clone of the given command.
        /// </returns>
        private static DbCommand CloneCommand(DbCommand command)
        {
            return (DbCommand)((ICloneable)command).Clone();
        } 
        #endregion

        #region CreateUniqueName(IContainer,string)
        /// <summary>
        /// Generates a unique name for the given object.
        /// </summary>
        /// <param name="container">
        /// The container in which this object is being instantiated.
        /// </param>
        /// <param name="baseName">
        /// The core name of the object for which to create a unique name instance.
        /// </param>
        /// <returns>
        /// A unique name within the given container.
        /// </returns>
        private static string CreateUniqueName(IContainer container, string baseName)
        {
            ComponentCollection collection = container.Components;
            string uniqueName;
            int n = 1;

            do
            {
                uniqueName = String.Format("hsqldb{0}{1}", baseName, n++);
            } while (collection[uniqueName] != null);

            return uniqueName;
        } 
        #endregion

        #endregion
    }

    #endregion
}

#endif
