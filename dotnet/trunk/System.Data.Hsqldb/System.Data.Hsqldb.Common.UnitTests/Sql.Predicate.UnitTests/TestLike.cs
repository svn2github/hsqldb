using System;
using NUnit.Framework;
using System.Data.Hsqldb.TestCoverage;

namespace System.Data.Hsqldb.Common.Sql.Predicate.UnitTests
{
    [TestFixture, ForSubject(typeof(Like))]
    public class TestLike
    {

        [Test, OfMember("Matches")]
        public void Matches()
        {
            bool ignoreCase = false;
            char? escapeCharacter = '\\';
            Like testSubject = new Like(ignoreCase, escapeCharacter);

            testSubject.SetPattern("foo%");

            bool expected = true;
            bool? actual = testSubject.Matches("foobar");

            Assert.AreEqual(expected, actual);
        }
    }
}
