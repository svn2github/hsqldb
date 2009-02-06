using System;
using NUnit.Framework;
using System.Data.Hsqldb.TestCoverage;
using System.Data.Hsqldb.Common.Enumeration;

namespace System.Data.Hsqldb.Client.UnitTests
{
    [TestFixture, ForSubject(typeof(HsqlCommand))]
    public class TestHsqlCommand
    {
        static HsqlConnection NewConnection()
        {
            HsqlConnectionStringBuilder builder = new HsqlConnectionStringBuilder();

            builder.Protocol = ConnectionProtocol.Mem;
            builder.Path = "test";
            builder.UserId = "SA";
            builder.Password = "";

            HsqlConnection connection = new HsqlConnection(builder.ToString());

            connection.Open();

            return connection;
        }

        [Test, OfMember("Cancel")]
        public void Cancel()
        {
            using (HsqlConnection connection = NewConnection())
            using (HsqlCommand testSubject = connection.CreateCommand())
            {
                testSubject.Cancel(); // no-op.

                Assert.Fail("TODO");
            }
        }

        [Test, OfMember("Clone")]
        public void Clone()
        {
            using (HsqlConnection connection = NewConnection())
            using (HsqlCommand testSubject = connection.CreateCommand())
            {
                HsqlCommand actual = testSubject.Clone();

                Assert.Fail("TODO");
            }
        }

        [Test, OfMember("CommandText")]
        public void CommandText()
        {
            using (HsqlConnection connection = NewConnection())
            using (HsqlCommand testSubject = connection.CreateCommand())
            {
                string actual = testSubject.CommandText;

                Assert.Fail("TODO");
            }
        }

        [Test, OfMember("CommandText")]
        public void CommandTimeout()
        {
            using (HsqlConnection connection = NewConnection())
            using (HsqlCommand testSubject = connection.CreateCommand())
            {
                int actual = testSubject.CommandTimeout;

                Assert.Fail("TODO");
            }
        }

        [Test, OfMember("CommandType")]
        public void CommandType()
        {
            using (HsqlConnection connection = NewConnection())
            using (HsqlCommand testSubject = connection.CreateCommand())
            {
                CommandType expected = System.Data.CommandType.Text;
                CommandType actual = testSubject.CommandType;

                Assert.AreEqual(expected, actual);

                Assert.Fail("TODO");
            }
        }

        [Test, OfMember("CreateParameter")]
        public void CreateParameter()
        {
            using (HsqlConnection connection = NewConnection())
            using (HsqlCommand testSubject = connection.CreateCommand())
            {
                HsqlParameter parameter = testSubject.CreateParameter();

                Assert.Fail("TODO");
            }
        }

        [Test, OfMember("DeriveParameters")]
        public void DeriveParameters()
        {
            using (HsqlConnection connection = NewConnection())
            using (HsqlCommand testSubject = connection.CreateCommand())
            {


                testSubject.DeriveParameters();

                //foreach (HsqlParameter parameter in testSubject.Parameters)
                //{
                //    Assert.AreEqual(DbType.Object, parameter.DbType);
                //    Assert.AreEqual(ParameterDirection.Input, parameter.Direction);
                //    Assert.AreEqual(true, parameter.IsNullable);
                //    Assert.AreEqual("@p0", parameter.ParameterName);
                //    Assert.AreEqual(10, parameter.Precision);
                //    Assert.AreEqual(HsqlProviderType.Object, parameter.ProviderType);
                //    Assert.AreEqual(0, parameter.Scale);
                //    Assert.AreEqual(int.MaxValue, parameter.Size);
                //    Assert.AreEqual(DataRowVersion.Current, parameter.SourceVersion);
                //}

                // 
                Assert.Fail("TODO");
            }

        }

        [Test, OfMember("ExecuteNonQuery")]
        public virtual void ExecuteNonQuery()
        {
            using (HsqlConnection connection = NewConnection())
            using (HsqlCommand testSubject = connection.CreateCommand())
            {
                testSubject.CommandText = ";";
                testSubject.ExecuteNonQuery();

                Assert.Fail("TODO");
            }
        }

