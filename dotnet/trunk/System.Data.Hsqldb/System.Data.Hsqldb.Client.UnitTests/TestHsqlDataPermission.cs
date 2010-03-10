using System;
using System.Data.Hsqldb.TestCoverage;
using NUnit.Framework;
using TestCategory = NUnit.Framework.CategoryAttribute;

namespace System.Data.Hsqldb.Client.UnitTests
{
    [TestFixture, TestCategory("DBDataPermission"), ForSubject(typeof(HsqlDataPermission))]
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
