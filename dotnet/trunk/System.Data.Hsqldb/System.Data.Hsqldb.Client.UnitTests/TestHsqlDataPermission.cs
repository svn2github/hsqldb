using System;
using System.Data.Hsqldb.TestCoverage;
using NUnit.Framework;

namespace System.Data.Hsqldb.Client.UnitTests
{
    [TestFixture, ForSubject(typeof(HsqlDataPermission))]
    public class TestHsqlDataPermission
    {        
        [Test, OfMember("Add")]
        public void Add()
        {
            Assert.Fail("TODO");
        }
        
        [Test, OfMember("Copy")]
        public void Copy()
        {
            Assert.Fail("TODO");
        }
    }
}