        [Test, OfMember("ExecuteReader")]
        public void ExecuteReader()
        {
            using (HsqlConnection connection = NewConnection())
            using (HsqlCommand testSubject = connection.CreateCommand())
            {
                testSubject.CommandText = "select * from information_schema.system_tables";

                using (HsqlDataReader reader = testSubject.ExecuteReader())
                {
                    while (reader.Read())
                    {
                        int fieldCount = reader.GetValues(new object[reader.FieldCount]);
                    }
                }
            }

            using (HsqlConnection connection = NewConnection())
            using (HsqlCommand testSubject = connection.CreateCommand())
            {
                testSubject.CommandText = "select * from information_schema.system_tables";
                HsqlDataReader reader = testSubject.ExecuteReader(CommandBehavior.SchemaOnly);
                DataTable schemaTable = reader.GetSchemaTable();

                foreach (DataRow row in schemaTable.Rows)
                {
                    object[] values = row.ItemArray;
                }
            }
        }

        [Test, OfMember("ExecuteScalar")]
        public virtual void ExecuteScalar()
        {
            using (HsqlConnection connection = NewConnection())
            using (HsqlCommand testSubject = connection.CreateCommand())
            {
                testSubject.CommandText = "call database();";
                object expected = "mem:test";
                object actual = testSubject.ExecuteScalar();

                Assert.Fail("TODO");
            } 
        }

        [Test, OfMember("Prepare")]
        public virtual void Prepare()
        {
            using (HsqlConnection connection = NewConnection())
            using (HsqlCommand testSubject = connection.CreateCommand())
            {
                testSubject.CommandText = "select * from information_schema.system_tables";
                testSubject.Prepare();

                bool expected = true;
                bool actual = testSubject.IsPrepared;

                Assert.AreEqual(expected, actual);

                // 
                Assert.Fail("TODO");
            }
        }

        [Test, OfMember("StatementCompleted")]
        public virtual void StatementCompleted()
        {
            using (HsqlConnection connection = NewConnection())
            using (HsqlCommand testSubject = connection.CreateCommand())
            {
                testSubject.StatementCompleted += delegate(object sender, StatementCompletedEventArgs e)
                {
                    Assert.AreNotSame(testSubject, sender);
                    Assert.AreEqual(0, e.RecordCount);
                };

                // 
                Assert.Fail("TODO");
            }
        }

        [Test, OfMember("Transaction")]
        public virtual void Transaction()
        {
            using (HsqlConnection connection = NewConnection())
            using (HsqlCommand testSubject = connection.CreateCommand())
            {
                testSubject.CommandText = "update foo set bar = baz";
                testSubject.Connection.BeginTransaction(IsolationLevel.ReadUncommitted);
                HsqlTransaction transaction = testSubject.Transaction;

                IsolationLevel expected = IsolationLevel.ReadUncommitted;
                IsolationLevel actual = transaction.IsolationLevel;

                Assert.AreEqual(expected, actual);

                // 
                Assert.Fail("TODO");
            }
        }

        [Test, OfMember("UnPrepare")]
        public virtual void UnPrepare()
        {
            using (HsqlConnection connection = NewConnection())
            using (HsqlCommand testSubject = connection.CreateCommand())
            {
                testSubject.CommandText = "select * from information_schema.system_tables";
                testSubject.Prepare();

                
                testSubject.UnPrepare();

                bool expected = false;
                bool actual = testSubject.IsPrepared;

                Assert.AreEqual(expected, actual);

                Assert.Fail("TODO");
            }
        }

        [Test, OfMember("UpdatedRowSource")]
        public virtual void UpdatedRowSource()
        {
            using (HsqlConnection connection = NewConnection())
            using (HsqlCommand testSubject = connection.CreateCommand())
            {
                UpdateRowSource expected = UpdateRowSource.Both;
                UpdateRowSource actual = testSubject.UpdatedRowSource;

                Assert.AreEqual(expected, actual);

                Assert.Fail("TODO");
            }
        }
    }
}
