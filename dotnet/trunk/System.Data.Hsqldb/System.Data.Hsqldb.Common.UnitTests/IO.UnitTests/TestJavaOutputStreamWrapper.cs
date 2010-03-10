using System;
using NUnit.Framework;
using System.Data.Hsqldb.TestCoverage;
using System.Data.Hsqldb.Common.IO;
using System.IO;

namespace System.Data.Hsqldb.Common.IO.UnitTests
{
    [TestFixture, Category("IO"), ForSubject(typeof(JavaOutputStreamWrapper))]
    public class TestJavaOutputStreamWrapper
    {
        static JavaOutputStreamWrapper NewTestSubject()
        {
            return NewTestSubject(new java.io.ByteArrayOutputStream());
        }

        static JavaOutputStreamWrapper NewTestSubject(java.io.OutputStream outputStream)
        {
            return new JavaOutputStreamWrapper(outputStream);
        }

        [Test, OfMember("Flush")]
        public void Flush()
        {

            using (JavaOutputStreamWrapper testSubject = NewTestSubject())
            {
                testSubject.Flush();
            }
        }
        
        [Test, OfMember("Read"), ExpectedException(typeof(NotSupportedException))]
        public void Read()
        {
            using (JavaOutputStreamWrapper testSubject = NewTestSubject())
            {
                testSubject.Read(null, 0, 0);
            }
        }
        
        [Test, OfMember("Seek"), ExpectedException(typeof(NotSupportedException))]
        public void Seek()
        {
            using (JavaOutputStreamWrapper testSubject = NewTestSubject())
            {
                testSubject.Seek(0, SeekOrigin.Begin);
            } 
        }

        [Test, OfMember("SetLength"), ExpectedException(typeof(NotSupportedException))]
        public void SetLength()
        {
            using (JavaOutputStreamWrapper testSubject = NewTestSubject())
            {
                testSubject.SetLength(0);
            } 
        }
        
        [Test, OfMember("Write")]
        public void Write()
        {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

            using (JavaOutputStreamWrapper testSubject = NewTestSubject(baos))
            {
                testSubject.Write(new byte[] { 1, 2, 3, 4, 5 }, 0, 5);
                testSubject.Flush();

                byte[] expected = new byte[] { 1, 2, 3, 4, 5 };
                byte[] actual = baos.toByteArray();

                Assert.AreEqual(expected.Length, actual.Length);

                for (int i = 0; i < expected.Length; i++)
                {
                    Assert.AreEqual(expected[i], actual[i]);
                }
            }
        }
    }
}
