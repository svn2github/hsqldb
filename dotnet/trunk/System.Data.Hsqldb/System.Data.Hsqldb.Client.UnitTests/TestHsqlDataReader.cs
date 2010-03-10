using System;
using System.Data.Hsqldb.TestCoverage;
using NUnit.Framework;
using System.Data.Hsqldb.Common.Enumeration;
using System.Data.Hsqldb.Common;
using System.Collections;
using System.Data.SqlTypes;
using System.IO;
using TestCategory = NUnit.Framework.CategoryAttribute;
using HsqlBinary = org.hsqldb.types.Binary;
using HsqlObject = org.hsqldb.types.JavaObject;
using HsqlResult = org.hsqldb.Result;
using HsqlTypes = org.hsqldb.Types;
using JavaByte = java.lang.Byte;
using JavaBoolean = java.lang.Boolean;
using JavaDate = java.sql.Date;
using JavaDecimal = java.math.BigDecimal;
using JavaDouble = java.lang.Double;
using JavaInteger = java.lang.Integer;
using JavaBigInteger = java.math.BigInteger;
using JavaShort = java.lang.Short;
using JavaTime = java.sql.Time;
using JavaTimestamp = java.sql.Timestamp;
using JavaLong = java.lang.Long;

namespace System.Data.Hsqldb.Client.UnitTests
{
    [TestFixture, TestCategory("DbDataDreader"),ForSubject(typeof(HsqlDataReader))]
    public class TestHsqlDataReader
    {
        static class ColumnOrdinalFor
        {
            public const int Array = 0;
            public const int Bigint = 1;
            public const int Binary = 2;
            public const int Blob = 3;
            public const int Boolean = 4;
            public const int Char = 5;
            public const int Clob = 6;
            public const int DataLink = 7;
            public const int Date = 8;
            public const int Decimal = 9;
            public const int Distinct = 10;
            public const int Double = 11;
            public const int Float = 12;
            public const int Integer = 13;
            public const int JavaObject = 14;
            public const int LongVarBinary = 15;
            public const int LongVarChar = 16;
            public const int Null = 17;
            public const int Numeric = 18;
            public const int Object = 19;
            public const int Real = 20;
            public const int Ref = 21;
            public const int SmallInt = 22;
            public const int Struct = 23;
            public const int Time = 24;
            public const int Timestamp = 25;
            public const int TinyInt = 26;
            public const int VarBinary = 27;
            public const int VarChar = 28;
            public const int Xml = 29;
            public const int Guid = 30;
        }

        static readonly object[][] columns = new object[][] {
            new object[]{HsqlProviderType.Array, null},
            new object[]{HsqlProviderType.BigInt, new JavaLong(JavaLong.MIN_VALUE)},
            new object[]{HsqlProviderType.Binary, new HsqlBinary(new byte[]{1,2,3,4}, false)},
            new object[]{HsqlProviderType.Blob, null},
            new object[]{HsqlProviderType.Boolean, JavaBoolean.TRUE},
            new object[]{HsqlProviderType.Char, "Y"},
            new object[]{HsqlProviderType.Clob, null},
            new object[]{HsqlProviderType.DataLink, null},
            new object[]{HsqlProviderType.Date, JavaDate.valueOf("2009-02-03")},
            new object[]{HsqlProviderType.Decimal, new JavaDecimal("1.0000")},
            new object[]{HsqlProviderType.Distinct, null},
            new object[]{HsqlProviderType.Double, new JavaDouble(JavaDouble.MIN_VALUE)},
            new object[]{HsqlProviderType.Float, new JavaDouble(JavaDouble.MAX_VALUE)},
            new object[]{HsqlProviderType.Integer, new JavaInteger(JavaInteger.MIN_VALUE)},
            new object[]{HsqlProviderType.JavaObject, null}, 
            new object[]{HsqlProviderType.LongVarBinary, new HsqlBinary(new byte[]{1,2,3,4}, false)},
            new object[]{HsqlProviderType.LongVarChar, "longvarchar"},
            new object[]{HsqlProviderType.Null, null},
            new object[]{HsqlProviderType.Numeric, new JavaDecimal("1.0000")},
            new object[]{HsqlProviderType.Object, new HsqlObject(new JavaBigInteger("1234"))},
            new object[]{HsqlProviderType.Real, new JavaDouble(1D)},
            new object[]{HsqlProviderType.Ref, null},
            new object[]{HsqlProviderType.SmallInt, new JavaInteger(JavaShort.MIN_VALUE)},
            new object[]{HsqlProviderType.Struct, null},
            new object[]{HsqlProviderType.Time, JavaTime.valueOf("12:00:00")},
            new object[]{HsqlProviderType.TimeStamp, JavaTimestamp.valueOf("2009-02-03 20:41:45.546729")},
            new object[]{HsqlProviderType.TinyInt, new JavaInteger(JavaByte.MIN_VALUE)},
            new object[]{HsqlProviderType.VarBinary, new HsqlBinary(new byte[]{1,2,3,4}, false)},
            new object[]{HsqlProviderType.VarChar, "varchar"},
            new object[]{HsqlProviderType.Xml, null},
            new object[]{HsqlProviderType.Binary, Guid.Empty.ToByteArray()}
        };

