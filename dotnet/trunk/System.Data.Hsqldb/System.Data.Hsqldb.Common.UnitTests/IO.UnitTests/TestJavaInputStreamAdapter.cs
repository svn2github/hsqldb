#region Using
using System;
using System.Data.Hsqldb.TestCoverage;
using System.IO;
using System.Text;
using NUnit.Framework;
#endregion

namespace System.Data.Hsqldb.Common.IO.UnitTests
{
    [TestFixture, ForSubject(typeof(JavaInputStreamAdapter))]
    public class TestJavaInputStreamAdapter
    {
        static JavaInputStreamAdapter NewTestSubject(string s, Encoding e)
        {
            MemoryStream ms = new System.IO.MemoryStream();

            using (StreamWriter sw = new StreamWriter(ms, e))
            {
                sw.Write(s);
                sw.Flush();
            }

            ms.Position = 0;

            return new JavaInputStreamAdapter(ms);
        }

        [Test, OfMember("close")]
        public void close()
        {
            using (JavaInputStreamAdapter testSubject = NewTestSubject("asdf", Encoding.UTF8))
            {
                testSubject.close();
            }
        }

        [Test, OfMember("Dispose"), ExpectedException(typeof(ObjectDisposedException))]
        public void Dispose()
        {
            JavaInputStreamAdapter testSubject;

            using (testSubject = NewTestSubject("asdf", Encoding.UTF8))
            {

            }

            testSubject.read();
        }

        [Test, OfMember("read")]
        public void read()
        {
            using (JavaInputStreamAdapter testSubject = NewTestSubject("asdf", Encoding.UTF8))
            {
                testSubject.read();
            }
        }
    }
}
