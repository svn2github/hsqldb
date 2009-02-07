using System;
using NUnit.Framework;
using System.Data.Hsqldb.TestCoverage;
using System.Runtime.Serialization.Formatters.Binary;
using System.IO;

namespace System.Data.Hsqldb.Common.Sql.Type.UnitTests
{
    [TestFixture, ForSubject(typeof(SqlObject))]
    public class TestSqlObject
    {
        
        [Test, OfMember("AddSerializationHeader")]
        public void AddSerializationHeader()
        {
            byte[] actual = SqlObject.AddSerializationHeader(new byte[] { 1, 2, 3, 4 });

            Guid header = SqlObject.SerializationHeader;
            byte[] headerBytes = header.ToByteArray();

            Assert.AreEqual(headerBytes.Length + 4, actual.Length);

            for (int i = 0; i < headerBytes.Length; i++)
            {
                Assert.AreEqual(headerBytes[i], actual[i]);
            }

            for (int i = headerBytes.Length, j = 1; i < headerBytes.Length + 4; i++, j++)
            {
                Assert.AreEqual(j, actual[i]);
            }
        }

        [Test, OfMember("Deserialize")]
        public void Deserialize()
        {
            MemoryStream stream = new MemoryStream();
            (new BinaryFormatter()).Serialize(stream, 12345.6789D);

            byte[] bytes = SqlObject.AddSerializationHeader(stream.ToArray());
            bool isJavaObject = true;
            
            object obj = SqlObject.Deserialize(bytes, out isJavaObject);

            Assert.IsFalse(isJavaObject);
            Assert.AreEqual(12345.6789D, obj);
            Assert.IsInstanceOfType(typeof(double), obj);

        }

        [Test, OfMember("getObject")]
        public virtual void getObject()
        {
            SqlObject sqlObject = new SqlObject(decimal.MaxValue);

            object obj = sqlObject.getObject();

            Assert.AreEqual(decimal.MaxValue, obj);
        }

        [Test, OfMember("Serialize")]
        public void Serialize()
        {
            object obj = new java.lang.Long(long.MaxValue);
            byte[] bytes = SqlObject.Serialize(obj);

            Assert.That(!SqlObject.StartsWithSerializationHeader(bytes));

            SqlObject wrapper = new SqlObject(bytes, true);

            object wrappedObject = wrapper.getObject();

            Assert.AreEqual(new java.lang.Long(long.MaxValue), wrappedObject);

            bytes = SqlObject.Serialize(long.MaxValue);

            Assert.That(SqlObject.StartsWithSerializationHeader(bytes));

            bool isJavaObject;

            obj = SqlObject.Deserialize(bytes, out isJavaObject);

            Assert.IsFalse(isJavaObject);
            Assert.AreEqual(long.MaxValue, obj);

            wrapper = new SqlObject(bytes);

            obj = wrapper.getObject();

            Assert.AreEqual(long.MaxValue, obj);
        }

        [Test, OfMember("StartsWithSerializationHeader")]
        public void StartsWithSerializationHeader()
        {
            Assert.IsTrue(SqlObject.StartsWithSerializationHeader(
                SqlObject.SerializationHeader.ToByteArray()));
        }

        [Test, OfMember("UnWrap")]
        public void UnWrap()
        {
            SqlObject sqlObject = new SqlObject(1234.5678D);

            bool isJavaObject = true;
            object unwrapped = sqlObject.UnWrap(out isJavaObject);

            Assert.AreEqual(false, isJavaObject);
            Assert.AreEqual(1234.5678D, unwrapped);

            sqlObject = new SqlObject(new java.lang.Double(1234.5678D));

            isJavaObject = false;
            unwrapped = sqlObject.UnWrap(out isJavaObject);

            Assert.AreEqual(true, isJavaObject);
            Assert.AreEqual(new java.lang.Double(1234.5678D), unwrapped);
        }
    }
}
