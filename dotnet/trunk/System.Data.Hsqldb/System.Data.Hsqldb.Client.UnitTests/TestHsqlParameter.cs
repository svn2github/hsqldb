#region Using
using System;
using System.Data.Hsqldb.TestCoverage;
using NUnit.Framework; 
#endregion

namespace System.Data.Hsqldb.Client.UnitTests
{
    [TestFixture, ForSubject(typeof(HsqlParameter))]
    public class TestHsqlParameter
    {

        static HsqlParameter NewTestSubject()
        {
            return new HsqlParameter();
        }

        [Test, OfMember("Clone")]
        public void Clone()
        {
            HsqlParameter testSubject = NewTestSubject();

            HsqlParameter actual = testSubject.Clone();

            Assert.Fail("TODO");
        }
        
        [Test, OfMember("ResetDbType")]
        public void ResetDbType()
        {
            HsqlParameter testSubject = NewTestSubject();

            testSubject.ResetDbType();

            Assert.Fail("TODO");
        }
        
        [Test, OfMember("ToSqlLiteral")]
        public void ToSqlLiteral()
        {
            HsqlParameter testSubject = new HsqlParameter();

            string actual = testSubject.ToSqlLiteral();

            Assert.Fail("TODO");
        }
        
        [Test, OfMember("ToString")]
        public void ToString()
        {
            HsqlParameter testSubject = new HsqlParameter();

            string actual = testSubject.ToString();

            Assert.Fail("TODO");
        }
    }
}
