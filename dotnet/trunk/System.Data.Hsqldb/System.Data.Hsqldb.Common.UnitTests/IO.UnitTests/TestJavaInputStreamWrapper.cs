#region Using
using System;
using NUnit.Framework;
using System.Data.Hsqldb.TestCoverage;
using System.IO;
using System.Text;
#endregion

namespace System.Data.Hsqldb.Common.IO.UnitTests
{
    [TestFixture, Category("IO"), ForSubject(typeof(JavaInputStreamWrapper))]
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

        [Test, OfMember("ctor"),ExpectedException(typeof(ArgumentNullException))]
        public void ctorWithNullInputStream()
        {
            JavaInputStreamWrapper wrapper = new JavaInputStreamWrapper(null);
        }

        [Test, OfMember("CanRead")]
        public void CanRead()
        {
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {
                bool expected = true;
                bool actual = testSubject.CanRead;

                Assert.AreEqual(expected, actual);
            }
        }

        [Test, OfMember("CanSeek")]
        public void CanSeek()
        {
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {
                bool expected = true;
                bool actual = testSubject.CanSeek;

                Assert.AreEqual(expected, actual);
            }
        }

        [Test, OfMember("CanWrite")]
        public void CanWrite()
        {
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.ASCII))
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

            using (testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {

            }

            testSubject.ReadByte();
        }

        [Test, OfMember("Dispose"), ExpectedException(typeof(System.IO.IOException))]
        public void DisposeWithIOException()
        {
            JavaInputStreamWrapper testSubject = new JavaInputStreamWrapper(new BaseJavaInputStream());

            testSubject.Dispose();
        }

        [Test, OfMember("Flush")]
        public void Flush()
        {
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {
                testSubject.Flush(); // expect no-op.
            }
        }

        [Test, OfMember("Length"), ExpectedException(typeof(NotSupportedException))]
        public void Length()
        {
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {
                long actual = testSubject.Length;
            }
        }

        [Test, OfMember("Position")]
        public void Position_get()
        {
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {
                long expected = 0;
                long actual = testSubject.Position;

                Assert.AreEqual(expected, actual);
            }
        }

        [Test, OfMember("Position"),ExpectedException(typeof(NotSupportedException))]
        public void Position_set()
        {
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {
                testSubject.Position = 0;
            }
        }
        
        [Test, OfMember("Read")]
        public void Read()
        {
            using(JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.ASCII))
            using (StreamReader sr = new StreamReader(testSubject))
            {
                string expected = "asdf";
                string actual = sr.ReadToEnd();

                Assert.AreEqual(expected, actual);
            }
        }

        [Test, OfMember("ReadByte")]
        public void ReadByte()
        {
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {
                int b = testSubject.ReadByte();

                Assert.AreEqual((int)'a', b);

                b = testSubject.ReadByte();

                Assert.AreEqual((int)'s', b);

                b = testSubject.ReadByte();

                Assert.AreEqual((int)'d', b);

                b = testSubject.ReadByte();

                Assert.AreEqual((int)'f', b);

                b = testSubject.ReadByte();

                Assert.AreEqual(-1, b);

                b = testSubject.ReadByte();

                Assert.AreEqual(-1, b);
            }
        }

        [Test, OfMember("Read(byte[],int,int)")]
        public void ReadIntoBufferWithValidOffsetAndCount()
        {
            byte[] buffer = new byte[4];
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {
                int rval = testSubject.Read(buffer,0,4);

                Assert.AreEqual(4, rval);

                Assert.AreEqual((int)'a',buffer[0]);
                Assert.AreEqual((int)'s', buffer[1]);
                Assert.AreEqual((int)'d', buffer[2]);
                Assert.AreEqual((int)'f', buffer[3]);

                rval = testSubject.Read(buffer, 0, 4);

                Assert.AreEqual(0, rval);

                rval = testSubject.Read(buffer, 0, 4);

                Assert.AreEqual(0, rval);               
            }
        }

        [Test, OfMember("Read(byte[],int,int)"), ExpectedException(typeof(ArgumentOutOfRangeException))]
        public void ReadIntoBufferWithNegativeOffset()
        {
            byte[] buffer = new byte[4];
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {
                int rval = testSubject.Read(buffer, -1, 4);
            }
        }

        [Test, OfMember("Read(byte[],int,int)"), ExpectedException(typeof(ArgumentOutOfRangeException))]
        public void ReadIntoBufferWithOffsetAndNegativeCount()
        {
            byte[] buffer = new byte[4];
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {
                int rval = testSubject.Read(buffer, 0, -1);
            }
        }

        [Test, OfMember("Read(byte[],int,int)"), ExpectedException(typeof(ArgumentException))]
        public void ReadIntoBufferWithOffsetPlusCountGreaterThanBufferLength()
        {
            byte[] buffer = new byte[4];
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {
                int rval = testSubject.Read(buffer, 3, 4);
            }
        }

        [Test, OfMember("Read(byte[],int,int)"), ExpectedException(typeof(ArgumentNullException))]
        public void ReadIntoNullBuffer()
        {
            byte[] buffer = null;
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {
                int rval = testSubject.Read(buffer, 0, 4);
            }
        }

        [Test, OfMember("Read(byte[],int,int)"), ExpectedException(typeof(System.IO.IOException))]
        public void ReadBufferWithJavaIOException()
        {
            byte[] buffer = new byte[1];
            JavaInputStreamWrapper testSubject = new JavaInputStreamWrapper(new BaseJavaInputStream());
           
            int rval = testSubject.Read(buffer, 0, 1);
        }
        
        [Test, OfMember("Seek")]
        public void Seek()
        {
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {
                long offset = 0;
                SeekOrigin origin = SeekOrigin.Begin;

                long newPosition = testSubject.Seek(offset, origin);
            }
        }

        [Test, OfMember("Seek"), ExpectedException(typeof(System.IO.IOException))]
        public void SeekWithIOException()
        {
            JavaInputStreamWrapper testSubject = new JavaInputStreamWrapper(new BaseJavaInputStream());

            testSubject.Seek(1, SeekOrigin.Current);
        }

        [Test, OfMember("Seek"), ExpectedException(typeof(NotSupportedException))]
        public void SeekWithNotSupportedException()
        {
            JavaInputStreamWrapper testSubject = new JavaInputStreamWrapper(new BaseJavaInputStream());

            testSubject.Seek(1, SeekOrigin.End);
        }
        
        [Test, OfMember("SetLength"), ExpectedException(typeof(NotSupportedException))]
        public virtual void SetLength()
        {
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {
                long newLength = 0;
                testSubject.SetLength(newLength);
            }
        }
        
        [Test, OfMember("Write"), ExpectedException(typeof(NotSupportedException))]
        public virtual void Write()
        {
            using (JavaInputStreamWrapper testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {
                testSubject.Write(null, 0, 0);
            }
        }
    }
}
