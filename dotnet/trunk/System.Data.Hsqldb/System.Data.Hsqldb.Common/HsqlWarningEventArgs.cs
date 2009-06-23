using System;

namespace System.Data.Hsqldb.Common
{
    /// <summary>
    /// 
    /// </summary>
    delegate void HsqlWarningEventHandler(object sender, HsqlWarningEventArgs args);

    /// <summary>
    /// 
    /// </summary>
    public sealed class HsqlWarningEventArgs
    {
        #region Fields
        private HsqlDataSourceException m_exception; 
        #endregion

        #region Constructors

        #region HsqlWarningEventArgs(HsqlDataSourceException)
        /// <summary>
        /// Initializes a new instance of the <see cref="HsqlWarningEventArgs"/> class.
        /// </summary>
        /// <param name="exception">
        /// The object that directly represents the warning condition.
        /// </param>
        public HsqlWarningEventArgs(HsqlDataSourceException exception)
        {
            m_exception = exception;
        } 
        #endregion

        #endregion

        #region Properties

        #region RootWarning
        /// <summary>
        /// Gets the root warning, which may contain a collection of
        /// chained <see cref="HsqlDataSourceException.Exceptions"/>.
        /// </summary>
        /// <value>The root warning.</value>
        public HsqlDataSourceException RootWarning
        {
            get
            {
                return m_exception;
            }
        } 
        #endregion

        #endregion

    }
}
