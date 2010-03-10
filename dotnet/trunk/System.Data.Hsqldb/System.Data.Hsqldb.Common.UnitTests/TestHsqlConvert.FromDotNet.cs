#region Using
using System;
using System.Data.Hsqldb.TestCoverage;
using NUnit.Framework;
using TestSubject = System.Data.Hsqldb.Common.HsqlConvert.FromDotNet;
using org.hsqldb.store;
using System.Data.SqlTypes;
#endregion

namespace System.Data.Hsqldb.Common.UnitTests
{
    [TestFixture, ForSubject(typeof(HsqlConvert.FromDotNet))]
    public class TestHsqlConvertFromDotNet
    {

        [Test, OfMember("ToBigInt(bool)")]        
        public virtual void bool_ToBigInt()
        {
            Assert.AreEqual(ValuePool.getLong(0),TestSubject.ToBigInt(false));
            Assert.AreEqual(ValuePool.getLong(1), TestSubject.ToBigInt(true));
        }

        [Test, OfMember("ToBigInt(byte)")]        
        public virtual void byte_ToBigInt()
        {
            byte byteValue = default(byte);

            TestSubject.ToBigInt(byteValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBigInt(INullable)")]        
        public virtual void INullable_ToBigInt()
        {
            INullable nullable = default(INullable);

            TestSubject.ToBigInt(nullable);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBigInt(decimal)")]        
        public virtual void decimal_ToBigInt()
        {
            decimal decimalValue = default(decimal);


            TestSubject.ToBigInt(decimalValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBigInt(double)")]
        
        public virtual void double_ToBigInt()
        {
            double doubleValue = default(double);

            TestSubject.ToBigInt(doubleValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBigInt(short)")]        
        public virtual void short_ToBigInt()
        {
            short shortValue = default(short);

            TestSubject.ToBigInt(shortValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBigInt(int)")]        
        public virtual void int_ToBigInt()
        {
            int intValue = default(int);

            TestSubject.ToBigInt(intValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBigInt(long)")]        
        public virtual void long_ToBigInt()
        {
            long longValue = default(long);

            TestSubject.ToBigInt(longValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBigInt(object)")]        
        public virtual void object_ToBigInt()
        {
            object objectValue = null;

            TestSubject.ToBigInt(objectValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBigInt(sbyte)")]        
        public virtual void sbyte_ToBigInt()
        {
            sbyte sbyteValue = default(sbyte);

            TestSubject.ToBigInt(sbyteValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBigInt(float)")]        
        public virtual void float_ToBigInt()
        {
            float floatValue = default(float);

            TestSubject.ToBigInt(floatValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBigInt(string)")]        
        public virtual void string_ToBigInt()
        {
            string stringValue = "123456789123456789";


            TestSubject.ToBigInt(stringValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBigInt(ushort)")]        
        public virtual void ushort_ToBigInt()
        {
            ushort ushortValue = default(ushort);

            TestSubject.ToBigInt(ushortValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBigInt")]
        
        public virtual void uint_ToBigInt()
        {

            


            // Create Test Method Parameters

            // There is no default constuctor for the parameter unitValue type UInt32.
            uint unitValue = default(uint);


            TestSubject.ToBigInt(unitValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBigInt")]
        
        public virtual void ulong_ToBigInt()
        {

            


            // Create Test Method Parameters

            // There is no default constuctor for the parameter ulongValue type UInt64.
            ulong ulongValue = default(ulong);


            TestSubject.ToBigInt(ulongValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBinary")]
        
        public virtual void bool_ToBinary()
        {

            


            // Create Test Method Parameters

            // There is no default constuctor for the parameter boolValue type Boolean.
            bool boolValue = default(bool);


            TestSubject.ToBinary(boolValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBinary")]
        
        public virtual void byte_ToBinary()
        {

            


            // Create Test Method Parameters

            // There is no default constuctor for the parameter byteValue type Byte.
            byte byteValue = default(byte);


            TestSubject.ToBinary(byteValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBinary")]
        
        public virtual void INullable_ToBinary()
        {

            


            // Create Test Method Parameters
            INullable nullable = null;

            TestSubject.ToBinary(nullable);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBinary")]
        
        public virtual void decimal_ToBinary()
        {

            


            // Create Test Method Parameters

            // There is no default constuctor for the parameter decimalValue type Decimal.
            decimal decimalValue = default(decimal);


            TestSubject.ToBinary(decimalValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBinary")]
        
        public virtual void double_ToBinary()
        {

            


            // Create Test Method Parameters

            // There is no default constuctor for the parameter doubleValue type Double.
            double doubleValue = default(double);


            TestSubject.ToBinary(doubleValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBinary")]
        
        public virtual void short_ToBinary()
        {

            


            // Create Test Method Parameters

            // There is no default constuctor for the parameter shortValue type Int16.
            short shortValue = default(short);


            TestSubject.ToBinary(shortValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBinary")]
        
        public virtual void int_ToBinary()
        {

            


            // Create Test Method Parameters

            // There is no default constuctor for the parameter intValue type Int32.
            int intValue = default(int);


            TestSubject.ToBinary(intValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBinary")]
        
        public virtual void long_ToBinary()
        {

            


            // Create Test Method Parameters

            // There is no default constuctor for the parameter longValue type Int64.
            long longValue = default(long);


            TestSubject.ToBinary(longValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBinary")]        
        public virtual void object_ToBinary()
        {
            object[][] values = new object[][]
            {
                new object[] {default(byte), new byte[1] , null},
                new object[] {default(short), new byte[2], null},
                new object[] {default(char), new byte[2], null},
                new object[] {default(int), new byte[4], null},
                new object[] {default(long), new byte[8], null},
            };

            foreach(object[] item in values)
            {
                object input = item[0];
                byte[] expected = (byte[]) item[1];
                object error = item[2];

                org.hsqldb.types.Binary actual = TestSubject.ToBinary(input);

                Assert.AreEqual(expected.Length, actual.getBytesLength());

                byte[] actualBytes = actual.getBytes();

                for (int i = 0; i < expected.Length; i++)
                {
                    Assert.AreEqual(expected[i], actualBytes[i]);
                }

            }
        }

        [Test, OfMember("ToBinary")]
        public virtual void sbyte_ToBinary()
        {
            sbyte sbyteValue = default(sbyte);


            TestSubject.ToBinary(sbyteValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBinary")]        
        public virtual void float_ToBinary()
        {
            float floatValue = default(float);


            TestSubject.ToBinary(floatValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBinary")]
        
        public virtual void string_ToBinary()
        {
            string stringValue = string.Empty;

            TestSubject.ToBinary(stringValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBinary")]
        
        public virtual void ushort_ToBinary()
        {
            ushort ushortValue = default(ushort);

            TestSubject.ToBinary(ushortValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBinary")]        
        public virtual void unit_ToBinary()
        {
            uint uintValue = default(uint);

            TestSubject.ToBinary(uintValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBinary")]        
        public virtual void ulong_ToBinary()
        {
            ulong ulongValue = default(ulong);

            TestSubject.ToBinary(ulongValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBinary")]        
        public virtual void bytes_ToBinary()
        {
            byte[] bytes = null;

            org.hsqldb.types.Binary actual = TestSubject.ToBinary(bytes);

        }

        [Test, OfMember("ToBoolean(bool)")]        
        public virtual void bool_ToBoolean()
        {
            bool boolValue = default(bool);
            TestSubject.ToBoolean(boolValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBoolean(byte)")]        
        public virtual void byte_ToBoolean()
        {
            TestSubject.ToBoolean((byte)0);
        }

        [Test, OfMember("ToBoolean(INullable)")]        
        public virtual void nullable_ToBoolean()
        {
            TestSubject.ToBoolean((INullable)null);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBoolean(decimal)")]        
        public virtual void decimal_ToBoolean()
        {
            decimal decimalValue = default(decimal);

            TestSubject.ToBoolean(decimalValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBoolean(double)")]        
        public virtual void double_ToBoolean()
        {
            double doubleValue = default(double);

            TestSubject.ToBoolean(doubleValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBoolean(short)")]        
        public virtual void short_ToBoolean()
        {
            short shortValue = default(short);

            TestSubject.ToBoolean(shortValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBoolean(int)")]        
        public virtual void int_ToBoolean()
        {
            int intValue = default(int);

            TestSubject.ToBoolean(intValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBoolean(long)")]        
        public virtual void long_ToBoolean()
        {
            long longValue = default(long);

            TestSubject.ToBoolean(longValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBoolean(object)")]        
        public virtual void object_ToBoolean()
        {
            object objectValue = null;

            TestSubject.ToBoolean(objectValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBoolean(sbyte)")]        
        public virtual void sbyte_ToBoolean()
        {
            sbyte sbyteValue = default(sbyte);

            TestSubject.ToBoolean(sbyteValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBoolean(float)")]        
        public virtual void float_ToBoolean()
        {
            float floatValue = default(float);

            TestSubject.ToBoolean(floatValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBoolean(string)")]        
        public virtual void string_ToBoolean()
        {
            string stringValue = string.Empty;

            TestSubject.ToBoolean(stringValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBoolean(ushort)")]        
        public virtual void ushort_ToBoolean()
        {
            ushort ushortValue = default(ushort);

            TestSubject.ToBoolean(ushortValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBoolean(uint)")]        
        public virtual void uint_ToBoolean()
        {
            uint uintValue = default(uint);

            TestSubject.ToBoolean(uintValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToBoolean(ulong)")]        
        public virtual void ulong_ToBoolean()
        {
            ulong ulongValue = default(ulong);

            TestSubject.ToBoolean(ulongValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDate(INullable)")]

        public virtual void INullable_ToDate()
        {
            INullable nullable = null;

            TestSubject.ToDate(nullable);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDate(DateTime)")]
        public virtual void DateTime_ToDate()
        {
            DateTime dateTimeValue = default(DateTime);

            TestSubject.ToDate(dateTimeValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDate(object)")]        
        public virtual void object_ToDate()
        {
            object objectValue = null;

            TestSubject.ToDate(objectValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDate(string)")]        
        public virtual void string_ToDate()
        {
            string stringValue = "2014-02-14";

            TestSubject.ToDate(stringValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDate(TimeSpan)")]

        public virtual void TimeSpan_ToDate()
        {

            TimeSpan timeSpanValue = default(TimeSpan);

            TestSubject.ToDate(timeSpanValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDateInMillis")]
        public virtual void DateTime_ToDateInMillis()
        {
            DateTime dateTimeValue = default(DateTime);

            TestSubject.ToDateInMillis(dateTimeValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDateInMillis(TimeSpan)")]
        public virtual void TimeSpan_ToDateInMillis()
        {
            TimeSpan timeSpanValue = default(TimeSpan);

            TestSubject.ToDateInMillis(timeSpanValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDateString")]        
        public virtual void ToDateString()
        {
            int year = 2010;
            int month = 1;
            int day = 1;
            bool checkRanges = true;

            TestSubject.ToDateString(year, month, day, checkRanges);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDateTimeString")]
        
        public virtual void ToDateTimeString()
        {
            int year = 2010;
            int month = 1;
            int day = 1;
            int hour = 12;
            int minute = 30;
            int second = 0;
            int nanosecond = 0;
            bool checkRanges = true;

            TestSubject.ToDateTimeString(year, month, day, hour, minute, second, nanosecond, checkRanges);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDecimal(bool)")]
        
        public virtual void bool_ToDecimal()
        {
            bool boolValue = default(bool);

            TestSubject.ToDecimal(boolValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDecimal(byte)")]        
        public virtual void byte_ToDecimal()
        {
            byte byteValue = default(byte);

            TestSubject.ToDecimal(byteValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDecimal(INullable)")]

        public virtual void INullable_ToDecimal()
        {
            INullable nullable = null;

            TestSubject.ToDecimal(nullable);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDecimal(decimal)")]        
        public virtual void decimal_ToDecimal()
        {
            decimal decimalValue = default(decimal);

            TestSubject.ToDecimal(decimalValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDecimal(double)")]        
        public virtual void double_ToDecimal()
        {
            double doubleValue = default(double);

            TestSubject.ToDecimal(doubleValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDecimal(short)")]        
        public virtual void short_ToDecimal()
        {
            short shortValue = default(short);

            TestSubject.ToDecimal(shortValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDecimal(int)")]        
        public virtual void int_ToDecimal()
        {
            int intValue = default(int);

            TestSubject.ToDecimal(intValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDecimal(long)")]        
        public virtual void long_ToDecimal()
        {
            long longValue = default(long);

            TestSubject.ToDecimal(longValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDecimal(object)")]        
        public virtual void object_ToDecimal()
        {
            object objectValue = null;

            TestSubject.ToDecimal(objectValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDecimal(sbyte)")]        
        public virtual void sbyte_ToDecimal()
        {
            sbyte sbyteValue = default(sbyte);

            TestSubject.ToDecimal(sbyteValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDecimal(float)")]        
        public virtual void float_ToDecimal()
        {
            float floatValue = default(float);

            TestSubject.ToDecimal(floatValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDecimal(string)")]
        
        public virtual void string_ToDecimal()
        {
            string stringValue = "123123123123.123123123123";

            TestSubject.ToDecimal(stringValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDecimal(ushort)")]        
        public virtual void ushort_ToDecimal()
        {
            ushort ushortValue = default(ushort);

            TestSubject.ToDecimal(ushortValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDecimal(uint)")]        
        public virtual void uint_ToDecimal()
        {
            uint uintValue = default(uint);

            TestSubject.ToDecimal(uintValue);

            // 
            // Write your assertions here.
            // 
        }

        [Test, OfMember("ToDecimal(ulong)")]
        
        public virtual void ulong_ToDecimal()
        {
            ulong ulongValue = default(ulong);


            TestSubject.ToDecimal(ulongValue);

            // 
            // Write your assertions here.
            // 
        }

        //[Test, OfMember("ToDouble")]
        
        //public virtual void ToDouble()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter boolValue type Boolean.
        //    bool boolValue;


        //    TestSubject.ToDouble(boolValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToDouble")]
        
        //public virtual void ToDouble()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter byteValue type Byte.
        //    byte byteValue;


        //    TestSubject.ToDouble(byteValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToDouble")]
        
        //public virtual void ToDouble()
        //{

            


        //    // Create Test Method Parameters
        //    RecorderINullable nullableRecording = new RecorderINullable();

        //    TestSubject.ToDouble(nullableRecording);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToDouble")]
        
        //public virtual void ToDouble()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter decimalValue type Decimal.
        //    decimal decimalValue;


        //    TestSubject.ToDouble(decimalValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToDouble")]
        
        //public virtual void ToDouble()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter shortValue type Int16.
        //    short shortValue;


        //    TestSubject.ToDouble(shortValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToDouble")]
        
        //public virtual void ToDouble()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter intValue type Int32.
        //    int intValue;


        //    TestSubject.ToDouble(intValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToDouble")]
        
        //public virtual void ToDouble()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter longValue type Int64.
        //    long longValue;


        //    TestSubject.ToDouble(longValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToDouble")]
        
        //public virtual void ToDouble()
        //{

            


        //    // Create Test Method Parameters
        //    object objectValue = new object();

        //    TestSubject.ToDouble(objectValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToDouble")]
        
        //public virtual void ToDouble()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter sbyteValue type SByte.
        //    sbyte sbyteValue;


        //    TestSubject.ToDouble(sbyteValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToDouble")]
        
        //public virtual void ToDouble()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter stringValue type String.
        //    string stringValue;


        //    TestSubject.ToDouble(stringValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToDouble")]
        
        //public virtual void ToDouble()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter ushortValue type UInt16.
        //    ushort ushortValue;


        //    TestSubject.ToDouble(ushortValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToDouble")]
        
        //public virtual void ToDouble()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter uintValue type UInt32.
        //    uint uintValue;


        //    TestSubject.ToDouble(uintValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToDouble")]
        
        //public virtual void ToDouble()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter ulongValue type UInt64.
        //    ulong ulongValue;


        //    TestSubject.ToDouble(ulongValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToInteger")]
        
        //public virtual void ToInteger()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter boolValue type Boolean.
        //    bool boolValue;


        //    TestSubject.ToInteger(boolValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToInteger")]
        
        //public virtual void ToInteger()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter byteValue type Byte.
        //    byte byteValue;


        //    TestSubject.ToInteger(byteValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToInteger")]
        
        //public virtual void ToInteger()
        //{

            


        //    // Create Test Method Parameters
        //    RecorderINullable nullableRecording = new RecorderINullable();

        //    TestSubject.ToInteger(nullableRecording);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToInteger")]
        
        //public virtual void ToInteger()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter decimalValue type Decimal.
        //    decimal decimalValue;


        //    TestSubject.ToInteger(decimalValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToInteger")]
        
        //public virtual void ToInteger()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter doubleValue type Double.
        //    double doubleValue;


        //    TestSubject.ToInteger(doubleValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToInteger")]
        
        //public virtual void ToInteger()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter shortValue type Int16.
        //    short shortValue;


        //    TestSubject.ToInteger(shortValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToInteger")]
        
        //public virtual void ToInteger()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter intValue type Int32.
        //    int intValue;


        //    TestSubject.ToInteger(intValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToInteger")]
        
        //public virtual void ToInteger()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter longValue type Int64.
        //    long longValue;


        //    TestSubject.ToInteger(longValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToInteger")]
        
        //public virtual void ToInteger()
        //{

            


        //    // Create Test Method Parameters
        //    object objectValue = new object();

        //    TestSubject.ToInteger(objectValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToInteger")]
        
        //public virtual void ToInteger()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter sbyteValue type SByte.
        //    sbyte sbyteValue;


        //    TestSubject.ToInteger(sbyteValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToInteger")]
        
        //public virtual void ToInteger()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter floatValue type Single.
        //    float floatValue;


        //    TestSubject.ToInteger(floatValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToInteger")]
        
        //public virtual void ToInteger()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter stringValue type String.
        //    string stringValue;


        //    TestSubject.ToInteger(stringValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToInteger")]
        
        //public virtual void ToInteger()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter ushortValue type UInt16.
        //    ushort ushortValue;


        //    TestSubject.ToInteger(ushortValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToInteger")]
        
        //public virtual void ToInteger()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter uintValue type UInt32.
        //    uint uintValue;


        //    TestSubject.ToInteger(uintValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToInteger")]
        
        //public virtual void ToInteger()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter ulongValue type UInt64.
        //    ulong ulongValue;


        //    TestSubject.ToInteger(ulongValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToObject")]
        
        //public virtual void ToObject()
        //{

            


        //    // Create Test Method Parameters
        //    object objectValue = new object();

        //    // There is no default constuctor for the parameter sqlType type Int32.
        //    int sqlType;


        //    TestSubject.ToObject(objectValue, sqlType);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToOther")]
        
        //public virtual void ToOther()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter serValue type Serializable.
        //    java.io.Serializable serValue;


        //    TestSubject.ToOther(serValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToOther")]
        
        //public virtual void ToOther()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter boolValue type Boolean.
        //    bool boolValue;


        //    TestSubject.ToOther(boolValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToOther")]
        
        //public virtual void ToOther()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter byteValue type Byte.
        //    byte byteValue;


        //    TestSubject.ToOther(byteValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToOther")]
        
        //public virtual void ToOther()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter dateTimeValue type DateTime.
        //    System.DateTime dateTimeValue;


        //    TestSubject.ToOther(dateTimeValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToOther")]
        
        //public virtual void ToOther()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter decimalValue type Decimal.
        //    decimal decimalValue;


        //    TestSubject.ToOther(decimalValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToOther")]
        
        //public virtual void ToOther()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter doubleValue type Double.
        //    double doubleValue;


        //    TestSubject.ToOther(doubleValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToOther")]
        
        //public virtual void ToOther()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter shortValue type Int16.
        //    short shortValue;


        //    TestSubject.ToOther(shortValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToOther")]
        
        //public virtual void ToOther()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter intValue type Int32.
        //    int intValue;


        //    TestSubject.ToOther(intValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToOther")]
        
        //public virtual void ToOther()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter longValue type Int64.
        //    long longValue;


        //    TestSubject.ToOther(longValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToOther")]
        
        //public virtual void ToOther()
        //{

            


        //    // Create Test Method Parameters
        //    object value = new object();

        //    TestSubject.ToOther(value);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToOther")]
        
        //public virtual void ToOther()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter sbyteValue type SByte.
        //    sbyte sbyteValue;


        //    TestSubject.ToOther(sbyteValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToOther")]
        
        //public virtual void ToOther()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter floatValue type Single.
        //    float floatValue;


        //    TestSubject.ToOther(floatValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToOther")]
        
        //public virtual void ToOther()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter timeSpanValue type TimeSpan.
        //    System.TimeSpan timeSpanValue;


        //    TestSubject.ToOther(timeSpanValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToOther")]
        
        //public virtual void ToOther()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter ushortValue type UInt16.
        //    ushort ushortValue;


        //    TestSubject.ToOther(ushortValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToOther")]
        
        //public virtual void ToOther()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter uintValue type UInt32.
        //    uint uintValue;


        //    TestSubject.ToOther(uintValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToOther")]
        
        //public virtual void ToOther()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter ulongValue type UInt64.
        //    ulong ulongValue;


        //    TestSubject.ToOther(ulongValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToOther")]
        
        //public virtual void ToOther()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter stringValue type String.
        //    string stringValue;


        //    // There is no default constuctor for the parameter isSerialForm type Boolean.
        //    bool isSerialForm;


        //    TestSubject.ToOther(stringValue, isSerialForm);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToOther")]
        
        //public virtual void ToOther()
        //{

            


        //    // Create Test Method Parameters
        //}

        //[Test, OfMember("ToReal")]
        
        //public virtual void ToReal()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter boolValue type Boolean.
        //    bool boolValue;


        //    TestSubject.ToReal(boolValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToReal")]
        
        //public virtual void ToReal()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter byteValue type Byte.
        //    byte byteValue;


        //    TestSubject.ToReal(byteValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToReal")]
        
        //public virtual void ToReal()
        //{

            


        //    // Create Test Method Parameters
        //    RecorderINullable nullableRecording = new RecorderINullable();

        //    TestSubject.ToReal(nullableRecording);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToReal")]
        
        //public virtual void ToReal()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter decimalValue type Decimal.
        //    decimal decimalValue;


        //    TestSubject.ToReal(decimalValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToReal")]
        
        //public virtual void ToReal()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter doubleValue type Double.
        //    double doubleValue;


        //    TestSubject.ToReal(doubleValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToReal")]
        
        //public virtual void ToReal()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter shortValue type Int16.
        //    short shortValue;


        //    TestSubject.ToReal(shortValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToReal")]
        
        //public virtual void ToReal()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter intValue type Int32.
        //    int intValue;


        //    TestSubject.ToReal(intValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToReal")]
        
        //public virtual void ToReal()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter longValue type Int64.
        //    long longValue;


        //    TestSubject.ToReal(longValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToReal")]
        
        //public virtual void ToReal()
        //{

            


        //    // Create Test Method Parameters
        //    object objectValue = new object();

        //    TestSubject.ToReal(objectValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToReal")]
        
        //public virtual void ToReal()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter sbyteValue type SByte.
        //    sbyte sbyteValue;


        //    TestSubject.ToReal(sbyteValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToReal")]
        
        //public virtual void ToReal()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter floatValue type Single.
        //    float floatValue;


        //    TestSubject.ToReal(floatValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToReal")]
        
        //public virtual void ToReal()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter stringValue type String.
        //    string stringValue;


        //    TestSubject.ToReal(stringValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToReal")]
        
        //public virtual void ToReal()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter ushortValue type UInt16.
        //    ushort ushortValue;


        //    TestSubject.ToReal(ushortValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToReal")]
        
        //public virtual void ToReal()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter uintValue type UInt32.
        //    uint uintValue;


        //    TestSubject.ToReal(uintValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToReal")]
        
        //public virtual void ToReal()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter value type UInt64.
        //    ulong value;


        //    TestSubject.ToReal(value);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToSmallInt")]
        
        //public virtual void ToSmallInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter boolValue type Boolean.
        //    bool boolValue;


        //    TestSubject.ToSmallInt(boolValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToSmallInt")]
        
        //public virtual void ToSmallInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter byteValue type Byte.
        //    byte byteValue;


        //    TestSubject.ToSmallInt(byteValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToSmallInt")]
        
        //public virtual void ToSmallInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter decimalValue type Decimal.
        //    decimal decimalValue;


        //    TestSubject.ToSmallInt(decimalValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToSmallInt")]
        
        //public virtual void ToSmallInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter doubleValue type Double.
        //    double doubleValue;


        //    TestSubject.ToSmallInt(doubleValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToSmallInt")]
        
        //public virtual void ToSmallInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter shortValue type Int16.
        //    short shortValue;


        //    TestSubject.ToSmallInt(shortValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToSmallInt")]
        
        //public virtual void ToSmallInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter intValue type Int32.
        //    int intValue;


        //    TestSubject.ToSmallInt(intValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToSmallInt")]
        
        //public virtual void ToSmallInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter longValue type Int64.
        //    long longValue;


        //    TestSubject.ToSmallInt(longValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToSmallInt")]
        
        //public virtual void ToSmallInt()
        //{

            


        //    // Create Test Method Parameters
        //    object objectValue = new object();

        //    TestSubject.ToSmallInt(objectValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToSmallInt")]
        
        //public virtual void ToSmallInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter sbyteValue type SByte.
        //    sbyte sbyteValue;


        //    TestSubject.ToSmallInt(sbyteValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToSmallInt")]
        
        //public virtual void ToSmallInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter floatValue type Single.
        //    float floatValue;


        //    TestSubject.ToSmallInt(floatValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToSmallInt")]
        
        //public virtual void ToSmallInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter stringValue type String.
        //    string stringValue;


        //    TestSubject.ToSmallInt(stringValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToSmallInt")]
        
        //public virtual void ToSmallInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter ushortValue type UInt16.
        //    ushort ushortValue;


        //    TestSubject.ToSmallInt(ushortValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToSmallInt")]
        
        //public virtual void ToSmallInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter uintValue type UInt32.
        //    uint uintValue;


        //    TestSubject.ToSmallInt(uintValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToSmallInt")]
        
        //public virtual void ToSmallInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter ulongValue type UInt64.
        //    ulong ulongValue;


        //    TestSubject.ToSmallInt(ulongValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToString")]
        
        //public virtual void ToString()
        //{

            


        //    // Create Test Method Parameters
        //    RecorderINullable nullableRecording = new RecorderINullable();

        //    TestSubject.ToString(nullableRecording);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToString")]
        
        //public virtual void ToString()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter dateTimeValue type DateTime.
        //    System.DateTime dateTimeValue;


        //    TestSubject.ToString(dateTimeValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToString")]
        
        //public virtual void ToString()
        //{

            


        //    // Create Test Method Parameters
        //    object value = new object();

        //    TestSubject.ToString(value);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToString")]
        
        //public virtual void ToString()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter timeSpanValue type TimeSpan.
        //    System.TimeSpan timeSpanValue;


        //    TestSubject.ToString(timeSpanValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTime")]
        
        //public virtual void ToTime()
        //{

            


        //    // Create Test Method Parameters
        //    RecorderINullable nullableRecording = new RecorderINullable();

        //    TestSubject.ToTime(nullableRecording);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTime")]
        
        //public virtual void ToTime()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter value type DateTime.
        //    System.DateTime value;


        //    TestSubject.ToTime(value);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTime")]
        
        //public virtual void ToTime()
        //{

            


        //    // Create Test Method Parameters
        //    object value = new object();

        //    TestSubject.ToTime(value);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTime")]
        
        //public virtual void ToTime()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter stringValue type String.
        //    string stringValue;


        //    TestSubject.ToTime(stringValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTime")]
        
        //public virtual void ToTime()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter value type TimeSpan.
        //    System.TimeSpan value;


        //    TestSubject.ToTime(value);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTimeInMillis")]
        
        //public virtual void ToTimeInMillis()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter value type DateTime.
        //    System.DateTime value;


        //    TestSubject.ToTimeInMillis(value);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTimeInMillis")]
        
        //public virtual void ToTimeInMillis()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter value type TimeSpan.
        //    System.TimeSpan value;


        //    TestSubject.ToTimeInMillis(value);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTimestamp")]
        
        //public virtual void ToTimestamp()
        //{

            


        //    // Create Test Method Parameters
        //    RecorderINullable nullableRecording = new RecorderINullable();

        //    TestSubject.ToTimestamp(nullableRecording);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTimestamp")]
        
        //public virtual void ToTimestamp()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter dateTimeValue type DateTime.
        //    System.DateTime dateTimeValue;


        //    TestSubject.ToTimestamp(dateTimeValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTimestamp")]
        
        //public virtual void ToTimestamp()
        //{

            


        //    // Create Test Method Parameters
        //    object value = new object();

        //    TestSubject.ToTimestamp(value);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTimestamp")]
        
        //public virtual void ToTimestamp()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter stringValue type String.
        //    string stringValue;


        //    TestSubject.ToTimestamp(stringValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTimestamp")]
        
        //public virtual void ToTimestamp()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter timeSpanValue type TimeSpan.
        //    System.TimeSpan timeSpanValue;


        //    TestSubject.ToTimestamp(timeSpanValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTimestampInMillis")]
        
        //public virtual void ToTimestampInMillis()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter dateTimeValue type DateTime.
        //    System.DateTime dateTimeValue;


        //    TestSubject.ToTimestampInMillis(dateTimeValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTimestampInMillis")]
        
        //public virtual void ToTimestampInMillis()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter timeSpanValue type TimeSpan.
        //    System.TimeSpan timeSpanValue;


        //    TestSubject.ToTimestampInMillis(timeSpanValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTimeString")]
        
        //public virtual void ToTimeString()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter hour type Int32.
        //    int hour;


        //    // There is no default constuctor for the parameter minute type Int32.
        //    int minute;


        //    // There is no default constuctor for the parameter second type Int32.
        //    int second;


        //    // There is no default constuctor for the parameter checkRanges type Boolean.
        //    bool checkRanges;


        //    TestSubject.ToTimeString(hour, minute, second, checkRanges);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTinyInt")]
        
        //public virtual void ToTinyInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter boolValue type Boolean.
        //    bool boolValue;


        //    TestSubject.ToTinyInt(boolValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTinyInt")]
        
        //public virtual void ToTinyInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter byteValue type Byte.
        //    byte byteValue;


        //    TestSubject.ToTinyInt(byteValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTinyInt")]
        
        //public virtual void ToTinyInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter decimalValue type Decimal.
        //    decimal decimalValue;


        //    TestSubject.ToTinyInt(decimalValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTinyInt")]
        
        //public virtual void ToTinyInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter doubleValue type Double.
        //    double doubleValue;


        //    TestSubject.ToTinyInt(doubleValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTinyInt")]
        
        //public virtual void ToTinyInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter shortValue type Int16.
        //    short shortValue;


        //    TestSubject.ToTinyInt(shortValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTinyInt")]
        
        //public virtual void ToTinyInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter intValue type Int32.
        //    int intValue;


        //    TestSubject.ToTinyInt(intValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTinyInt")]
        
        //public virtual void ToTinyInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter longValue type Int64.
        //    long longValue;


        //    TestSubject.ToTinyInt(longValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTinyInt")]
        
        //public virtual void ToTinyInt()
        //{

            


        //    // Create Test Method Parameters
        //    object objectValue = new object();

        //    TestSubject.ToTinyInt(objectValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTinyInt")]
        
        //public virtual void ToTinyInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter value type SByte.
        //    sbyte value;


        //    TestSubject.ToTinyInt(value);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTinyInt")]
        
        //public virtual void ToTinyInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter floatValue type Single.
        //    float floatValue;


        //    TestSubject.ToTinyInt(floatValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTinyInt")]
        
        //public virtual void ToTinyInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter stringValue type String.
        //    string stringValue;


        //    TestSubject.ToTinyInt(stringValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTinyInt")]
        
        //public virtual void ToTinyInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter ushortValue type UInt16.
        //    ushort ushortValue;


        //    TestSubject.ToTinyInt(ushortValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTinyInt")]
        
        //public virtual void ToTinyInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter uintValue type UInt32.
        //    uint uintValue;


        //    TestSubject.ToTinyInt(uintValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}

        //[Test, OfMember("ToTinyInt")]
        
        //public virtual void ToTinyInt()
        //{

            


        //    // Create Test Method Parameters

        //    // There is no default constuctor for the parameter ulongValue type UInt64.
        //    ulong ulongValue;


        //    TestSubject.ToTinyInt(ulongValue);

        //    // 
        //    // Write your assertions here.
        //    // 
        //}
    }
}
