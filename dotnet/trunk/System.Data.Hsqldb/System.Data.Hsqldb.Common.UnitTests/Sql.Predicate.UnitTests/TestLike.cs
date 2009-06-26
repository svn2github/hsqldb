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
            Like testSubject = new Like(/*ignorecase*/false,/*escapechar*/'\\');

            testSubject.SetPattern("foo%");

            Assert.That(null == testSubject.Matches(null));
            Assert.That(testSubject.Matches("foo") == true);
            Assert.That(testSubject.Matches("foobar") == true);
            Assert.That(testSubject.Matches("foa") == false);

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

            testSubject = new Like(/*ignorecase*/true,/*escapechar*/'\\');

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
