using System;
using System.Data.Hsqldb.TestCoverage;
using NUnit.Framework;
using System.Data.Common;
using System.IO;
using System.Diagnostics;

namespace System.Data.Hsqldb.Common.Lob.UnitTests
{
    [TestFixture, ForSubject(typeof(JdbcBlob))]
    public class TestJdbcBlob
    {
        static readonly byte[] LobBytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        static readonly int LobBytesLength = LobBytes.Length;

        static JdbcBlob NewTestSubject()
        {
            return new JdbcBlob((byte[])LobBytes.Clone());
        }

        [Test, OfMember("Free")]
        public void Free()
        {
            IBlob testSubject = NewTestSubject();

            long length = testSubject.Length;

            Assert.AreEqual(LobBytesLength, length);

            try
            {
                testSubject.Wrap(new byte[] { 5, 6, 7, 8 });

                Assert.Fail("successful invocation of Wrap(object) before Free()");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(InvalidOperationException), ex);
            }

            testSubject.Free();

            try
            {                
                Stream stream = testSubject.GetBinaryStream();

                Assert.Fail("successful invocation of GetBinaryStream() after Free()");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(InvalidOperationException), ex);
            }

            try
            {
                byte[] bytes = testSubject.GetBytes(0L, (int) length);

                Assert.Fail("successful invocation of GetBytes(long,int) after Free()");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(InvalidOperationException), ex);
            }

            try
            {
                long len = testSubject.Length;

                Assert.Fail("successful invocation of Length property after Free()");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(InvalidOperationException), ex);
            }

            try
            {
                long pos = testSubject.Position(new byte[] { 2, 3, 4 }, 1);

                Assert.Fail("successful invocation of Position(byte[],long) after Free()");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(InvalidOperationException), ex);
            }

            try
            {
                long pos = testSubject.Position(new JdbcBlob(new byte[] { 2, 3, 4, }), 1);

                Assert.Fail("successful invocation of Position(IBlob,long) after Free()");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(InvalidOperationException), ex);
            }

            try
            {
                Stream stream = testSubject.SetBinaryStream(1);

                Assert.Fail("successful invocation of SetBinaryStream(long) after Free()");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(InvalidOperationException), ex);
            }

            try
            {
                testSubject.SetBytes(1, new byte[] { 1, 2, 3 });

                Assert.Fail("successful invocation of SetBytes(long, byte[]) after Free()");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(InvalidOperationException), ex);
            }
            
            try
            {
                testSubject.SetBytes(1, new byte[] { 1, 2, 3 }, 0, 3);

                Assert.Fail("successful invocation of SetBytes(long, byte[], int, int) after Free()");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(InvalidOperationException), ex);
            }

            try
            {
                testSubject.Truncate(0);

                Assert.Fail("successful invocation of Truncate(long) after Free()");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(InvalidOperationException), ex);
            }

            try
            {
                testSubject.Wrap(new byte[] { 1, 2, 3, 4 });
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.Fail("Wrap(object) raised exception after Free(): " + ex.Message);
            }
        }

        [Test, OfMember("GetBinaryStream")]
        public void GetBinaryStream()
        {
            IBlob testSubject = NewTestSubject();

            using (Stream stream = testSubject.GetBinaryStream())
            {
                for (int i = 0; i < LobBytesLength; i++)
                {
                    Assert.AreEqual(LobBytes[i], stream.ReadByte());
                }

                Assert.AreEqual(-1, stream.ReadByte());
            }
        }

        [Test, OfMember("GetBytes")]
        public void GetBytes()
        {
            IBlob testSubject = NewTestSubject();
            byte[] bytes = testSubject.GetBytes(1, LobBytesLength);

            Assert.AreEqual(LobBytesLength, bytes.Length);

            for (int i = 0; i < LobBytesLength; i++)
            {
                Assert.AreEqual(LobBytes[i], bytes[i]);
            }

            for (int i = 0; i < LobBytesLength; i++)
            {
                bytes = testSubject.GetBytes(LobBytesLength - i, i + 1);

                Assert.AreEqual(i + 1, bytes.Length);
            }

            try
            {
                // CHECKME:  determine if this degnenerate borderline case should really be allowed....
                bytes = testSubject.GetBytes(LobBytesLength + 1, 0);
            }
            catch (Exception ex)
            {
                Assert.Fail(
                    "failed invocation of GetBytes(pos,len) with zero len and pos greater than max valid data position: " 
                    + ex.Message); 
            }

            try
            {
                bytes = testSubject.GetBytes(0, LobBytesLength);

                Assert.Fail("successful invocation of GetBytes(pos,len) with pos value too small (< 1)");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
            }


            try
            {
                bytes = testSubject.GetBytes(LobBytesLength + 1, 1);

                Assert.Fail("successful invocation of GetBytes(pos,len) with pos value too large (> LobBytesLength)");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
            }

            try
            {
                bytes = testSubject.GetBytes(LobBytesLength + 1, 1);

                Assert.Fail("successful invocation of GetBytes(pos,len) with pos value too large (> LobBytesLength) for non-zero len");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
            }

            try
            {
                bytes = testSubject.GetBytes(1, -1);

                Assert.Fail("successful invocation of GetBytes(pos,len) with negative len");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
            }

            try
            {
                bytes = testSubject.GetBytes(1, LobBytesLength + 1);

                Assert.Fail("successful invocation of GetBytes(pos,len) with len > LobBytesLength");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
            }
        }

        [Test, OfMember("Position")]
        public void Position()
        {
            IBlob testSubject = NewTestSubject();

            for (int i = 1; i <= LobBytesLength; i++)
            {
                long p1 = testSubject.Position(new byte[] { (byte) i }, 1);

                Assert.AreEqual(i, p1);
            }

            for (int i = 1; i <= LobBytesLength; i++)
            {
                long p2 = testSubject.Position(new JdbcBlob(new byte[] { (byte) i }), 1);

                Assert.AreEqual(i, p2);
            }

            long p3 = testSubject.Position(new byte[] { 42 }, 1);

            Assert.AreEqual(-1, p3);

            long p4 = testSubject.Position(new JdbcBlob(new byte[] { 42 }), 1);

            Assert.AreEqual(-1, p4);
        }

        [Test, OfMember("SetBinaryStream")]
        public void SetBinaryStream()
        {
            IBlob testSubject = NewTestSubject();

            if (testSubject.CanWrite)
            {
                Stream stream = testSubject.SetBinaryStream(1);

                stream.WriteByte((byte)10);
            }

            try
            {
                Stream stream = testSubject.SetBinaryStream(0);

                Assert.Fail("successful invocation of SetBinaryStream(pos) with pos too small (<1)");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
            }

            try
            {
                Stream stream = testSubject.SetBinaryStream(LobBytesLength + 1);

                Assert.Fail("successful invocation of SetBinaryStream(pos) with pos too large (LobBytesLength + 1)");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
            }
        }

        [Test, OfMember("SetBytes")]
        public void SetBytes()
        {
            IBlob testSubject = NewTestSubject();

            if (testSubject.CanWrite)
            {
                testSubject.SetBytes(1, new byte[] { 1, 2, 3 });
                testSubject.SetBytes(1, new byte[] { 1, 2, 3 }, 0, 3);
            }

            try
            {
                testSubject.SetBytes(0, new byte[] { 1, 2, 3 });

                Assert.Fail("successful invocation of SetBytes(pos,buff) with pos too small (<1)");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
            }

            try
            {
                testSubject.SetBytes(LobBytesLength + 1, new byte[] { 1, 2, 3 });

                Assert.Fail("successful invocation of SetBytes(pos,buff) with pos too large (> LobBytesLength)");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
            }

            try
            {
                testSubject.SetBytes(1, null);

                Assert.Fail("successful invocation of SetBytes(pos,buff) with null buff");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
            }

            try
            {
                testSubject.SetBytes(1, new byte[] { 1, 2, 3 }, -1, 3);

                Assert.Fail("successful invocation of SetBytes(pos,buff,offs,len) with offs < 0");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
            }

            try
            {
                testSubject.SetBytes(1, new byte[] { 1, 2, 3 }, 4, 3);

                Assert.Fail("successful invocation of SetBytes(pos,buff,offs,len) with offs > buff.length");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
            }

            try
            {
                testSubject.SetBytes(1, new byte[] { 1, 2, 3 }, 0, -1);

                Assert.Fail("successful invocation of SetBytes(pos,buff,offs,len) with len < 0");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
            }

            try
            {
                testSubject.SetBytes(1, new byte[] { 1, 2, 3 }, 0, 4);

                Assert.Fail("successful invocation of SetBytes(pos,buff,offs,len) with len > buff.length");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
            }
        }

        [Test, OfMember("Truncate")]
        public void Truncate()
        {
            IBlob testSubject = NewTestSubject();

            if (testSubject.CanWrite)
            {
                long len = testSubject.Length;

                for (int i = (int)len; i >= 0; i--)
                {
                    testSubject.Truncate(i);

                    Assert.AreEqual(i, testSubject.Length);
                }
            }

            testSubject = NewTestSubject();

            try
            {
                testSubject.Truncate(-1);

                Assert.Fail("successful invocation of Truncate(len) with len < 0");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
            }

            try
            {
                testSubject.Truncate(testSubject.Length + 1);

                Assert.Fail("successful invocation of Truncate(len) with len > testSubject.Length");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
            }
        }

        [Test, OfMember("UnWrap")]
        public void UnWrap()
        {
            IBlob testSubject = NewTestSubject();

            object wrapped = testSubject.UnWrap();

            Assert.IsInstanceOfType(typeof(org.hsqldb.jdbc.jdbcBlob), wrapped);
        }

        [Test, OfMember("Wrap")]
        public void Wrap()
        {
            IBlob testSubject = new JdbcBlob();

            if (testSubject.CanWrap)
            {
                if (testSubject.CanWrapType(typeof(byte[])))
                {
                    testSubject.Wrap(new byte[] { 1, 2, 3, 4 });
                }

                testSubject = new JdbcBlob();

                if (testSubject.CanWrapType(typeof(Stream)))
                {
                    testSubject.Wrap(new MemoryStream(new byte[] { 1, 2, 3, 4 }));
                }

                testSubject = new JdbcBlob();

                if (testSubject.CanWrapType(typeof(java.sql.Blob)))
                {
                    java.sql.Blob blob = new org.hsqldb.jdbc.jdbcBlob(new byte[] { 1, 2, 3, 4 });

                    testSubject.Wrap(blob);
                }
            }
        }
    }
}
