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
            bool ignoreCase;
            char? escapeCharacter;
            Like testSubject = new Like(ignoreCase = false, escapeCharacter = '\\');

            testSubject.SetPattern("foo%");

            Assert.That(null == testSubject.Matches(null));
            Assert.That(testSubject.Matches("foo").Value);
            Assert.That(testSubject.Matches("foobar").Value);
            Assert.That(!testSubject.Matches("foa").Value);

            testSubject.SetPattern("foo_");

            Assert.That(null == testSubject.Matches(null));
            Assert.That(testSubject.Matches("fooz").Value);
            Assert.That(!testSubject.Matches("foobar").Value);
            Assert.That(!testSubject.Matches("foo").Value);

            testSubject.SetPattern("foo\\_\\%");

            Assert.That(null == testSubject.Matches(null));
            Assert.That(testSubject.Matches("foo_%").Value);
            Assert.That(!testSubject.Matches("foobar").Value);
            Assert.That(!testSubject.Matches("foo").Value);

            testSubject = new Like(ignoreCase = true, escapeCharacter);

            testSubject.SetPattern("FOO%");

            Assert.That(null == testSubject.Matches(null));
            Assert.That(testSubject.Matches("foo").Value);
            Assert.That(testSubject.Matches("foobar").Value);
            Assert.That(!testSubject.Matches("foa").Value);

            Assert.That(testSubject.Matches("fOo").Value);
            Assert.That(testSubject.Matches("FOoBaR").Value);
            Assert.That(!testSubject.Matches("foa").Value);
        }
    }
}
