using System;
using NUnit.Framework;
using System.Data.Hsqldb.TestCoverage;
using System.Data.Hsqldb.Common.Enumeration;
using TestCategory = NUnit.Framework.CategoryAttribute;

namespace System.Data.Hsqldb.Client.UnitTests
{
    [TestFixture, TestCategory("DbCommand"), ForSubject(typeof(HsqlCommand))]
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

        [Test, OfMember("AddBatch")]
        public void AddBatch()
        {
            using (HsqlConnection connection = NewConnection())
            using (HsqlCommand testSubject = connection.CreateCommand())
            {
                testSubject.AddBatch();
                //testSubject.C
            }
        }

        [Test, OfMember("Cancel")]
        public void Cancel()
        {
            using (HsqlConnection connection = NewConnection())
            using (HsqlCommand testSubject = connection.CreateCommand())
            {
                testSubject.Cancel(); // no-op.
            }
        }

        [Test, OfMember("Clone")]
        public void Clone()
        {
            using (HsqlConnection connection = NewConnection())
            using (HsqlTransaction transaction = connection.BeginTransaction())
            using (HsqlCommand originalCommand = connection.CreateCommand())
            {
                originalCommand.CommandType = global::System.Data.CommandType.StoredProcedure;
                originalCommand.CommandText = "call 1 + cast(? as integer)";                
                originalCommand.DeriveParameters();
                
                HsqlCommand clonedCommand = originalCommand.Clone();

                Assert.AreEqual(originalCommand.CommandText, clonedCommand.CommandText);
                Assert.AreEqual(originalCommand.CommandTimeout, clonedCommand.CommandTimeout);
                Assert.AreEqual(originalCommand.CommandType, clonedCommand.CommandType);
                Assert.AreSame(connection, clonedCommand.Connection);
                Assert.AreEqual(originalCommand.DesignTimeVisible, clonedCommand.DesignTimeVisible);
                Assert.AreEqual(originalCommand.Parameters.Count, clonedCommand.Parameters.Count);
                
                for (int i = 0; i < originalCommand.Parameters.Count; i++)
                {

                    HsqlParameter orignalCommandParameter = originalCommand.Parameters[i];
                    HsqlParameter clonedCommandParameter = clonedCommand.Parameters[i];

                    Assert.AreEqual(orignalCommandParameter.DbType, clonedCommandParameter.DbType);
                    Assert.AreEqual(orignalCommandParameter.Direction, clonedCommandParameter.Direction);
                    Assert.AreEqual(orignalCommandParameter.IsNullable, clonedCommandParameter.IsNullable);
                    Assert.AreEqual(orignalCommandParameter.Offset, clonedCommandParameter.Offset);
                    Assert.AreEqual(orignalCommandParameter.ParameterName, clonedCommandParameter.ParameterName);
                    Assert.AreEqual(orignalCommandParameter.Precision, clonedCommandParameter.Precision);
                    Assert.AreEqual(orignalCommandParameter.ProviderType, clonedCommandParameter.ProviderType);
                    Assert.AreEqual(orignalCommandParameter.Scale, clonedCommandParameter.Scale);
                    Assert.AreEqual(orignalCommandParameter.Size, clonedCommandParameter.Size);
                    Assert.AreEqual(orignalCommandParameter.SourceColumn, clonedCommandParameter.SourceColumn);
                    Assert.AreEqual(orignalCommandParameter.SourceColumnNullMapping, clonedCommandParameter.SourceColumnNullMapping);
                    Assert.AreEqual(orignalCommandParameter.SourceVersion, clonedCommandParameter.SourceVersion);
                    Assert.AreEqual(orignalCommandParameter.ToSqlLiteral(), clonedCommandParameter.ToSqlLiteral());
                    Assert.AreEqual(orignalCommandParameter.Value, clonedCommandParameter.Value);
                }
                
                Assert.AreSame(originalCommand.Transaction, clonedCommand.Transaction);
                Assert.AreEqual(originalCommand.UpdatedRowSource, clonedCommand.UpdatedRowSource);
            }
        }

        [Test, OfMember("CommandText")]
        public void CommandText()
        {
            using (HsqlConnection connection = NewConnection())
            using (HsqlCommand testSubject = connection.CreateCommand())
            {
                Assert.AreEqual(string.Empty, testSubject.CommandText);

                testSubject.CommandText = "select * from information_schema.system_tables";

                Assert.AreEqual("select * from information_schema.system_tables", testSubject.CommandText);

                testSubject.CommandText = null;

                Assert.AreEqual(string.Empty, testSubject.CommandText);                
            }
        }

        [Test, OfMember("CommandTimeout")]
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

                testSubject.CommandText = "call 1 + cast(? as integer)";
                testSubject.CommandType = global::System.Data.CommandType.StoredProcedure;
                testSubject.DeriveParameters();
                
                HsqlParameterCollection parameters = testSubject.Parameters;

                Assert.AreEqual(1, parameters.Count);

                HsqlParameter parameter = parameters[0];

                Assert.AreEqual(DbType.Int32, parameter.DbType);
                Assert.AreEqual(ParameterDirection.Input, parameter.Direction);
                Assert.AreEqual(false, parameter.IsNullable);
                Assert.AreEqual(0, parameter.Offset);
                Assert.AreEqual("@p1", parameter.ParameterName);
                Assert.AreEqual(10, parameter.Precision);
                Assert.AreEqual(HsqlProviderType.Integer, parameter.ProviderType);
                Assert.AreEqual(0, parameter.Scale);
                Assert.AreEqual(4, parameter.Size);
                Assert.AreEqual("", parameter.SourceColumn);
                Assert.AreEqual(false, parameter.SourceColumnNullMapping);
                Assert.AreEqual(DataRowVersion.Default, parameter.SourceVersion);
                Assert.AreEqual("NULL", parameter.ToSqlLiteral());
                Assert.AreEqual(null, parameter.Value);
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

                //Assert.Fail("TODO");
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
                    object[] values = new object[reader.FieldCount];

                    while (reader.Read())
                    {
                        int fieldCount = reader.GetValues(values);

                        for (int i = 0; i < fieldCount; i++)
                        {
                            object value = values[i];
                            Console.Write(value);
                            Console.Write(" : ");
                        }
                        Console.WriteLine();
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

                    foreach (object value in values)
                    {
                        Console.Write(value);
                        Console.Write(" : ");
                    }
                    Console.WriteLine();
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
