using System;
using NUnit.Framework;
using System.Data.Hsqldb.Common.IO;
using System.Data.Hsqldb.TestCoverage;
using System.IO;

namespace UnitTests
{
    [TestFixture, ForSubject(typeof(JavaOutputStreamAdapter))]
    public class TestJavaOutputStreamAdapter
    {
        static JavaOutputStreamAdapter NewTestSubject()
        {
            return NewTestSubject(new MemoryStream());
        }

        static JavaOutputStreamAdapter NewTestSubject(Stream adaptee)
        {
            return new JavaOutputStreamAdapter(adaptee);
        }

        [Test, OfMember("close")]
        public void close()
        {
            using (JavaOutputStreamAdapter testSubject = NewTestSubject())
            {
                testSubject.close();
            }
        }

        [Test, OfMember("Dispose"), ExpectedException(typeof(ObjectDisposedException))]
        public void Dispose()
        {
            JavaOutputStreamAdapter testSubject;

            using (testSubject = NewTestSubject())
            {

            }

            testSubject.write(1);
        }

        [Test, OfMember("flush")]
        public void flush()
        {
            using (JavaOutputStreamAdapter testSubject = NewTestSubject())
            {
                testSubject.flush();
            }
        }

        [Test, OfMember("write")]
        public void write()
        {
            // Create Constructor Parameters
            using (MemoryStream stream = new MemoryStream())
            using (JavaOutputStreamAdapter testSubject = NewTestSubject(stream))
            {
                int b = 1;

                testSubject.write(b);
                testSubject.flush();

                Assert.AreEqual(1, stream.Length);
            }
        }
    }
}
