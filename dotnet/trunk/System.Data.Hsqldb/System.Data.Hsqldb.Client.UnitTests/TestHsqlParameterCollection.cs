#region Using
using System;
using System.Data.Hsqldb.TestCoverage;
using NUnit.Framework; 
#endregion

namespace System.Data.Hsqldb.Client.UnitTests
{    

    [TestFixture, ForSubject(typeof(HsqlParameterCollection))]
    public class TestHsqlParameterCollection
    {
        static HsqlParameterCollection NewTestSubject()
        {
            return (new HsqlCommand()).Parameters;
        }

        [Test, OfMember("Add")]
        public void Add()
        {
            HsqlParameterCollection testSubject = NewTestSubject();
            HsqlParameter value = new HsqlParameter();

            HsqlParameter rvalue = testSubject.Add(value);

            object ovalue = new HsqlParameter();

            int parameterIndex = testSubject.Add(ovalue);

            Assert.Fail("TODO");
        }
        
        [Test, OfMember("AddRange")]
        public void AddRange()
        {
            // Create Constructor Parameters

            HsqlParameterCollection testSubject = NewTestSubject();

            // Create Test Method Parameters
            Array avalues = new object[] { new HsqlParameter() };

            testSubject.AddRange(avalues);

            HsqlParameter[] pvalues = new HsqlParameter[] { new HsqlParameter() };

            testSubject.AddRange(pvalues);

            Assert.Fail("TODO");
        }

        
        [Test, OfMember("Clear")]
        public void Clear()
        {
            HsqlParameterCollection testSubject = NewTestSubject();

            testSubject.Clear();

            Assert.Fail("TODO");
        }
        
        [Test, OfMember("Clone")]
        public void Clone()
        {
            HsqlParameterCollection testSubject = NewTestSubject();

            testSubject.Clone();

            Assert.Fail("TODO");
        }
        
        [Test, OfMember("Contains")]
        public void Contains()
        {

            HsqlParameterCollection testSubject = NewTestSubject();
            object value = new HsqlParameter("foo", "bar");

            testSubject.Add(value);

            bool containsValue = testSubject.Contains(value);

            string parameterName = "foo";

            bool containsNamedParameter = testSubject.Contains(parameterName);

            Assert.Fail("TODO");
        }
        
        [Test, OfMember("CopyTo")]
        public void CopyTo()
        {
            HsqlParameterCollection testSubject = NewTestSubject();

            object[] array = new object[testSubject.Count];

            testSubject.CopyTo(array, 0);

            Assert.Fail("TODO"); 
        }
        
        [Test, OfMember("GetEnumerator")]
        public void GetEnumerator()
        {
            HsqlParameterCollection testSubject = NewTestSubject();

            foreach (HsqlParameter parameter in testSubject)
            {

            }

            Assert.Fail("TODO");
        }
        
        [Test, OfMember("IndexOf")]
        public void IndexOf()
        {
            HsqlParameterCollection testSubject = NewTestSubject();
            HsqlParameter value = new HsqlParameter("foo", "bar");

            int expectedValueIndex = testSubject.Add((object)value);
            int actualValueIndex = testSubject.IndexOf(value);

            Assert.AreEqual(expectedValueIndex, actualValueIndex);
            Assert.AreEqual(testSubject.IndexOf(value.ParameterName), testSubject.IndexOf(value));

            Assert.Fail("TODO");
        }
        
        [Test, OfMember("Insert")]
        public void Insert()
        {
            HsqlParameterCollection testSubject = NewTestSubject();

            testSubject.Insert(0, new HsqlParameter("foo", "bar"));

            Assert.Fail("TODO"); 
        }
        
        [Test, OfMember("Remove")]
        public void Remove() {
            HsqlParameterCollection testSubject = NewTestSubject();

            HsqlParameter parameter = new HsqlParameter("foo", "bar");

            testSubject.Add(parameter);

            Assert.AreEqual(1, testSubject.Count);
 
            testSubject.Remove(parameter);

            Assert.AreEqual(0, testSubject.Count);

            Assert.Fail("TODO");
        }

        [Test, OfMember("RemoveAt")]
        public void RemoveAt()
        {
            HsqlParameterCollection testSubject = NewTestSubject();

            testSubject.Add(new HsqlParameter("foo", "bar"));
            testSubject.Add(new HsqlParameter("foo2", "bar"));

            Assert.That(testSubject.Contains("foo"));
            Assert.That(testSubject.Contains("foo2"));

            testSubject.RemoveAt(0);            
            testSubject.RemoveAt("foo2");

            Assert.That(!testSubject.Contains("foo"));
            Assert.That(!testSubject.Contains("foo2"));

            Assert.Fail("TODO");
        }
    }
}
