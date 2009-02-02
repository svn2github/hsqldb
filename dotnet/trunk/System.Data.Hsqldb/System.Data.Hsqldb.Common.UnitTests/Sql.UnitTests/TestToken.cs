using System;
using NUnit.Framework;
using System.Data.Hsqldb.TestCoverage;

namespace System.Data.Hsqldb.Common.Sql.UnitTests
{
    [TestFixture, ForSubject(typeof(Token))]
    public class TestToken
    {
        
        [Test, OfMember("Equals")]
        public void Equals()
        {
            Assert.Fail("TODO");
        }

        
        [Test, OfMember("GetHashCode")]
        new public void GetHashCode()
        {
            Assert.Fail("TODO");
        }
    }
}
