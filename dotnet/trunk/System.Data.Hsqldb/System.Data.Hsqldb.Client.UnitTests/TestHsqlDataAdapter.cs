using System;
using System.Data.Hsqldb.TestCoverage;
using NUnit.Framework;


namespace System.Data.Hsqldb.Client.UnitTests
{
    [TestFixture, ForSubject(typeof(HsqlDataAdapter))]
    public class TestHsqlDataAdapter
    {        
        [Test, OfMember("Clone")]
        public void Clone()
        {
            HsqlDataAdapter testSubject = new HsqlDataAdapter();

            HsqlDataAdapter actual = testSubject.Clone();

            Assert.Fail("TODO");
        }
    }
}
