using System;
using System.Data;
using System.Data.Hsqldb.Client;
using System.Data.Hsqldb.TestCoverage;
using System.Transactions;
using NUnit.Framework;

namespace System.Data.Hsqldb.Client.UnitTests
{    
    [TestFixture, ForSubject(typeof(HsqlConnection))]
    public class TestHsqlConnection
    {
        [Test, OfMember("BeginTransaction")]
        public virtual void BeginTransaction()
        {
            using (HsqlConnection testSubject = new HsqlConnection())
            using (HsqlTransaction transaction = testSubject.BeginTransaction())
            {
                
            }

            using (HsqlConnection testSubject = new HsqlConnection())
            using (HsqlTransaction transaction = testSubject.BeginTransaction(System.Data.IsolationLevel.ReadUncommitted))
            {
                
            }

            Assert.Fail("TODO");
        }
        
        [Test, OfMember("ChangeDatabase")]
        public virtual void ChangeDatabase()
        {
            using (HsqlConnection testSubject = new HsqlConnection())
            {
                string databaseName = "mem:test";

                testSubject.ChangeDatabase(databaseName);
 
                Assert.Fail("TODO");
            }
        }
        
        [Test, OfMember("Clone")]
        public virtual void Clone()
        {
            using (HsqlConnection testSubject = new HsqlConnection())
            {
                HsqlConnection copy = testSubject.Clone();

                Assert.Fail("TODO");
            }
        }
        
        [Test, OfMember("Close")]
        public virtual void Close()
        {
            using (HsqlConnection testSubject = new HsqlConnection())
            {
                testSubject.Close();

                Assert.Fail("TODO");
            } 
        }
        
        [Test, OfMember("CreateCommand")]
        public virtual void CreateCommand()
        {
            using (HsqlConnection testSubject = new HsqlConnection())
            {
                testSubject.CreateCommand();
 
                Assert.Fail("TODO");
            }
        }
        
        [Test, OfMember("EnlistTransaction")]
        public virtual void EnlistTransaction()
        {
            using(TransactionScope scope = new TransactionScope(TransactionScopeOption.Required))
            using (HsqlConnection testSubject = new HsqlConnection())
            {
                testSubject.EnlistTransaction(Transaction.Current);

                // 
                Assert.Fail("TODO");
            }
        }
        
        [Test, OfMember("GetSchema")]
        public virtual void GetSchema()
        {
            using (HsqlConnection testSubject = new HsqlConnection())
            {
                DataTable dataTable = testSubject.GetSchema();

                Assert.Fail("TODO");
            }
        }
        
        [Test, OfMember("Open")]
        public virtual void Open()
        {
            using (HsqlConnection testSubject = new HsqlConnection())
            {
                testSubject.Open();

                Assert.Fail("TODO");
            }
        }
    }
}