        static HsqlResult NewResult()
        {
            int columnCount = columns.GetUpperBound(1);
            HsqlResult result = new HsqlResult(columnCount);
            object[] row = new object[columnCount];
            int i = 0;
            const int TypeOrdinal = 0;
            const int ValueOrdinal = 1;

            result.add(row);

            foreach (object[] column in columns)
            {
                HsqlProviderType dataType = (HsqlProviderType)column[TypeOrdinal];
                row[i] = column[ValueOrdinal];
                int scale = 0;

                switch(dataType) 
                {
                    case HsqlProviderType.Decimal:
                    case HsqlProviderType.Numeric: {
                        scale = 4;
                        break;
                    }
                }

                int size = HsqlTypes.getPrecision((int)dataType);

                result.metaData.catalogNames[i] = "mem:test";
                result.metaData.classNames[i] = HsqlTypes.getTypeName((int)dataType);
                result.metaData.colLabels[i] = "COLUMN_" + i;
                result.metaData.colNames[i] = "C" + i;
                result.metaData.colNullable[i] = (int) BaseColumnNullability.Nullable;
                result.metaData.colScales[i] = scale;
                result.metaData.colSizes[i] = size;
                result.metaData.colTypes[i] = (int) dataType;
                result.metaData.isIdentity[i] = false;
                result.metaData.isLabelQuoted[i] = false;
                result.metaData.isWritable[i] = false;
                result.metaData.paramMode[i] = (int) ParameterMode.Unknown;
                result.metaData.schemaNames[i] = "PUBLIC";
                result.metaData.tableNames[i] = "ALL_COL_TYPES";
            }

            return result;
        }

        static readonly HsqlResult ResultInstance = NewResult();

        static HsqlDataReader NewTestSubject()
        {
            return new HsqlDataReader(ResultInstance);
        }

        [Test, OfMember("Close")]
        public void Close()
        {
            HsqlDataReader testSubject = NewTestSubject();

            testSubject.Close();

            Assert.Fail("TODO");
        }

