#region Using
using System;
using System.Data.Hsqldb.TestCoverage;
using System.IO;
using System.Text;
using NUnit.Framework;
#endregion

namespace System.Data.Hsqldb.Common.IO.UnitTests
{
    [TestFixture, Category("IO"), ForSubject(typeof(JavaInputStreamAdapter))]
    public class TestJavaInputStreamAdapter
    {
        static JavaInputStreamAdapter NewTestSubject(string s, Encoding e, bool closed)
        {
            MemoryStream ms = new System.IO.MemoryStream();

            StreamWriter sw = new StreamWriter(ms, e);

            sw.Write(s);
            sw.Flush();

            ms.Position = 0;

            if (closed)
            {
                ms.Close();
            }

            return new JavaInputStreamAdapter(ms);
        }

        static JavaInputStreamAdapter NewTestSubject(string s, Encoding e)
        {
            return NewTestSubject(s, e, false);
        }

        static JavaInputStreamAdapter NewClosedTestSubject(string s, Encoding e)
        {
            return NewTestSubject(s, e, true);
        }

        [Test, OfMember("ctor"), ExpectedException(typeof(ArgumentNullException))]
        
        public void ctorWithNullStream()
        {
            using (JavaInputStreamAdapter testSubject = new JavaInputStreamAdapter(null))
            {

            }
        }

