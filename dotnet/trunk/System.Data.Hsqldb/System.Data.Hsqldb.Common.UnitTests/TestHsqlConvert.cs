using System;
using NUnit.Framework;
using System.Data.Hsqldb.TestCoverage;
using System.Data.Hsqldb.Common.Enumeration;
using System.Data.SqlTypes;

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
            Assert.AreEqual(typeof(object), HsqlConvert.ToDataType(HsqlProviderType.Array));
            Assert.AreEqual(typeof(long), HsqlConvert.ToDataType(HsqlProviderType.BigInt));
            Assert.AreEqual(typeof(byte[]), HsqlConvert.ToDataType(HsqlProviderType.Binary));
            Assert.AreEqual(typeof(object), HsqlConvert.ToDataType(HsqlProviderType.Blob));
            Assert.AreEqual(typeof(bool), HsqlConvert.ToDataType(HsqlProviderType.Boolean));
            Assert.AreEqual(typeof(string), HsqlConvert.ToDataType(HsqlProviderType.Char));
            Assert.AreEqual(typeof(object), HsqlConvert.ToDataType(HsqlProviderType.Clob));
            Assert.AreEqual(typeof(object), HsqlConvert.ToDataType(HsqlProviderType.DataLink));
            Assert.AreEqual(typeof(DateTime), HsqlConvert.ToDataType(HsqlProviderType.Date));
            Assert.AreEqual(typeof(decimal), HsqlConvert.ToDataType(HsqlProviderType.Decimal));
            Assert.AreEqual(typeof(object), HsqlConvert.ToDataType(HsqlProviderType.Distinct));
            Assert.AreEqual(typeof(double), HsqlConvert.ToDataType(HsqlProviderType.Double));
            Assert.AreEqual(typeof(double), HsqlConvert.ToDataType(HsqlProviderType.Float));
            Assert.AreEqual(typeof(int), HsqlConvert.ToDataType(HsqlProviderType.Integer));
            Assert.AreEqual(typeof(object), HsqlConvert.ToDataType(HsqlProviderType.JavaObject));
            Assert.AreEqual(typeof(byte[]), HsqlConvert.ToDataType(HsqlProviderType.LongVarBinary));
            Assert.AreEqual(typeof(string), HsqlConvert.ToDataType(HsqlProviderType.LongVarChar));
            Assert.AreEqual(typeof(object), HsqlConvert.ToDataType(HsqlProviderType.Null));
            Assert.AreEqual(typeof(decimal), HsqlConvert.ToDataType(HsqlProviderType.Numeric));
            Assert.AreEqual(typeof(object), HsqlConvert.ToDataType(HsqlProviderType.Object));
            Assert.AreEqual(typeof(float), HsqlConvert.ToDataType(HsqlProviderType.Real));
            Assert.AreEqual(typeof(object), HsqlConvert.ToDataType(HsqlProviderType.Ref));
            Assert.AreEqual(typeof(short), HsqlConvert.ToDataType(HsqlProviderType.SmallInt));
            Assert.AreEqual(typeof(object), HsqlConvert.ToDataType(HsqlProviderType.Struct));
            Assert.AreEqual(typeof(DateTime), HsqlConvert.ToDataType(HsqlProviderType.Time));
            Assert.AreEqual(typeof(DateTime), HsqlConvert.ToDataType(HsqlProviderType.TimeStamp));
            Assert.AreEqual(typeof(sbyte), HsqlConvert.ToDataType(HsqlProviderType.TinyInt));
            Assert.AreEqual(typeof(byte[]), HsqlConvert.ToDataType(HsqlProviderType.VarBinary));
            Assert.AreEqual(typeof(string), HsqlConvert.ToDataType(HsqlProviderType.VarChar));
            Assert.AreEqual(typeof(string), HsqlConvert.ToDataType(HsqlProviderType.Xml));
        }

        [Test, OfMember("ToDbType")]
        public void ToDbType()
        {
            Assert.AreEqual(DbType.Object, HsqlConvert.ToDbType(HsqlProviderType.Array));
            Assert.AreEqual(DbType.Int64, HsqlConvert.ToDbType(HsqlProviderType.BigInt));
            Assert.AreEqual(DbType.Binary, HsqlConvert.ToDbType(HsqlProviderType.Binary));
            Assert.AreEqual(DbType.Object, HsqlConvert.ToDbType(HsqlProviderType.Blob));
            Assert.AreEqual(DbType.Boolean, HsqlConvert.ToDbType(HsqlProviderType.Boolean));
            Assert.AreEqual(DbType.StringFixedLength, HsqlConvert.ToDbType(HsqlProviderType.Char));
            Assert.AreEqual(DbType.Object, HsqlConvert.ToDbType(HsqlProviderType.Clob));
            Assert.AreEqual(DbType.Object, HsqlConvert.ToDbType(HsqlProviderType.DataLink));
            Assert.AreEqual(DbType.DateTime, HsqlConvert.ToDbType(HsqlProviderType.Date));
            Assert.AreEqual(DbType.Decimal, HsqlConvert.ToDbType(HsqlProviderType.Decimal));
            Assert.AreEqual(DbType.Object, HsqlConvert.ToDbType(HsqlProviderType.Distinct));
            Assert.AreEqual(DbType.Double, HsqlConvert.ToDbType(HsqlProviderType.Double));
            Assert.AreEqual(DbType.Double, HsqlConvert.ToDbType(HsqlProviderType.Float));
            Assert.AreEqual(DbType.Int32, HsqlConvert.ToDbType(HsqlProviderType.Integer));
            Assert.AreEqual(DbType.Object, HsqlConvert.ToDbType(HsqlProviderType.JavaObject));
            Assert.AreEqual(DbType.Object, HsqlConvert.ToDbType(HsqlProviderType.LongVarBinary));
            Assert.AreEqual(DbType.String, HsqlConvert.ToDbType(HsqlProviderType.LongVarChar));
            Assert.AreEqual(DbType.Object, HsqlConvert.ToDbType(HsqlProviderType.Null));
            Assert.AreEqual(DbType.VarNumeric, HsqlConvert.ToDbType(HsqlProviderType.Numeric));
            Assert.AreEqual(DbType.Object, HsqlConvert.ToDbType(HsqlProviderType.Object));
            Assert.AreEqual(DbType.Single, HsqlConvert.ToDbType(HsqlProviderType.Real));
            Assert.AreEqual(DbType.Object, HsqlConvert.ToDbType(HsqlProviderType.Ref));
            Assert.AreEqual(DbType.Int16, HsqlConvert.ToDbType(HsqlProviderType.SmallInt));
            Assert.AreEqual(DbType.Object, HsqlConvert.ToDbType(HsqlProviderType.Struct));
            Assert.AreEqual(DbType.DateTime, HsqlConvert.ToDbType(HsqlProviderType.Time));
            Assert.AreEqual(DbType.DateTime2, HsqlConvert.ToDbType(HsqlProviderType.TimeStamp));
            Assert.AreEqual(DbType.SByte, HsqlConvert.ToDbType(HsqlProviderType.TinyInt));
            Assert.AreEqual(DbType.Object, HsqlConvert.ToDbType(HsqlProviderType.VarBinary));
            Assert.AreEqual(DbType.String, HsqlConvert.ToDbType(HsqlProviderType.VarChar));
            Assert.AreEqual(DbType.Xml, HsqlConvert.ToDbType(HsqlProviderType.Xml));
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
                HsqlIsolationLevel actual = HsqlConvert.ToHsqlIsolationLevel(IsolationLevel.Chaos);

                Assert.Fail("There is no HsqlIsolationLevel corresponding to IsolationLevel.Chaos"); 
            }
            catch (ArgumentException ae)
            {
                Assert.AreEqual("isolationLevel", ae.ParamName);
                Assert.AreEqual(string.Format("Unsupported: (0) ", IsolationLevel.Chaos), ae.Message); 
            }

            try
            {
                HsqlIsolationLevel actual = HsqlConvert.ToHsqlIsolationLevel(IsolationLevel.Snapshot);

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
            Assert.AreEqual(HsqlProviderType.VarChar, HsqlConvert.ToHsqlProviderType(DbType.AnsiString));
            Assert.AreEqual(HsqlProviderType.Char, HsqlConvert.ToHsqlProviderType(DbType.AnsiStringFixedLength));
            Assert.AreEqual(HsqlProviderType.Binary, HsqlConvert.ToHsqlProviderType(DbType.Binary));
            Assert.AreEqual(HsqlProviderType.Boolean, HsqlConvert.ToHsqlProviderType(DbType.Boolean));
            Assert.AreEqual(HsqlProviderType.SmallInt, HsqlConvert.ToHsqlProviderType(DbType.Byte));
            Assert.AreEqual(HsqlProviderType.Decimal, HsqlConvert.ToHsqlProviderType(DbType.Currency));
            Assert.AreEqual(HsqlProviderType.TimeStamp, HsqlConvert.ToHsqlProviderType(DbType.Date));
            Assert.AreEqual(HsqlProviderType.TimeStamp, HsqlConvert.ToHsqlProviderType(DbType.DateTime));
            Assert.AreEqual(HsqlProviderType.TimeStamp, HsqlConvert.ToHsqlProviderType(DbType.DateTime2));
            Assert.AreEqual(HsqlProviderType.Char, HsqlConvert.ToHsqlProviderType(DbType.DateTimeOffset));
            Assert.AreEqual(HsqlProviderType.Decimal, HsqlConvert.ToHsqlProviderType(DbType.Decimal));
            Assert.AreEqual(HsqlProviderType.Double, HsqlConvert.ToHsqlProviderType(DbType.Double));
            Assert.AreEqual(HsqlProviderType.Binary, HsqlConvert.ToHsqlProviderType(DbType.Guid));
            Assert.AreEqual(HsqlProviderType.SmallInt, HsqlConvert.ToHsqlProviderType(DbType.Int16));
            Assert.AreEqual(HsqlProviderType.Integer, HsqlConvert.ToHsqlProviderType(DbType.Int32));
            Assert.AreEqual(HsqlProviderType.BigInt, HsqlConvert.ToHsqlProviderType(DbType.Int64));
            Assert.AreEqual(HsqlProviderType.Object, HsqlConvert.ToHsqlProviderType(DbType.Object));
            Assert.AreEqual(HsqlProviderType.TinyInt, HsqlConvert.ToHsqlProviderType(DbType.SByte));
            Assert.AreEqual(HsqlProviderType.Real, HsqlConvert.ToHsqlProviderType(DbType.Single));
            Assert.AreEqual(HsqlProviderType.VarChar, HsqlConvert.ToHsqlProviderType(DbType.String));
            Assert.AreEqual(HsqlProviderType.Char, HsqlConvert.ToHsqlProviderType(DbType.StringFixedLength));
            Assert.AreEqual(HsqlProviderType.TimeStamp, HsqlConvert.ToHsqlProviderType(DbType.Time));
            Assert.AreEqual(HsqlProviderType.Integer, HsqlConvert.ToHsqlProviderType(DbType.UInt16));
            Assert.AreEqual(HsqlProviderType.BigInt, HsqlConvert.ToHsqlProviderType(DbType.UInt32));
            Assert.AreEqual(HsqlProviderType.Numeric, HsqlConvert.ToHsqlProviderType(DbType.UInt64));
            Assert.AreEqual(HsqlProviderType.Numeric, HsqlConvert.ToHsqlProviderType(DbType.VarNumeric));
            Assert.AreEqual(HsqlProviderType.Xml, HsqlConvert.ToHsqlProviderType(DbType.Xml));
        }

        [Test, OfMember("ToIsolationLevel")]
        public void ToIsolationLevel()
        {
            Assert.AreEqual(IsolationLevel.Unspecified, HsqlConvert.ToIsolationLevel(HsqlIsolationLevel.None));
            Assert.AreEqual(IsolationLevel.ReadCommitted, HsqlConvert.ToIsolationLevel(HsqlIsolationLevel.ReadCommited));
            Assert.AreEqual(IsolationLevel.ReadUncommitted, HsqlConvert.ToIsolationLevel(HsqlIsolationLevel.ReadUncommited));
            Assert.AreEqual(IsolationLevel.RepeatableRead, HsqlConvert.ToIsolationLevel(HsqlIsolationLevel.RepeatableRead));
            Assert.AreEqual(IsolationLevel.Serializable, HsqlConvert.ToIsolationLevel(HsqlIsolationLevel.Serializable));
        }

        [Test, OfMember("ToParameterDirection")]
        public void ToParameterDirection()
        {
            Assert.AreEqual(ParameterDirection.Input, HsqlConvert.ToParameterDirection(ParameterMode.In));
            Assert.AreEqual(ParameterDirection.InputOutput, HsqlConvert.ToParameterDirection(ParameterMode.InOut));
            Assert.AreEqual(ParameterDirection.Output, HsqlConvert.ToParameterDirection(ParameterMode.Out));
            Assert.AreEqual(null, HsqlConvert.ToParameterDirection(ParameterMode.Unknown));
        }

        [Test, OfMember("ToProviderSpecificDataType")]
        public void ToProviderSpecificDataType()
        {
            Assert.AreEqual(typeof(java.sql.Array), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.Array));
            Assert.AreEqual(typeof(java.lang.Long), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.BigInt));
            Assert.AreEqual(typeof(byte[]), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.Binary));
            Assert.AreEqual(typeof(System.Data.Hsqldb.Common.Lob.IBlob), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.Blob));
            Assert.AreEqual(typeof(java.lang.Boolean), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.Boolean));
            Assert.AreEqual(typeof(string), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.Char));
            Assert.AreEqual(typeof(System.Data.Hsqldb.Common.Lob.IClob), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.Clob));
            Assert.AreEqual(typeof(java.net.URL), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.DataLink));
            Assert.AreEqual(typeof(java.sql.Date), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.Date));
            Assert.AreEqual(typeof(java.math.BigDecimal), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.Decimal));
            Assert.AreEqual(typeof(object), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.Distinct));
            Assert.AreEqual(typeof(java.lang.Double), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.Double));
            Assert.AreEqual(typeof(java.lang.Double), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.Float));
            Assert.AreEqual(typeof(java.lang.Integer), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.Integer));
            Assert.AreEqual(typeof(java.lang.Object), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.JavaObject));
            Assert.AreEqual(typeof(byte[]), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.LongVarBinary));
            Assert.AreEqual(typeof(string), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.LongVarChar));
            Assert.AreEqual(typeof(void), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.Null));
            Assert.AreEqual(typeof(java.math.BigDecimal), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.Numeric));
            Assert.AreEqual(typeof(System.Data.Hsqldb.Common.Sql.Type.SqlObject), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.Object));
            Assert.AreEqual(typeof(java.lang.Double), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.Real));
            Assert.AreEqual(typeof(java.sql.Ref), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.Ref));
            Assert.AreEqual(typeof(java.lang.Integer), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.SmallInt));
            Assert.AreEqual(typeof(java.sql.Struct), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.Struct));
            Assert.AreEqual(typeof(java.sql.Time), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.Time));
            Assert.AreEqual(typeof(java.sql.Timestamp), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.TimeStamp));
            Assert.AreEqual(typeof(java.lang.Integer), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.TinyInt));
            Assert.AreEqual(typeof(byte[]), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.VarBinary));
            Assert.AreEqual(typeof(string), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.VarChar));
            Assert.AreEqual(typeof(string), HsqlConvert.ToProviderSpecificDataType(org.hsqldb.Types.VARCHAR_IGNORECASE));
            Assert.AreEqual(typeof(java.sql.SQLXML), HsqlConvert.ToProviderSpecificDataType(HsqlProviderType.Xml));
        }

        [Test, OfMember("ToSqlLiteral")]
        public void ToSqlLiteral()
        {
            Assert.AreEqual("'foo'", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.AnsiString, "foo")));
            Assert.AreEqual("'foo'", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.AnsiStringFixedLength, "foo")));
            //--
            Assert.AreEqual("CAST('0123456789abcdef' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, new byte[] {
                (byte)1,(byte)35, (byte)69, (byte) 103, (byte) 137, (byte) 171, (byte) 205, (byte) 239})));
            Assert.AreEqual("CAST('0123456789abcdef' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, "0123456789abcdef")));

            long longBits = java.lang.Double.doubleToRawLongBits(123456789.987654321D);

            byte[] longBytes = new byte[]
                    {
                        (byte) ((longBits >> 56) & 0xff),
                        (byte) ((longBits >> 48) & 0xff),
                        (byte) ((longBits >> 40) & 0xff),
                        (byte) ((longBits >> 32) & 0xff),
                        (byte) ((longBits >> 24) & 0xff),
                        (byte) ((longBits >> 16) & 0xff),
                        (byte) ((longBits >> 8)  & 0xff),
                        (byte) ((longBits >> 0)  & 0xff)
                    };

            Assert.AreEqual("CAST('419d6f3457f35ba8' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, longBytes)));
            Assert.AreEqual("CAST('419d6f3457f35ba8' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, longBits)));
            Assert.AreEqual("CAST('419d6f3457f35ba8' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, 123456789.987654321D)));

            int intBits = java.lang.Float.floatToRawIntBits(123456789.987654321F);

            byte[] intBytes = new byte[]
                    {
                        (byte) ((intBits >> 24) & 0xff),
                        (byte) ((intBits >> 16) & 0xff),
                        (byte) ((intBits >> 8)  & 0xff),
                        (byte) ((intBits >> 0)  & 0xff)
                    };

            Assert.AreEqual("CAST('4ceb79a3' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, intBits)));
            Assert.AreEqual("CAST('4ceb79a3' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, intBytes)));
            Assert.AreEqual("CAST('4ceb79a3' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, 123456789.987654321F)));

            Assert.AreEqual("CAST('ffffffffffffffffffffffff00000000' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, decimal.MaxValue)));
            Assert.AreEqual("CAST('ffffffffffffffffffffffff80000000' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, decimal.MinValue)));

            Assert.AreEqual("CAST('00' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, (sbyte)0)));
            Assert.AreEqual("CAST('01' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, (sbyte)1)));
            Assert.AreEqual("CAST('80' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, sbyte.MinValue)));
            Assert.AreEqual("CAST('7f' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, sbyte.MaxValue)));

            Assert.AreEqual("CAST('00' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, (byte)0)));
            Assert.AreEqual("CAST('01' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, (byte)1)));
            Assert.AreEqual("CAST('00' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, byte.MinValue)));
            Assert.AreEqual("CAST('ff' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, byte.MaxValue)));

            Assert.AreEqual("CAST('0000' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, (char)0)));
            Assert.AreEqual("CAST('0001' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, (char)1)));
            Assert.AreEqual("CAST('0000' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, char.MinValue)));
            Assert.AreEqual("CAST('ffff' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, char.MaxValue)));

            Assert.AreEqual("CAST('0000' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, (short)0)));
            Assert.AreEqual("CAST('0001' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, (short)1)));
            Assert.AreEqual("CAST('8000' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, short.MinValue)));
            Assert.AreEqual("CAST('7fff' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, short.MaxValue)));

            Assert.AreEqual("CAST('0000' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, (ushort)0)));
            Assert.AreEqual("CAST('0001' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, (ushort)1)));
            Assert.AreEqual("CAST('0000' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, ushort.MinValue)));
            Assert.AreEqual("CAST('ffff' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, ushort.MaxValue)));

            Assert.AreEqual("CAST('00000000' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, (int)0)));
            Assert.AreEqual("CAST('00000001' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, (int)1)));
            Assert.AreEqual("CAST('80000000' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, int.MinValue)));
            Assert.AreEqual("CAST('7fffffff' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, int.MaxValue)));

            Assert.AreEqual("CAST('00000000' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, (uint)0)));
            Assert.AreEqual("CAST('00000001' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, (uint)1)));
            Assert.AreEqual("CAST('00000000' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, uint.MinValue)));
            Assert.AreEqual("CAST('ffffffff' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, uint.MaxValue)));

            Assert.AreEqual("CAST('0000000000000000' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, (long)0)));
            Assert.AreEqual("CAST('0000000000000001' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, (long)1)));
            Assert.AreEqual("CAST('8000000000000000' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, long.MinValue)));
            Assert.AreEqual("CAST('7fffffffffffffff' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, long.MaxValue)));

            Assert.AreEqual("CAST('0000000000000000' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, (ulong)0)));
            Assert.AreEqual("CAST('0000000000000001' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, (ulong)1)));
            Assert.AreEqual("CAST('0000000000000000' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, ulong.MinValue)));
            Assert.AreEqual("CAST('ffffffffffffffff' AS BINARY)", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Binary, ulong.MaxValue)));

            //--
            Assert.AreEqual("FALSE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, false)));
            Assert.AreEqual("FALSE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, "frue")));
            Assert.AreEqual("FALSE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (sbyte)0)));
            Assert.AreEqual("FALSE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (byte)0)));
            Assert.AreEqual("FALSE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (char)0)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (char)'0')));
            Assert.AreEqual("FALSE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (short)0)));
            Assert.AreEqual("FALSE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (ushort)0)));
            Assert.AreEqual("FALSE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (int)0)));
            Assert.AreEqual("FALSE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (uint)0)));
            Assert.AreEqual("FALSE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (long)0)));
            Assert.AreEqual("FALSE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (ulong)0)));
            Assert.AreEqual("FALSE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (float)0)));
            Assert.AreEqual("FALSE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (double)0)));
            Assert.AreEqual("FALSE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (decimal)0)));

            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, true)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, "TrUe")));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (sbyte)1)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (byte)1)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (short)1)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (ushort)1)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (int)1)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (uint)1)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (long)1)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (ulong)1)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (float)1)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (double)1)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (decimal)1)));

            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (sbyte)-1)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (byte)1)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (short)-1)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (ushort)2)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (int)-1)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (uint)2)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (long)-1)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (ulong)2)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (float)-1)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (double)-1)));
            Assert.AreEqual("TRUE", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Boolean, (decimal)-1)));
            //--

            Assert.AreEqual("1", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Byte, (sbyte)1)));
            Assert.AreEqual("1", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Byte, (byte)1)));
            Assert.AreEqual("1", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Byte, (short)1)));
            Assert.AreEqual("1", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Byte, (ushort)1)));
            Assert.AreEqual("1", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Byte, (int)1)));
            Assert.AreEqual("1", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Byte, (uint)1)));
            Assert.AreEqual("1", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Byte, (long)1)));
            Assert.AreEqual("1", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Byte, (ulong)1)));
            Assert.AreEqual("1", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Byte, (float)1)));
            Assert.AreEqual("1", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Byte, (double)1)));
            Assert.AreEqual("1", HsqlConvert.ToSqlLiteral(new FakeDataParameter(DbType.Byte, (decimal)1)));
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

    public class FakeDataParameter : IDataParameter
    {
        DbType m_dbType = DbType.Object;
        ParameterDirection m_parmeterDirection = ParameterDirection.Input;
        bool m_nullable = true;
        string m_parameterName;
        string m_sourceColumn;
        DataRowVersion m_sourceVersion = DataRowVersion.Current;
        object m_value;

        public FakeDataParameter() { }

        public FakeDataParameter(
            DbType dbType, 
            ParameterDirection parameterDirection, 
            bool nullable, 
            string parameterName, 
            string sourceColumn, 
            DataRowVersion sourceVersion, 
            object value)
        {
            m_dbType = dbType;
            m_parmeterDirection = parameterDirection;
            m_nullable = nullable;
            m_parameterName = parameterName;
            m_sourceColumn = sourceColumn;
            m_sourceVersion = sourceVersion;
            m_value = value;
        }

        public FakeDataParameter(DbType dbType, string parameterName, object value)
            : this(dbType, ParameterDirection.Input, true, null, null, DataRowVersion.Current, value) { }

        public FakeDataParameter(string parameterName, object value) 
            : this(DbType.Object, parameterName, value) { }

        public FakeDataParameter(DbType dbType, object value)
            : this(dbType, null, value) { }

        #region IDataParameter Members

        public DbType DbType
        {
            get { return m_dbType; }
            set { m_dbType = value; }
        }

        public ParameterDirection Direction
        {
            get { return m_parmeterDirection; }
            set { m_parmeterDirection = value; }
        }

        public bool IsNullable
        {
            get { return m_nullable; }
            set { m_nullable = value; }
        }

        public string ParameterName
        {
            get { return m_parameterName; }
            set { m_parameterName = value; }
        }

        public string SourceColumn
        {
            get { return m_sourceColumn; }
            set { m_sourceColumn = value; }
        }

        public DataRowVersion SourceVersion
        {
            get { return m_sourceVersion; }
            set { m_sourceVersion = value; }
        }

        public object Value
        {
            get { return m_value; }
            set { m_value = value; }
        }

        #endregion
    }
}
