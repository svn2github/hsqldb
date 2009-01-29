using System;
using System.ComponentModel;
using System.ComponentModel.Design;
using System.Data.Common;
using System.Data;
using System.Collections;

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
