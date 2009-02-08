using System;
using NUnit.Framework;
using System.Data.Hsqldb.TestCoverage;
using System.Data.Hsqldb.Common.Enumeration;

namespace System.Data.Hsqldb.Common.Sql.UnitTests
{
    [TestFixture, ForSubject(typeof(Token))]
    public class TestToken
    {
        
        [Test, OfMember("Equals")]
        public void Equals()
        {
            Token testSubject = new Token(Token.ValueFor.ACTION, TokenType.Null);

            Assert.AreEqual(Token.ValueFor.ACTION, testSubject.Value);
        }

        
        [Test, OfMember("GetHashCode")]
        new public void GetHashCode()
        {
            Assert.Fail("TODO");
        }
    }
}
