using System;
using System.ComponentModel;
using System.ComponentModel.Design;
using System.Data.Common;
using System.Collections;
using System.Reflection;
using System.Data.Hsqldb.Client.Design.Toolbox;

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