        [Test, OfMember("FromByteArray")]
        public void FromByteArray()
        {
            HsqlDataReader testSubject = NewTestSubject();

            byte[] bytes = HsqlDataReader.ToByteArray(testSubject);

            HsqlDataReader actual = HsqlDataReader.FromByteArray(bytes);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetBoolean")]
        public void GetBoolean()
        {
            HsqlDataReader testSubject = NewTestSubject();

            testSubject.GetBoolean(ColumnOrdinalFor.Boolean);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetByte")]

        public void GetByte()
        {
            HsqlDataReader testSubject = NewTestSubject();

            byte actual = testSubject.GetByte(ColumnOrdinalFor.TinyInt);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetBytes")]
        public void GetBytes()
        {
            HsqlDataReader testSubject = NewTestSubject();
            byte[] buffer = new byte[4];

            long bytesRead = testSubject.GetBytes(ColumnOrdinalFor.Binary, 0, buffer, 0, 4);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetChar")]
        public void GetChar()
        {
            HsqlDataReader testSubject = NewTestSubject();

            char actual = testSubject.GetChar(ColumnOrdinalFor.Char);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetChars")]
        public void GetChars()
        {
            HsqlDataReader testSubject = NewTestSubject();
            char[] buffer = new char[4];

            long charsRead = testSubject.GetChars(ColumnOrdinalFor.LongVarChar, 0, buffer, 0, buffer.Length);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetData")]
        public void GetData()
        {
            //HsqlDataReader testSubject = NewTestSubject();

            //testSubject.GetData(ordinal);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetDataTypeName")]
        public void GetDataTypeName()
        {
            HsqlDataReader testSubject = NewTestSubject();

            string actual = testSubject.GetDataTypeName(ColumnOrdinalFor.Bigint);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetDateTime")]
        public void GetDateTime()
        {
            HsqlDataReader testSubject = NewTestSubject();

            DateTime actualDate = testSubject.GetDateTime(ColumnOrdinalFor.Date);
            DateTime actualTime = testSubject.GetDateTime(ColumnOrdinalFor.Time);
            DateTime actualTimestamp = testSubject.GetDateTime(ColumnOrdinalFor.Timestamp);

            Assert.Fail("TODO"); 
        }

        [Test, OfMember("GetDecimal")]
        public void GetDecimal()
        {
            HsqlDataReader testSubject = NewTestSubject();

            decimal actualDecimal = testSubject.GetDecimal(ColumnOrdinalFor.Decimal);
            decimal actualNumeric = testSubject.GetDecimal(ColumnOrdinalFor.Numeric);

            Assert.Fail("TODO");  
        }

        [Test, OfMember("GetDouble")]
        public void GetDouble()
        {
            HsqlDataReader testSubject = NewTestSubject();
            
            double actual = testSubject.GetDouble(ColumnOrdinalFor.Double);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetEnumerator")]
        public void GetEnumerator()
        {
            HsqlDataReader testSubject = NewTestSubject();

            foreach (IDataRecord row in testSubject)
            {
                int fieldCount = row.FieldCount;

                for (int i = 0; i < fieldCount; i++)
                {
                    object value = row.IsDBNull(i) ? null : row.GetValue(i);

                    if (value != null)
                    {
                        Assert.IsInstanceOfType(row.GetFieldType(i), value);
                    }
                }
            }

            Assert.Fail("TODO"); 
        }

        [Test, OfMember("GetFieldType")]
        public void GetFieldType()
        {
            HsqlDataReader testSubject = NewTestSubject();

            for (int i = 0; i < testSubject.FieldCount; i++)
            {
                Type actual = testSubject.GetFieldType(i);

                switch (i)
                {
                    case ColumnOrdinalFor.Array:
                        {
                            // TODO
                            break;
                        }
                    case ColumnOrdinalFor.Bigint:
                        {
                            Assert.AreEqual(typeof(long), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Binary:
                        {
                            Assert.AreEqual(typeof(byte[]), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Blob:
                        {
                            // TODO
                            break;
                        }
                    case ColumnOrdinalFor.Boolean:
                        {
                            Assert.AreEqual(typeof(bool), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Char:
                        {
                            Assert.AreEqual(typeof(string), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Clob:
                        {
                            // TODO
                            break;
                        }
                    case ColumnOrdinalFor.DataLink:
                        {
                            // TODO
                            break;
                        }
                    case ColumnOrdinalFor.Date:
                        {
                            Assert.AreEqual(typeof(DateTime), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Decimal:
                        {
                            Assert.AreEqual(typeof(decimal), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Distinct:
                        {
                            // TODO
                            break;
                        }
                    case ColumnOrdinalFor.Double:
                        {
                            Assert.AreEqual(typeof(double), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Float:
                        {
                            Assert.AreEqual(typeof(double), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Integer:
                        {
                            Assert.AreEqual(typeof(int), actual);

                            break;
                        }
                    case ColumnOrdinalFor.JavaObject:
                        {
                            // TODO
                            break;
                        }
                    case ColumnOrdinalFor.LongVarBinary:
                        {
                            Assert.AreEqual(typeof(byte[]), actual);

                            break;
                        }
                    case ColumnOrdinalFor.LongVarChar:
                        {
                            Assert.AreEqual(typeof(string), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Null:
                        {
                            break;
                        }
                    case ColumnOrdinalFor.Numeric:
                        {
                            Assert.AreEqual(typeof(decimal), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Object:
                        {
                            Assert.AreEqual(typeof(object), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Real:
                        {
                            Assert.AreEqual(typeof(float), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Ref:
                        {
                            // TODO
                            break;
                        }
                    case ColumnOrdinalFor.SmallInt:
                        {
                            Assert.AreEqual(typeof(short), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Struct:
                        {
                            // TODO
                            break;
                        }
                    case ColumnOrdinalFor.Time:
                        {
                            Assert.AreEqual(typeof(DateTime), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Timestamp:
                        {
                            Assert.AreEqual(typeof(DateTime), actual);

                            break;
                        }
                    case ColumnOrdinalFor.TinyInt:
                        {
                            Assert.AreEqual(typeof(sbyte), actual);

                            break;
                        }
                    case ColumnOrdinalFor.VarBinary:
                        {
                            Assert.AreEqual(typeof(byte[]), actual);

                            break;
                        }
                    case ColumnOrdinalFor.VarChar:
                        {
                            Assert.AreEqual(typeof(string), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Xml:
                        {
                            // TODO
                            break;
                        }
                }
            }
        }

        [Test, OfMember("GetFloat")]
        public void GetFloat()
        {
            HsqlDataReader testSubject = NewTestSubject();

            float actual = testSubject.GetFloat(ColumnOrdinalFor.Real);

            Assert.Fail("TODO"); 
        }

        [Test, OfMember("GetGuid")]

        public void GetGuid()
        {
            HsqlDataReader testSubject = NewTestSubject();

            Guid actual = testSubject.GetGuid(ColumnOrdinalFor.Guid);

            Assert.Fail("TODO");  
        }

        [Test, OfMember("GetInt16")]
        public void GetInt16()
        {
            HsqlDataReader testSubject = NewTestSubject();

            short actual = testSubject.GetInt16(ColumnOrdinalFor.SmallInt);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetInt32")]
        public void GetInt32()
        {
            HsqlDataReader testSubject = NewTestSubject();

            int actual = testSubject.GetInt32(ColumnOrdinalFor.Integer);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetInt64")]
        public void GetInt64()
        {
            HsqlDataReader testSubject = NewTestSubject();

            long actual = testSubject.GetInt64(ColumnOrdinalFor.Bigint);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetName")]
        public void GetName()
        {
            HsqlDataReader testSubject = NewTestSubject();

            for (int i = 0; i < testSubject.FieldCount; i++)
            {
                string actual = testSubject.GetName(i);

                Assert.AreEqual("COLUMN_" + i, actual);
            }
        }

        [Test, OfMember("GetOrdinal")]

        public void GetOrdinal()
        {
            HsqlDataReader testSubject = NewTestSubject();

            for (int i = 0; i < testSubject.FieldCount; i++)
            {
                int actual = testSubject.GetOrdinal("COLUMN_" + i);

                Assert.AreEqual(i, actual);
            }
        }

        [Test, OfMember("GetProviderSpecificFieldType")]
        public void GetProviderSpecificFieldType()
        {
            HsqlDataReader testSubject = NewTestSubject();

            for (int i = 0; i < testSubject.FieldCount; i++)
            {
                Type actual = testSubject.GetProviderSpecificFieldType(i);

                switch (i)
                {
                    case ColumnOrdinalFor.Array:
                        {
                            // TODO
                            break;
                        }
                    case ColumnOrdinalFor.Bigint:
                        {
                            Assert.AreEqual(typeof(java.lang.Long), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Binary:
                        {
                            Assert.AreEqual(typeof(org.hsqldb.types.Binary), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Blob:
                        {
                            // TODO
                            break;
                        }
                    case ColumnOrdinalFor.Boolean:
                        {
                            Assert.AreEqual(typeof(java.lang.Boolean), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Char:
                        {
                            Assert.AreEqual(typeof(string), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Clob:
                        {
                            // TODO
                            break;
                        }
                    case ColumnOrdinalFor.DataLink:
                        {
                            // TODO
                            break;
                        }
                    case ColumnOrdinalFor.Date:
                        {
                            Assert.AreEqual(typeof(java.sql.Date), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Decimal:
                        {
                            Assert.AreEqual(typeof(java.math.BigDecimal), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Distinct:
                        {
                            // TODO
                            break;
                        }
                    case ColumnOrdinalFor.Double:
                        {
                            Assert.AreEqual(typeof(java.lang.Double), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Float:
                        {
                            Assert.AreEqual(typeof(java.lang.Double), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Integer:
                        {
                            Assert.AreEqual(typeof(java.lang.Integer), actual);

                            break;
                        }
                    case ColumnOrdinalFor.JavaObject:
                        {
                            // TODO
                            break;
                        }
                    case ColumnOrdinalFor.LongVarBinary:
                        {
                            Assert.AreEqual(typeof(org.hsqldb.types.Binary), actual);

                            break;
                        }
                    case ColumnOrdinalFor.LongVarChar:
                        {
                            Assert.AreEqual(typeof(string), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Null:
                        {
                            break;
                        }
                    case ColumnOrdinalFor.Numeric:
                        {
                            Assert.AreEqual(typeof(java.math.BigDecimal), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Object:
                        {
                            Assert.AreEqual(typeof(org.hsqldb.types.JavaObject), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Real:
                        {
                            Assert.AreEqual(typeof(java.lang.Double), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Ref:
                        {
                            // TODO
                            break;
                        }
                    case ColumnOrdinalFor.SmallInt:
                        {
                            Assert.AreEqual(typeof(java.lang.Integer), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Struct:
                        {
                            // TODO
                            break;
                        }
                    case ColumnOrdinalFor.Time:
                        {
                            Assert.AreEqual(typeof(java.sql.Time), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Timestamp:
                        {
                            Assert.AreEqual(typeof(java.sql.Timestamp), actual);

                            break;
                        }
                    case ColumnOrdinalFor.TinyInt:
                        {
                            Assert.AreEqual(typeof(java.lang.Integer), actual);

                            break;
                        }
                    case ColumnOrdinalFor.VarBinary:
                        {
                            Assert.AreEqual(typeof(org.hsqldb.types.Binary), actual);

                            break;
                        }
                    case ColumnOrdinalFor.VarChar:
                        {
                            Assert.AreEqual(typeof(string), actual);

                            break;
                        }
                    case ColumnOrdinalFor.Xml:
                        {
                            // TODO
                            break;
                        }
                }
            }
        }

        [Test, OfMember("GetProviderSpecificValue")]
        public void GetProviderSpecificValue()
        {
            HsqlDataReader testSubject = NewTestSubject();

            testSubject.Read();

            for (int i = 0; i < testSubject.FieldCount; i++)
            {
                object actual = testSubject.GetProviderSpecificValue(i);
            }

            Assert.Fail("TODO"); 
        }

        [Test, OfMember("GetProviderSpecificValues")]
        public void GetProviderSpecificValues()
        {
            HsqlDataReader testSubject = NewTestSubject();

            object[] values = new object[testSubject.FieldCount];

            testSubject.Read();

            int fieldsRead = testSubject.GetProviderSpecificValues(values);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetSchemaTable")]

        public void GetSchemaTable()
        {
            HsqlDataReader testSubject = NewTestSubject();


            DataTable schemaTable = testSubject.GetSchemaTable();

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetSqlBinary")]
        public void GetSqlBinary()
        {
            HsqlDataReader testSubject = NewTestSubject();

            SqlBinary actual = testSubject.GetSqlBinary(ColumnOrdinalFor.Binary);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetSqlBoolean")]
        public void GetSqlBoolean()
        {
            HsqlDataReader testSubject = NewTestSubject();

            SqlBoolean actual = testSubject.GetSqlBoolean(ColumnOrdinalFor.Boolean);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetSqlByte")]
        public void GetSqlByte()
        {
            HsqlDataReader testSubject = NewTestSubject();

            SqlByte actual = testSubject.GetSqlByte(ColumnOrdinalFor.TinyInt);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetSqlBytes")]
        public void GetSqlBytes()
        {
            HsqlDataReader testSubject = NewTestSubject();

            SqlBytes actual = testSubject.GetSqlBytes(ColumnOrdinalFor.Binary);

            Assert.Fail("TODO"); 
        }

        [Test, OfMember("GetSqlChars")]
        public void GetSqlChars()
        {
            HsqlDataReader testSubject = NewTestSubject();

            SqlChars actual = testSubject.GetSqlChars(ColumnOrdinalFor.Char);

            Assert.Fail("TODO"); 
        }

        [Test, OfMember("GetSqlDateTime")]
        public void GetSqlDateTime()
        {
            HsqlDataReader testSubject = NewTestSubject();

            SqlDateTime actual = testSubject.GetSqlDateTime(ColumnOrdinalFor.Timestamp);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetSqlDecimal")]
        public void GetSqlDecimal()
        {
            HsqlDataReader testSubject = NewTestSubject();

            SqlDecimal actual = testSubject.GetSqlDecimal(ColumnOrdinalFor.Decimal);

            Assert.Fail("TODO"); 
        }

        [Test, OfMember("GetSqlDouble")]
        public void GetSqlDouble()
        {
            HsqlDataReader testSubject = NewTestSubject();

            SqlDouble actual = testSubject.GetSqlDouble(ColumnOrdinalFor.Double);

            Assert.Fail("TODO"); 
        }

        [Test, OfMember("GetSqlGuid")]
        public void GetSqlGuid()
        {
            HsqlDataReader testSubject = NewTestSubject();

            SqlGuid actual = testSubject.GetSqlGuid(ColumnOrdinalFor.Guid);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetSqlInt16")]
        public void GetSqlInt16()
        {
            HsqlDataReader testSubject = NewTestSubject();

            SqlInt16 actual = testSubject.GetSqlInt16(ColumnOrdinalFor.SmallInt);

            Assert.Fail("TODO"); 
        }

        [Test, OfMember("GetSqlInt32")]
        public void GetSqlInt32()
        {
            HsqlDataReader testSubject = NewTestSubject();

            SqlInt32 actual = testSubject.GetSqlInt32(ColumnOrdinalFor.Integer);

            Assert.Fail("TODO"); 
        }

        [Test, OfMember("GetSqlInt64")]
        public void GetSqlInt64()
        {
            HsqlDataReader testSubject = NewTestSubject();

            SqlInt64 actual = testSubject.GetSqlInt64(ColumnOrdinalFor.Bigint);

            Assert.Fail("TODO"); 
        }

        [Test, OfMember("GetSqlMoney")]
        public void GetSqlMoney()
        {
            HsqlDataReader testSubject = NewTestSubject();

            SqlMoney actual = testSubject.GetSqlMoney(ColumnOrdinalFor.Decimal);

            Assert.Fail("TODO");  
        }

        [Test, OfMember("GetSqlSingle")]
        public void GetSqlSingle()
        {
            HsqlDataReader testSubject = NewTestSubject();

            SqlSingle actual = testSubject.GetSqlSingle(ColumnOrdinalFor.Real);

            Assert.Fail("TODO");  
        }

        [Test, OfMember("GetSqlString")]

        public void GetSqlString()
        {
            HsqlDataReader testSubject = NewTestSubject();

            SqlString actual = testSubject.GetSqlString(ColumnOrdinalFor.VarChar);

            Assert.Fail("TODO"); 
        }

        [Test, OfMember("GetSqlValue")]

        public void GetSqlValue()
        {
            HsqlDataReader testSubject = NewTestSubject();

            testSubject.Read();

            for (int i = 0; i < testSubject.FieldCount; i++)
            {
                object actual = testSubject.GetSqlValue(i);
            }

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetSqlValues")]
        public void GetSqlValues()
        {
            HsqlDataReader testSubject = NewTestSubject();

            testSubject.Read();

            object[] values = new object[testSubject.FieldCount];

            int count = testSubject.GetSqlValues(values);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetSqlXml")]
        public void GetSqlXml()
        {
            HsqlDataReader testSubject = NewTestSubject();

            SqlXml actual = testSubject.GetSqlXml(ColumnOrdinalFor.Xml);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetString")]
        public void GetString()
        {
            HsqlDataReader testSubject = NewTestSubject();

            string actual = testSubject.GetString(ColumnOrdinalFor.VarChar);

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetValue")]
        public void GetValue()
        {
            HsqlDataReader testSubject = NewTestSubject();

            testSubject.Read();

            for (int i = 0; i < testSubject.FieldCount; i++)
            {
                object actual = testSubject.GetValue(i);
            }

            Assert.Fail("TODO");
        }

        [Test, OfMember("GetValues")]
        public void GetValues()
        {
            HsqlDataReader testSubject = NewTestSubject();

            testSubject.Read();

            object[] values = new object[testSubject.FieldCount];

            int count = testSubject.GetValues(values);

            Assert.Fail("TODO");
        }

        [Test, OfMember("IsDBNull")]
        public void IsDBNull()
        {
            HsqlDataReader testSubject = NewTestSubject();

            testSubject.Read();

            for (int i = 0; i < testSubject.FieldCount; i++)
            {
                bool actual = testSubject.IsDBNull(i);
            }

            Assert.Fail("TODO");
        }

        [Test, OfMember("NextResult")]
        public void NextResult()
        {
            HsqlDataReader testSubject = new HsqlDataReader(new int[] { 1, 2, 3, 4 });

            Assert.AreEqual(1, testSubject.RecordsAffected);
            Assert.That(testSubject.NextResult());
            Assert.AreEqual(2, testSubject.RecordsAffected);
            Assert.That(testSubject.NextResult());
            Assert.AreEqual(3, testSubject.RecordsAffected);
            Assert.That(testSubject.NextResult());
            Assert.AreEqual(4, testSubject.RecordsAffected);
            Assert.That(!testSubject.NextResult());
        }

        [Test, OfMember("Read")]
        public void Read()
        {
            HsqlDataReader testSubject = NewTestSubject();

            Assert.That(testSubject.Read());
        }

        [Test, OfMember("ReadFromStream")]

        public void ReadFromStream()
        {
            HsqlDataReader expected = NewTestSubject();

            MemoryStream ms = new MemoryStream(HsqlDataReader.ToByteArray(expected));

            HsqlDataReader.WriteToStream(ms, expected);

            ms.Position = 0;

            HsqlDataReader actual = HsqlDataReader.ReadFromStream(ms);

            Assert.AreEqual(expected.Depth, actual.Depth, "Depth");
            Assert.AreEqual(expected.FieldCount, actual.FieldCount, "FieldCount");
            Assert.AreEqual(expected.HasRows, actual.HasRows, "HasRows");
            Assert.AreEqual(expected.IsClosed, actual.IsClosed, "IsClosed");
            Assert.AreEqual(expected.RecordsAffected, actual.RecordsAffected, "RecordsAffected");
            Assert.AreEqual(expected.VisibleFieldCount, actual.VisibleFieldCount, "VisibleFieldCount");

            object[] expectedValues = new object[expected.FieldCount];
            object[] actualValues = new object[actual.FieldCount];

            while (expected.Read())
            {
                Assert.That(actual.Read());

                expected.GetValues(expectedValues);
                actual.GetValues(actualValues);

                for (int i = 0; i < expectedValues.Length; i++)
                {
                    Assert.AreEqual(expectedValues[i], actualValues[i]);
                }
            }       
        }

        [Test, OfMember("ReadResult")]

        public void ReadResult()
        {
            Assert.Fail("TODO");
        }

        [Test, OfMember("ToByteArray")]

        public void ToByteArray()
        {
            Assert.Fail("TODO"); 
        }

        [Test, OfMember("WriteResult")]

        public void WriteResult()
        {
            Assert.Fail("TODO"); 
        }

        [Test, OfMember("WriteToStream")]

        public void WriteToStream()
        {
            Assert.Fail("TODO"); 
        }
    }
}
