using System;
using NUnit.Framework;
using System.Data.Hsqldb.Common.IO;
using System.Data.Hsqldb.TestCoverage;
using System.IO;

namespace System.Data.Hsqldb.Common.IO.UnitTests
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

        [Test, OfMember("ctor"),ExpectedException(typeof(ArgumentNullException))]
        public void ctorWithNullAdaptee()
        {
            using (JavaOutputStreamAdapter testSubject = NewTestSubject(null))
            {

            }
        }

        [Test, OfMember("close")]
        public void close()
        {
            using (JavaOutputStreamAdapter testSubject = NewTestSubject())
            {
                testSubject.close();
            }
        }

        [Test, OfMember("close"), ExpectedException(typeof(java.io.IOException))]
        public void closeWithIOException()
        {
            JavaOutputStreamAdapter testSubject = NewTestSubject(new BaseNonClosableStream());         
                
            testSubject.close();            
        }

        [Test, OfMember("Dispose"), ExpectedException(typeof(java.io.IOException))]
        public void Dispose()
        {
            JavaOutputStreamAdapter testSubject;

            using (testSubject = NewTestSubject())
            {
                testSubject.write(1);
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

        [Test, OfMember("flush"), ExpectedException(typeof(java.io.IOException))]
        public void flushWithIOException()
        {
            using (JavaOutputStreamAdapter testSubject = NewTestSubject(new BaseStreamAdapter()))
            {
                testSubject.flush();
            }
        }

        [Test, OfMember("write")]
        public void writeByte()
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

        [Test, OfMember("write(byte[],int,int")]
        public void writeFromBuffer()
        {
            byte[] buffer = new byte[] { (byte)'a', (byte)'s', (byte)'d', (byte)'f' };

            // Create Constructor Parameters
            using (MemoryStream stream = new MemoryStream())
            using (JavaOutputStreamAdapter testSubject = NewTestSubject(stream))
            {
                testSubject.write(buffer);

                Assert.AreEqual(4, stream.Length);

                stream.Position = 0;

                Assert.AreEqual((int)'a', stream.ReadByte());
                Assert.AreEqual((int)'s', stream.ReadByte());
                Assert.AreEqual((int)'d', stream.ReadByte());
                Assert.AreEqual((int)'f', stream.ReadByte());
                Assert.AreEqual(-1, stream.ReadByte());
            }
        }

        [Test, OfMember("write(byte[],int,int")]
        public void writeFromBufferWithValidOffsetAndLength()
        {
            byte[] buffer = new byte[] { (byte)'a', (byte)'s', (byte)'d', (byte)'f' };

            // Create Constructor Parameters
            using (MemoryStream stream = new MemoryStream())
            using (JavaOutputStreamAdapter testSubject = NewTestSubject(stream))
            {
                testSubject.write(buffer,0,4);

                Assert.AreEqual(4, stream.Length);

                stream.Position = 0;

                Assert.AreEqual((int)'a', stream.ReadByte());
                Assert.AreEqual((int)'s', stream.ReadByte());
                Assert.AreEqual((int)'d', stream.ReadByte());
                Assert.AreEqual((int)'f', stream.ReadByte());
                Assert.AreEqual(-1, stream.ReadByte());
            }
        }

        [Test, OfMember("write(byte[],int,int"), ExpectedException(typeof(System.NullReferenceException))]
        public void writeFromNullBuffer()
        {
            byte[] buffer = null;

            // Create Constructor Parameters
            using (MemoryStream stream = new MemoryStream()) 
            {
                JavaOutputStreamAdapter testSubject = NewTestSubject(stream);
            
                testSubject.write(buffer);
            }
        }

        [Test, OfMember("write(byte[],int,int"), ExpectedException(typeof(java.io.IOException))]
        public void writeFromBufferWithNegativeOffset()
        {
            byte[] buffer = new byte[1];

            // Create Constructor Parameters
            using (MemoryStream stream = new MemoryStream())
            using (JavaOutputStreamAdapter testSubject = NewTestSubject(stream))
            {
                testSubject.write(buffer,-1,1);
            }
        }

        [Test, OfMember("write(byte[],int,int"), ExpectedException(typeof(java.io.IOException))]
        public void writeFromBufferWithOffsetBeyondBufferLength()
        {
            byte[] buffer = new byte[1];

            // Create Constructor Parameters
            using (MemoryStream stream = new MemoryStream())
            using (JavaOutputStreamAdapter testSubject = NewTestSubject(stream))
            {
                testSubject.write(buffer, 2, 0);
            }
        }

        [Test, OfMember("write(byte[],int,int"), ExpectedException(typeof(java.io.IOException))]
        public void writeFromBufferWithNegativeLength()
        {
            byte[] buffer = new byte[1];

            // Create Constructor Parameters
            using (MemoryStream stream = new MemoryStream())
            using (JavaOutputStreamAdapter testSubject = NewTestSubject(stream))
            {
                testSubject.write(buffer, 0, -1);
            }
        }

        [Test, OfMember("write(byte[],int,int"), ExpectedException(typeof(java.io.IOException))]
        public void writeFromBufferWithOffsetPlusLengthGreaterThanBufferLength()
        {
            byte[] buffer = new byte[1];

            // Create Constructor Parameters
            using (MemoryStream stream = new MemoryStream())
            using (JavaOutputStreamAdapter testSubject = NewTestSubject(stream))
            {
                testSubject.write(buffer, 0, -1);
            }
        }
    }
}
