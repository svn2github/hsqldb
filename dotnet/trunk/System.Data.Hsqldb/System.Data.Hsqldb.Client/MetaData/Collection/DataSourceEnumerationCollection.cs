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
using System.Data;
using System.Globalization;
using System.IO;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    #region DataSourceEnumerationCollection
    
    /// <summary>
    /// <para>
    /// Provides the core logic for the HSQLDB 
    /// <see cref="System.Data.Common.DbDataSourceEnumerator"/> implementation.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.DataSourceEnumerationCollection.png"
    ///      alt="DataSourceEnumerationCollection Class Diagram"/>
    /// </summary>
    public static class DataSourceEnumerationCollection
    {
        private const string m_File = @"\datasource-enumeration.xml";

        #region Public Constants

        #region DataSetSchema
        /// <summary>
        /// 
        /// </summary>
        public const string DataSetSchema =
@"<?xml version='1.0' encoding='utf-8'?>
<xs:schema
    id='DataSourceEnumerationDataSet'
    targetNamespace='http://org.hsqldb/schemas/1.8.0.7/DataSourceEnumeration.xsd'
    xmlns:mstns='http://org.hsqldb/schemas/1.8.0.7/DataSourceEnumeration.xsd'
    xmlns='http://org.hsqldb/schemas/1.8.0.7/DataSourceEnumeration.xsd'
    xmlns:xs='http://www.w3.org/2001/XMLSchema'
    xmlns:msdata='urn:schemas-microsoft-com:xml-msdata'
    xmlns:msprop='urn:schemas-microsoft-com:xml-msprop'
    attributeFormDefault='qualified'
    elementFormDefault='qualified'>
  <xs:annotation>
    <xs:appinfo source='urn:schemas-microsoft-com:xml-msdatasource'>
      <DataSource
        DefaultConnectionIndex='0'
        FunctionsComponentName='QueriesTableAdapter'
        Modifier='AutoLayout, AnsiClass, Class, Public'
        SchemaSerializationMode='IncludeSchema'
        xmlns='urn:schemas-microsoft-com:xml-msdatasource'>
        <Connections>
        </Connections>
        <Tables>
        </Tables>
        <Sources>
        </Sources>
      </DataSource>
    </xs:appinfo>
  </xs:annotation>
  <xs:element
     name='DataSourceEnumerationDataSet'
     msdata:IsDataSet='true'
     msdata:UseCurrentLocale='true'
     msprop:Generator_UserDSName='DataSourceEnumerationDataSet'
     msprop:Generator_DataSetName='DataSourceEnumerationDataSet'>
    <xs:complexType>
      <xs:choice minOccurs='0' maxOccurs='unbounded'>
        <xs:element
           name='SqlDataSources'
           msprop:Generator_UserTableName='SqlDataSources'
           msprop:Generator_RowDeletedName='SqlDataSourcesRowDeleted'
           msprop:Generator_RowChangedName='SqlDataSourcesRowChanged'
           msprop:Generator_RowClassName='SqlDataSourcesRow'
           msprop:Generator_RowChangingName='SqlDataSourcesRowChanging'
           msprop:Generator_RowEvArgName='SqlDataSourcesRowChangeEvent'
           msprop:Generator_RowEvHandlerName='SqlDataSourcesRowChangeEventHandler'
           msprop:Generator_TableClassName='SqlDataSourcesDataTable'
           msprop:Generator_TableVarName='tableSqlDataSources'
           msprop:Generator_RowDeletingName='SqlDataSourcesRowDeleting'
           msprop:Generator_TablePropName='SqlDataSources'>
          <xs:complexType>
            <xs:sequence>
              <xs:element
                 name='ServerName'
                 msprop:Generator_UserColumnName='ServerName'
                 msprop:Generator_ColumnPropNameInRow='ServerName'
                 msprop:Generator_ColumnVarNameInTable='columnServerName'
                 msprop:Generator_ColumnPropNameInTable='ServerNameColumn'
                 type='xs:string'
                 minOccurs='0' />
              <xs:element
                 name='InstanceName'
                 msprop:Generator_UserColumnName='InstanceName'
                 msprop:Generator_ColumnPropNameInRow='InstanceName'
                 msprop:Generator_ColumnVarNameInTable='columnInstanceName'
                 msprop:Generator_ColumnPropNameInTable='InstanceNameColumn'
                 type='xs:string' minOccurs='0' />
              <xs:element name='IsClustered'
                 msprop:Generator_UserColumnName='IsClustered'
                 msprop:Generator_ColumnPropNameInRow='IsClustered'
                 msprop:Generator_ColumnVarNameInTable='columnIsClustered'
                 msprop:Generator_ColumnPropNameInTable='IsClusteredColumn'
                 type='xs:string' minOccurs='0' />
              <xs:element
                 name='Version'
                 msprop:Generator_UserColumnName='Version'
                 msprop:Generator_ColumnPropNameInRow='Version'
                 msprop:Generator_ColumnVarNameInTable='columnVersion'
                 msprop:Generator_ColumnPropNameInTable='VersionColumn'
                 type='xs:string'
                 minOccurs='0' />
            </xs:sequence>
          </xs:complexType>
        </xs:element>
      </xs:choice>
    </xs:complexType>
  </xs:element>
</xs:schema>";
        #endregion

        #region ServerNameColumnName
        /// <summary>
        /// 
        /// </summary>
        public const string ServerNameColumnName = "ServerName"; 
        #endregion

        #region ServerNameColumnOrdinal
        /// <summary>
        /// 
        /// </summary>
        public const int ServerNameColumnOrdinal = 0; 
        #endregion

        #region InstanceNameColumnName
        /// <summary>
        /// 
        /// </summary>
        public const string InstanceNameColumnName = "InstanceName"; 
        #endregion

        #region InstanceNameColumnOrdinal
        /// <summary>
        /// 
        /// </summary>
        public const int InstanceNameColumnOrdinal = 1;

        #region IsClusteredColumnName
        #endregion
        /// <summary>
        /// 
        /// </summary>
        public const string IsClusteredColumnName = "IsClustered"; 
        #endregion

        #region IsClusteredColumnOrdinal
        /// <summary>
        /// 
        /// </summary>
        public const int IsClusteredColumnOrdinal = 2; 
        #endregion

        #region VersionColumnName
        /// <summary>
        /// 
        /// </summary>
        public const string VersionColumnName = "Version"; 
        #endregion

        #region VersionColumnOrdinal
        /// <summary>
        /// 
        /// </summary>
        public const int VersionColumnOrdinal = 3; 
        #endregion

        #region PathSuffix
        /// <summary>
        /// 
        /// </summary>
        public const string PathSuffix = @"\.hsqldb\1.8.0" + m_File; 
        #endregion

        #region TableName
        /// <summary>
        /// 
        /// </summary>
        public const string TableName = "SqlDataSources"; 
        #endregion
        
        #endregion


        #region GetPath(Environment.SpecialFolder,string)
        /// <summary>
        /// Gets the path.
        /// </summary>
        /// <param name="type">The type.</param>
        /// <param name="suffix">The suffix.</param>
        /// <returns></returns>
        private static string GetPath(Environment.SpecialFolder type, string suffix)
        {
            try
            {
                string path = Environment.GetFolderPath(type);

                return (string.IsNullOrEmpty(path)) ? null : path + suffix;
            }
            catch (Exception)
            {
                return null;
            }
        }
        #endregion

        #region GetEnvPath(string,string)
        /// <summary>
        /// Gets the env path.
        /// </summary>
        /// <param name="key">The key.</param>
        /// <param name="suffix">The suffix.</param>
        /// <returns></returns>
        private static string GetEnvPath(string key, string suffix)
        {
            try
            {
                string path = Environment.GetEnvironmentVariable(key);

                return (string.IsNullOrEmpty(path)) ? null : path + suffix;
            }
            catch (Exception)
            {
                return null;
            }
        }
        #endregion

        #region GetCurrentDirPath()
        /// <summary>
        /// Gets the current dir path.
        /// </summary>
        /// <returns></returns>
        private static string GetCurrentDirPath()
        {
            try
            {
                return Environment.CurrentDirectory + m_File;
            }
            catch (Exception)
            {
                return null;
            }
        }
        #endregion

        #region GetDataSources()
        /// <summary>
        /// Gets the data sources.
        /// </summary>
        /// <returns></returns>
        public static DataTable GetDataSources()
        {
            string[] paths = new string[]
                {
                    GetEnvPath("SystemRoot", PathSuffix),
                    GetEnvPath("ALLUSERSPROFILE", PathSuffix),
                    GetPath(Environment.SpecialFolder.ApplicationData, PathSuffix),
                    GetPath(Environment.SpecialFolder.LocalApplicationData, PathSuffix),
                    GetEnvPath("USERPROFILE", PathSuffix),
                    GetCurrentDirPath()
                };

            DataSet dseMaster = new DataSet();

            dseMaster.ReadXmlSchema(new StringReader(DataSetSchema));

            for (int i = 0; i < paths.Length; i++)
            {
                string path = paths[i];

                //Console.WriteLine(path);

                if (string.IsNullOrEmpty(path))
                {
                    continue;
                }

                try
                {
                    if (File.Exists(path))
                    {
                        //Console.WriteLine("processing " + path);
                        DataSet dse = new DataSet();
                        TextReader reader = new StringReader(DataSetSchema);

                        dse.ReadXmlSchema(reader);
                        dse.ReadXml(path, XmlReadMode.IgnoreSchema);
                        dseMaster.Merge(dse);
                    }
                }
                catch (Exception) { }
            }

            DataTable table = dseMaster.Tables[TableName];

            table.Locale = CultureInfo.InvariantCulture;

            foreach (DataColumn column in table.Columns)
            {
                column.ReadOnly = true;
            }

            return table;
        }
        #endregion
    } 
    
    #endregion
}