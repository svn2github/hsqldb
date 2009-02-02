using System;
using NUnit.Framework;
using System.Data.Hsqldb.TestCoverage;

namespace System.Data.Hsqldb.Common.Sql.Type.UnitTests
{
    [TestFixture, ForSubject(typeof(SqlObject))]
    public class TestSqlObject
    {
        
        [Test, OfMember("AddSerializationHeader")]
        public void AddSerializationHeader()
        {
            Assert.Fail("TODO");
        }

        [Test, OfMember("Deserialize")]
        public void Deserialize()
        {
            Assert.Fail("TODO");
        }

        [Test, OfMember("getObject")]
        public virtual void getObject()
        {
            Assert.Fail("TODO");
        }

        [Test, OfMember("Serialize")]
        public void Serialize()
        {
            Assert.Fail("TODO");
        }

        [Test, OfMember("StartsWithSerializationHeader")]
        public void StartsWithSerializationHeader()
        {
            Assert.Fail("TODO");
        }

        [Test, OfMember("UnWrap")]
        public void UnWrap()
        {
            Assert.Fail("TODO");
        }
    }
}