        [Test, OfMember("available")]
        public void available()
        {
            using (JavaInputStreamAdapter testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {
                int available = testSubject.available();

                Assert.GreaterOrEqual(available,0);
            }
        }

        [Test, OfMember("available"), ExpectedException(typeof(java.io.IOException))]
        public void availableWithIOException()
        {
            using (JavaInputStreamAdapter testSubject = new JavaInputStreamAdapter(new BaseStreamAdapter()))
            {
                int available = testSubject.available();
            }
        }

        [Test, OfMember("close")]
        public void close()
        {
            using (JavaInputStreamAdapter testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {
                testSubject.close();
            }
        }

        [Test, OfMember("close"), ExpectedException(typeof(java.io.IOException))]
        public void closeWithIOException()
        {
            JavaInputStreamAdapter testSubject = new JavaInputStreamAdapter(new BaseNonClosableStream());

            testSubject.close();

        }

        [Test, OfMember("close"), OfMember("Dispose")]
        public void closeAfterDispose()
        {
            JavaInputStreamAdapter testSubject = NewClosedTestSubject("asdf", Encoding.ASCII);

            testSubject.close();
            testSubject.close();
        }

        [Test, OfMember("Dispose")]
        public void Dispose()
        {
            JavaInputStreamAdapter testSubject = NewTestSubject("asdf", Encoding.ASCII);

            testSubject.Dispose();
        }

        [Test, OfMember("available"), OfMember("Dispose")]
        public void availableAfterDispose()
        {
            JavaInputStreamAdapter testSubject = NewTestSubject("asdf", Encoding.ASCII);

            testSubject.Dispose();

            int available = testSubject.available();

            Assert.AreEqual(0, available);
        }

        [Test, OfMember("skip"), OfMember("Dispose"), ExpectedException(typeof(java.io.IOException))]
        public void skipAfterDispose()
        {
            JavaInputStreamAdapter testSubject = NewTestSubject("asdf", Encoding.ASCII);

            testSubject.Dispose();

            try
            {
                testSubject.skip(1);
            }
            catch (Exception ex)
            {
                java.io.IOException ioe = ex as java.io.IOException;

                if (ioe != null)
                {
                    System.Exception cause = ioe.getCause();
                    Assert.IsNotNull(cause);
                    Assert.IsInstanceOfType(typeof(System.ObjectDisposedException), cause);
                }

                throw;
            }
        }

        [Test, OfMember("reset"), OfMember("Dispose"), ExpectedException(typeof(java.io.IOException),ExpectedMessage="mark/reset not supported")]
        public void resetAfterDispose()
        {
            JavaInputStreamAdapter testSubject = NewTestSubject("asdf", Encoding.ASCII);

            testSubject.Dispose();

            testSubject.reset();
        }

        [Test, OfMember("read"), OfMember("Dispose"), ExpectedException(typeof(java.io.IOException))]
        public void readByteAfterDispose()
        {
            JavaInputStreamAdapter testSubject = NewTestSubject("asdf", Encoding.ASCII);

            testSubject.Dispose();

            try
            {
                testSubject.read();
            }
            catch (Exception ex)
            {
                java.io.IOException ioe = ex as java.io.IOException;

                if (ioe != null)
                {
                    System.Exception cause = ioe.getCause();
                    Assert.IsNotNull(cause);
                    Assert.IsInstanceOfType(typeof(System.ObjectDisposedException), cause);
                }

                throw;
            }
        }

        [Test, OfMember("read(byte[])"), OfMember("Dispose"), ExpectedException(typeof(java.io.IOException))]
        public void readIntoBufferAfterDispose()
        {
            JavaInputStreamAdapter testSubject = NewTestSubject("asdf", Encoding.ASCII);

            testSubject.Dispose();

            byte[] buff = new byte[1];

            try
            {
                testSubject.read(buff);
            }
            catch (Exception ex)
            {
                java.io.IOException ioe = ex as java.io.IOException;

                if (ioe != null)
                {
                    System.Exception cause = ioe.getCause();
                    Assert.IsNotNull(cause);
                    Assert.IsInstanceOfType(typeof(System.ObjectDisposedException), cause);
                }

                throw;
            }
        }

        [Test, OfMember("read(byte[],int,int)"), OfMember("Dispose"), ExpectedException(typeof(java.io.IOException))]
        public void readIntoBufferWithOffsetAfterDispose()
        {
            JavaInputStreamAdapter testSubject = NewTestSubject("asdf", Encoding.ASCII);

            testSubject.Dispose();

            byte[] buff = new byte[2];

            try
            {
                testSubject.read(buff, 1, 2);
            }
            catch (Exception ex)
            {
                java.io.IOException ioe = ex as java.io.IOException;

                if (ioe != null)
                {
                    System.Exception cause = ioe.getCause();
                    Assert.IsNotNull(cause);
                    Assert.IsInstanceOfType(typeof(System.ObjectDisposedException), cause);
                }

                throw;
            }
        }

        [Test, OfMember("read")]
        public void readByte()
        {
            using (JavaInputStreamAdapter testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {
                int b = testSubject.read();

                Assert.AreEqual((int)'a', b);

                b = testSubject.read();

                Assert.AreEqual((int)'s', b);

                b = testSubject.read();

                Assert.AreEqual((int)'d', b);

                b = testSubject.read();

                Assert.AreEqual((int)'f', b);

                b = testSubject.read();

                Assert.That(b == -1);

                b = testSubject.read();

                Assert.That(b == -1);
            }
        }

        [Test, OfMember("read(byte[])")]
        public void readIntoBuffer()
        {
            byte[] buffer = new byte[4];

            using (JavaInputStreamAdapter testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {
                int rval = testSubject.read(buffer);

                Assert.That(rval == 4);

                Assert.AreEqual((int)'a', buffer[0]);
                Assert.AreEqual((int)'s', buffer[1]);
                Assert.AreEqual((int)'d', buffer[2]);
                Assert.AreEqual((int)'f', buffer[3]);

                rval = testSubject.read(buffer);

                Assert.That(rval == -1);

                rval = testSubject.read(buffer);

                Assert.That(rval == -1);
            }
        }

        [Test, OfMember("read(byte[],int,int)")]
        public void readIntoBufferWithOffset()
        {
            byte[] buffer = new byte[4];

            using (JavaInputStreamAdapter testSubject = NewTestSubject("asdf", Encoding.ASCII))
            {
                int rval = testSubject.read(buffer, 1, 2);

                Assert.AreEqual(2, rval);

                Assert.AreEqual((int)'a', buffer[1]);
                Assert.AreEqual((int)'s', buffer[2]);

                rval = testSubject.read(buffer,0,2);

                Assert.AreEqual(2, rval);

                rval = testSubject.read(buffer);

                Assert.AreEqual(-1, rval);
            }
        }
    }
}
