using System;
using System.Data.Hsqldb.TestCoverage;
using System.IO;
using NUnit.Framework;

namespace System.Data.Hsqldb.Common.IO.UnitTests
{   
    [TestFixture, Category("IO"), ForSubject(typeof(JavaReaderWrapper))]
    public class TestJavaReaderWrapper
    {
        static JavaReaderWrapper NewTestSubject(string s)
        {
            return NewTestSubject(new java.io.StringReader(s));
        }

        static JavaReaderWrapper NewTestSubject(java.io.Reader reader)
        {
            return new JavaReaderWrapper(reader);
        }
        
        [Test, OfMember("Peek")]
        public void Peek()
        {
            using (TextReader testSubject = NewTestSubject("a"))
            {
                int expected = "a"[0];
                int actual = testSubject.Peek();

                Assert.AreEqual(expected, actual);
            }
        }

        [Test, OfMember("Read")]
        public void Read()
        {
            using (TextReader testSubject = NewTestSubject("a"))
            {
                int expected = (int)"a"[0];
                int actual = testSubject.Read();

                Assert.AreEqual(expected, actual);
            }
        }

        [Test, OfMember("ReadLine")]
        public virtual void ReadLine()
        {
            using (TextReader testSubject = NewTestSubject("a\na"))
            {
                string expected = "a";
                string actual = testSubject.ReadLine();

                Assert.AreEqual(expected, actual);
            }
        }

        [Test, OfMember("ReadToEnd")]
        public virtual void ReadToEnd()
        {
            using (TextReader testSubject = NewTestSubject("a\na"))
            {
                string expected = "a\na";
                string actual = testSubject.ReadToEnd();

                Assert.AreEqual(expected, actual);
            }
        }
    }
}
