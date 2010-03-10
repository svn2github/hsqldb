using System;
using NUnit.Framework;
using System.Data.Hsqldb.TestCoverage;
using System.IO;

namespace System.Data.Hsqldb.Common.IO.UnitTests
{
    [TestFixture, Category("IO"), ForSubject(typeof(JavaWriterWrapper))]
    public class TestJavaWriterWrapper
    {
        static JavaWriterWrapper NewTestSubject()
        {
            return NewTestSubject(new java.io.StringWriter());
        }

        static JavaWriterWrapper NewTestSubject(java.io.Writer writer)
        {
            return new JavaWriterWrapper(writer);
        }

        [Test, OfMember("Dispose"), ExpectedException(typeof(ObjectDisposedException))]
        public virtual void Dispose()
        {
            TextWriter testSubject;

            using (testSubject = NewTestSubject())
            {
                testSubject.Write("asdasd");
            }

            testSubject.Write("asasdasd");
        }

        [Test, OfMember("Flush")]
        public virtual void Flush()
        {
            using (TextWriter testSubject = NewTestSubject())
            {
                testSubject.Flush();
            }
        }

        [Test, OfMember("Write")]
        public virtual void Write()
        {
            java.io.StringWriter stringWriter = new java.io.StringWriter();
            using (TextWriter testSubject = NewTestSubject(stringWriter))
            {
                char value = 'a';

                testSubject.Write(value);
                testSubject.Flush();

                Assert.AreEqual("a", stringWriter.toString());
            }
        }
    }
}
