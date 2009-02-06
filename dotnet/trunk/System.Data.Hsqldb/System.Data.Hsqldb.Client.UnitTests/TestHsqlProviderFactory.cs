#region Using
using System;
using System.Data;
using System.Data.Common;
using System.Data.Hsqldb.TestCoverage;
using System.Security;
using System.Security.Permissions;
using NUnit.Framework; 
#endregion

namespace System.Data.Hsqldb.Client.UnitTests
{
    [TestFixture, ForSubject(typeof(HsqlProviderFactory))]
    public class TestHsqlProviderFactory
    {
        static DbProviderFactory TestSubject
        {
            get { return HsqlProviderFactory.Instance; }
        }

        [Test, OfMember("CreateCommand")]
        public void CreateCommand()
        {
            DbCommand actual = TestSubject.CreateCommand();

            Assert.IsInstanceOfType(typeof(HsqlCommand), actual);           
        }
        
        [Test, OfMember("CreateCommandBuilder")]
        public void CreateCommandBuilder()
        {
            DbCommandBuilder actual = TestSubject.CreateCommandBuilder();

            Assert.IsInstanceOfType(typeof(HsqlCommandBuilder), actual);
        }
        
        [Test, OfMember("CreateConnection")]
        public void CreateConnection()
        {
            DbConnection actual = TestSubject.CreateConnection();

            Assert.IsInstanceOfType(typeof(HsqlConnection), actual);
        }
        
        [Test, OfMember("CreateConnectionStringBuilder")]
        public void CreateConnectionStringBuilder()
        {
           
            DbConnectionStringBuilder actual = TestSubject.CreateConnectionStringBuilder();

            Assert.IsInstanceOfType(typeof(HsqlConnectionStringBuilder), actual);
        }
        
        [Test, OfMember("CreateDataAdapter")]
        public void CreateDataAdapter()
        {
            DbDataAdapter actual = TestSubject.CreateDataAdapter();

            Assert.IsInstanceOfType(typeof(HsqlDataAdapter), actual);
        }
        
        [Test, OfMember("CreateDataSourceEnumerator")]
        public void CreateDataSourceEnumerator()
        {
            DbDataSourceEnumerator actual = TestSubject.CreateDataSourceEnumerator();

            Assert.IsInstanceOfType(typeof(HsqlDataSourceEnumerator), actual);
        }
        
        [Test, OfMember("CreateParameter")]
        public void CreateParameter()
        {
            DbParameter actual = TestSubject.CreateParameter();

            Assert.IsInstanceOfType(typeof(HsqlParameter), actual);
        }
        
        [Test, OfMember("CreatePermission")]
        public void CreatePermission()
        {
            CodeAccessPermission actual = TestSubject.CreatePermission(PermissionState.None);

            Assert.IsInstanceOfType(typeof(DBDataPermission), actual);
            Assert.IsInstanceOfType(typeof(HsqlDataPermission), actual);
        }
    }
}
