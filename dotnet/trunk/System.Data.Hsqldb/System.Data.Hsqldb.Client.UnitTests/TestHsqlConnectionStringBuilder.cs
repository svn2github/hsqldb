using System;
using System.Data.Hsqldb.TestCoverage;
using NUnit.Framework;
using System.Data.Hsqldb.Client;

namespace UnitTests
{
    [TestFixture, ForSubject(typeof(HsqlConnectionStringBuilder))]
    public class TestHsqlConnectionStringBuilder
    {
        
        [Test, OfMember("Clear")]
        public virtual void Clear()
        {
            HsqlConnectionStringBuilder testSubject = new HsqlConnectionStringBuilder();

            testSubject.Clear();

            Assert.Fail("TODO");
        }
        
        [Test, OfMember("ContainsKey")]
        public virtual void ContainsKey()
        {
            // Create Constructor Parameters

            HsqlConnectionStringBuilder testSubject = new HsqlConnectionStringBuilder();
 
            testSubject.ContainsKey("foo");

            Assert.Fail("TODO");
        }
        
        [Test, OfMember("EquivalentTo")]
        public virtual void EquivalentTo()
        {
            HsqlConnectionStringBuilder testSubject = new HsqlConnectionStringBuilder();

            bool expected = true;
            bool actual = testSubject.EquivalentTo(testSubject);

            Assert.AreEqual(expected, actual);

            Assert.Fail("TODO"); 
        }
        
        [Test, OfMember("Remove")]
        public virtual void Remove()
        {
            HsqlConnectionStringBuilder testSubject = new HsqlConnectionStringBuilder();
            bool actual = testSubject.Remove("foo");

            Assert.Fail("TODO"); 
        }
        
        [Test, OfMember("ShouldSerialize")]
        public virtual void ShouldSerialize()
        {
            HsqlConnectionStringBuilder testSubject = new HsqlConnectionStringBuilder();
            bool actual = testSubject.ShouldSerialize("foo");

            Assert.Fail("TODO");
        }
        
        [Test, OfMember("TryGetValue")]
        public virtual void TryGetValue()
        {
            HsqlConnectionStringBuilder testSubject = new HsqlConnectionStringBuilder();

            object value;
            bool success = false;

            try
            {
                success = testSubject.TryGetValue("foo", out value);
            }
            catch (Exception ex)
            {

            }

            Assert.Fail("TODO");

        }
    }
}
