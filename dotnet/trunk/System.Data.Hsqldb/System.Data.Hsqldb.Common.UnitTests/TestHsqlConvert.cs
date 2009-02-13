using System;
using NUnit.Framework;
using System.Data.Hsqldb.TestCoverage;
using System.Data.Hsqldb.Common.Enumeration;

namespace System.Data.Hsqldb.Common.UnitTests
{
    [TestFixture, ForSubject(typeof(HsqlConvert))]
    public class TestHsqlConvert
    {
        [Test, OfMember("InvalidConversion")]
        public void InvalidConversion()
        {
            try
            {
                throw HsqlConvert.InvalidConversion(java.sql.Types.DATALINK);
            }
            catch (HsqlDataSourceException hdse)
            {
                Assert.AreEqual(org.hsqldb.Trace.INVALID_CONVERSION, -hdse.ErrorCode);
            }
        }

        [Test, OfMember("NumericValueOutOfRange")]
        public void NumericValueOutOfRange()
        {
            object n = 42L;

            try
            {
                throw HsqlConvert.NumericValueOutOfRange(n);
            }
            catch (HsqlDataSourceException hdse)
            {
                Assert.AreEqual(org.hsqldb.Trace.NUMERIC_VALUE_OUT_OF_RANGE, -hdse.ErrorCode);
            }
        }

        

        [Test, OfMember("ToDataType")]
        public void ToDataType()
        {
            Type dataType = HsqlConvert.ToDataType(HsqlProviderType.BigInt);

            Assert.AreEqual(typeof(long), dataType);

            Assert.Fail("TODO");
        }

        [Test, OfMember("ToDbType")]
        public void ToDbType()
        {
            DbType actual = HsqlConvert.ToDbType(HsqlProviderType.BigInt);

            Assert.AreEqual(DbType.Int64, actual);

            Assert.Fail("TODO");
        }

        [Test, OfMember("ToHsqlIsolationLevel")]
        public void ToHsqlIsolationLevel()
        {
            Assert.AreEqual(HsqlIsolationLevel.ReadCommited, HsqlConvert.ToHsqlIsolationLevel(IsolationLevel.ReadCommitted));
            Assert.AreEqual(HsqlIsolationLevel.ReadCommited, HsqlConvert.ToHsqlIsolationLevel(IsolationLevel.Unspecified));
            //
            Assert.AreEqual(HsqlIsolationLevel.ReadUncommited, HsqlConvert.ToHsqlIsolationLevel(IsolationLevel.ReadUncommitted));
            Assert.AreEqual(HsqlIsolationLevel.RepeatableRead, HsqlConvert.ToHsqlIsolationLevel(IsolationLevel.RepeatableRead));
            Assert.AreEqual(HsqlIsolationLevel.Serializable, HsqlConvert.ToHsqlIsolationLevel(IsolationLevel.Serializable));

            try
            {
                HsqlConvert.ToHsqlIsolationLevel(IsolationLevel.Chaos);

                Assert.Fail("There is no HsqlIsolationLevel corresponding to IsolationLevel.Chaos"); 
            }
            catch (ArgumentException ae)
            {
                Assert.AreEqual("isolationLevel", ae.ParamName);
                Assert.AreEqual(string.Format("Unsupported: (0) ", IsolationLevel.Chaos), ae.Message); 
            }

            try
            {
                HsqlConvert.ToHsqlIsolationLevel(IsolationLevel.Snapshot);

                Assert.Fail("There is no HsqlIsolationLevel corresponding to IsolationLevel.Snapshot"); 
            }
            catch (ArgumentException ae)
            {
                Assert.AreEqual("isolationLevel", ae.ParamName);
                Assert.AreEqual(string.Format("Unsupported: (0) ", IsolationLevel.Snapshot), ae.Message); 
            }
        }

        [Test, OfMember("ToHsqlProviderType")]
        public void ToHsqlProviderType()
        {
            HsqlProviderType actual = HsqlConvert.ToHsqlProviderType(DbType.Int64);

            Assert.AreEqual(HsqlProviderType.BigInt, actual);

            Assert.Fail("TODO");
        }

        [Test, OfMember("ToIsolationLevel")]
        public void ToIsolationLevel()
        {
            IsolationLevel actual  = HsqlConvert.ToIsolationLevel(HsqlIsolationLevel.ReadCommited);

            Assert.AreEqual(IsolationLevel.ReadCommitted, actual);

            Assert.Fail("TODO");
        }

        [Test, OfMember("ToParameterDirection")]
        public void ToParameterDirection()
        {
            ParameterDirection? actual = HsqlConvert.ToParameterDirection(ParameterMode.In);

            Assert.AreEqual(ParameterDirection.Input, actual);

            Assert.Fail("TODO");
        }

        [Test, OfMember("ToProviderSpecificDataType")]
        public void ToProviderSpecificDataType()
        {
            Type dataType = HsqlConvert.ToProviderSpecificDataType(java.sql.Types.BIGINT);

            Assert.AreEqual(typeof(long), dataType);

            Assert.Fail("TODO");
        }

        [Test, OfMember("ToSqlLiteral")]
        public void ToSqlLiteral()
        {
            //IDataParameter parameter;

            //string actual = HsqlConvert.ToSqlLiteral(parameter);
            
            Assert.Fail("TODO");
        }

        [Test, OfMember("UnknownConversion")]
        public void UnknownConversion()
        {
            object o = new object();
            int targetType = java.sql.Types.BIGINT;


            try
            {
                throw HsqlConvert.UnknownConversion(o, targetType);
            }
            catch (HsqlDataSourceException hdse)
            {
                Assert.AreEqual(org.hsqldb.Trace.INVALID_CONVERSION, -hdse.ErrorCode);
            }
        }

        [Test, OfMember("WrongDataType")]
        public void WrongDataType()
        {
            object o = new object();

            try
            {
                throw HsqlConvert.WrongDataType(o);
            }
            catch (HsqlDataSourceException hdse)
            {
                Assert.AreEqual(org.hsqldb.Trace.WRONG_DATA_TYPE, -hdse.ErrorCode);
            }
        }
    }
}
