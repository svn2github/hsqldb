#region Using
using System;
using NUnit.Framework;
using System.Data.Hsqldb.TestCoverage;
using System.IO;
using System.Text;
#endregion

namespace System.Data.Hsqldb.Common.IO.UnitTests
{
    [TestFixture, ForSubject(typeof(JavaInputStreamWrapper))]
    public class TestJavaInputStreamWrapper
    {
        static JavaInputStreamWrapper NewTestSubject(string s, Encoding e)
        {
            using(MemoryStream ms = new MemoryStream())
            using (StreamWriter sw = new StreamWriter(ms, e))
            {
                sw.Write(s);
                sw.Flush();

                return new JavaInputStreamWrapper( new java.io.ByteArrayInputStream(ms.ToArray()));
            }
        }

        public void CanRead()
        {
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.UTF8))
            {
                bool expected = true;
                bool actual = testSubject.CanRead;

                Assert.AreEqual(expected, actual);
            }
        }

        public void CanSeek()
        {
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.UTF8))
            {
                bool expected = true;
                bool actual = testSubject.CanSeek;

                Assert.AreEqual(expected, actual);
            }
        }

        public void CanWrite()
        {
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.UTF8))
            {
                bool expected = false;
                bool actual = testSubject.CanWrite;

                Assert.AreEqual(expected, actual);
            }
        }

        [Test, OfMember("Dispose"), ExpectedException(typeof(ObjectDisposedException))]
        public void Dispose()
        {
            JavaInputStreamWrapper testSubject;

            using (testSubject = NewTestSubject("asdf", Encoding.UTF8))
            {

            }

            testSubject.ReadByte();
        }

        [Test, OfMember("Flush")]
        public void Flush()
        {
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.UTF8))
            {
                testSubject.Flush(); // expect no-op.
            }
        }

        [Test, OfMember("Length"), ExpectedException(typeof(NotSupportedException))]
        public void Length()
        {
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.UTF8))
            {
                long actual = testSubject.Length;
            }
        }

        [Test, OfMember("Position")]
        public void Position()
        {
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.UTF8))
            {
                long expected = 0;
                long actual = testSubject.Position;

                Assert.AreEqual(expected, actual);
            }
        }
        
        [Test, OfMember("Read")]
        public void Read()
        {
            using(JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.UTF8))
            using (StreamReader sr = new StreamReader(testSubject))
            {
                string expected = "asdf";
                string actual = sr.ReadToEnd();

                Assert.AreEqual(expected, actual);
            }
        }
        
        [Test, OfMember("Seek")]
        public void Seek()
        {
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.UTF8))
            {
                long offset = 0;
                SeekOrigin origin = SeekOrigin.Begin;

                long newPosition = testSubject.Seek(offset, origin);
            }
        }
        
        [Test, OfMember("SetLength"), ExpectedException(typeof(NotSupportedException))]
        public virtual void SetLength()
        {
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.UTF8))
            {
                long newLength = 0;
                testSubject.SetLength(newLength);
            }
        }
        
        [Test, OfMember("Write"), ExpectedException(typeof(NotSupportedException))]
        public virtual void Write()
        {
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.UTF8))
            {
                testSubject.Write(null, 0, 0);
            }
        }
    }
}
