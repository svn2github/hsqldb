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
        void TestBeginTransaction(IsolationLevel isolationLevel, bool isolationLevelIsSupported)
        {
            try
            {
                using (HsqlConnection testSubject = new HsqlConnection())
                {
                    testSubject.Open();

                    using (HsqlTransaction transaction = testSubject.BeginTransaction(isolationLevel))
                    {

                    }
                }

                Assert.That(isolationLevelIsSupported,
                    "System.Data.IsolationLevel: " + Enum.GetName(typeof(IsolationLevel), 
                    isolationLevel));
            }
            catch (Exception ex)
            {
                Assert.That(!isolationLevelIsSupported,
                    "System.Data.IsolationLevel: " + Enum.GetName(typeof(IsolationLevel),
                    isolationLevel));
            }
        }

        [Test, OfMember("BeginTransaction")]
        public virtual void BeginTransaction()
        {
            using (HsqlConnection testSubject = new HsqlConnection())
            {
                testSubject.Open();

                using (HsqlTransaction transaction = testSubject.BeginTransaction())
                {

                }
            }

            object[] expected = new object[]
            {
                IsolationLevel.Chaos, false, 
                IsolationLevel.ReadCommitted, true,
                IsolationLevel.ReadUncommitted, true,
                IsolationLevel.RepeatableRead, true,
                IsolationLevel.Serializable, true,
                IsolationLevel.Snapshot, true,
                IsolationLevel.Unspecified, true
            };

            IsolationLevel isolationLevel;
            bool isolationLevelIsSupported;

            for (int i = 0; i < expected.Length; i += 2)
            {
                isolationLevel = (IsolationLevel)expected[i];
                isolationLevelIsSupported = (bool) expected[i+1];

                TestBeginTransaction(isolationLevel, isolationLevelIsSupported);
            }
        }

        [Test, OfMember("ChangeDatabase")]
        public virtual void ChangeDatabase()
        {
            using (HsqlConnection testSubject = new HsqlConnection("DataSource=mem:test2"))
            {
                string databaseName = "test1";

                testSubject.ChangeDatabase(databaseName);

                testSubject.Open();
            }

            using (HsqlConnection testSubject = new HsqlConnection("DataSource=mem:test2"))
            {
                testSubject.Open();
                string databaseName = "test1";

                try
                {
                    testSubject.ChangeDatabase(databaseName);

                    Assert.Fail("it is not expected that it is legal to change database while a connection is open.");
                }
                catch (Exception ex)
                {

                }
            }
        }
        
        [Test, OfMember("Clone")]
        public virtual void Clone()
        {
            string connectionString = "DataSource=mem:test";

            using (HsqlConnection testSubject = new HsqlConnection(connectionString))
            {
                HsqlConnection copy = testSubject.Clone();

                Assert.AreEqual(connectionString, testSubject.ConnectionString);
            }
        }
        
        [Test, OfMember("Close")]
        public virtual void Close()
        {
            using (HsqlConnection testSubject = new HsqlConnection())
            {
                Assert.That(testSubject.State == ConnectionState.Closed);

                testSubject.Open();

                Assert.That(testSubject.State == ConnectionState.Open);

                testSubject.Close();

                Assert.That(testSubject.State == ConnectionState.Closed);

                testSubject.Close();

                Assert.That(testSubject.State == ConnectionState.Closed);
            } 
        }
        
        [Test, OfMember("CreateCommand")]
        public virtual void CreateCommand()
        {
            using (HsqlConnection testSubject = new HsqlConnection())
            {
                HsqlCommand command = testSubject.CreateCommand();

                Assert.AreSame(testSubject, command.Connection);
                Assert.AreEqual(string.Empty, command.CommandText);
                Assert.AreEqual(CommandType.Text, command.CommandType);
                Assert.AreEqual(true, command.DesignTimeVisible);
                Assert.AreEqual(false, command.IsPrepared);
                Assert.AreEqual(UpdateRowSource.Both, command.UpdatedRowSource);
                Assert.AreEqual(null, command.Transaction);
            }
        }
        
        [Test, OfMember("EnlistTransaction")]
        public virtual void EnlistTransaction()
        {
            HsqlConnection testSubject = new HsqlConnection();

            using (TransactionScope transactionScope = new TransactionScope(TransactionScopeOption.Required))
            {
                testSubject.Open();
                testSubject.EnlistTransaction(Transaction.Current);

                try
                {
                    testSubject.BeginTransaction();

                    Assert.Fail("The test subject allowed a local transaction to be started "
                        + "explicitly while participating in a system transaction");
                }
                catch (Exception ex)
                {
                }

                transactionScope.Complete();

                try
                {
                    testSubject.BeginTransaction();

                    Assert.Fail("The test subject allowed a local transaction to be started "
                        + "explicitly while participating in a system transaction");
                }
                catch (Exception ex)
                {
                    
                }
            }

            using (HsqlTransaction transaction = testSubject.BeginTransaction())
            {
                transaction.Commit();
            }
        }
        
        [Test, OfMember("GetSchema")]
        public virtual void GetSchema()
        {
            using (HsqlConnection testSubject = new HsqlConnection())
            {
                DataTable dataTable = testSubject.GetSchema();

                Console.WriteLine("Table Name: " + dataTable.TableName);
                Console.WriteLine("-----------------------------------");


                foreach (DataRow row in dataTable.Rows)
                {
                    foreach (DataColumn column in dataTable.Columns)
                    {
                        Console.WriteLine(column.Caption + ": " + row[column]);
                    }
                }

                //testSubject.GetSchema("");

                //testSubject.GetSchema("",new string[]);

                
            }
        }
        
        [Test, OfMember("Open")]
        public virtual void Open()
        {
            using (HsqlConnection testSubject = new HsqlConnection())
            {
                testSubject.Open();

                try
                {
                    testSubject.Open();

                    Assert.Fail("A second Open() invocation should not succeed when a connection is already open.");
                }
                catch (Exception ex)
                {                    
                    
                }
            }
        }
    }
}
