using System;
using NUnit.Framework;
using System.Data.Hsqldb.TestCoverage;
using System.IO;

namespace System.Data.Hsqldb.Common.Lob.UnitTests
{
    [TestFixture, ForSubject(typeof(JdbcClob))]
    public class TestJdbcClob
    {
        static readonly string LobChars = "123456789";
        static readonly int LobCharsLength = LobChars.Length;
        static JdbcClob NewTestSubject()
        {
            return new JdbcClob(LobChars);
        }

        [Test, OfMember("Free")]
        public void Free()
        {
            IClob testSubject = NewTestSubject();

            long length = testSubject.Length;

            Assert.AreEqual(LobCharsLength, length);

            try
            {
                testSubject.Wrap("5678");

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
                Stream stream = testSubject.GetAsciiStream();

                Assert.Fail("successful invocation of GetAsciiStream() after Free()");
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
                TextReader reader = testSubject.GetCharacterStream();

                Assert.Fail("successful invocation of GetCharactertream() after Free()");
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
                string chars = testSubject.GetSubString(0L, (int)length);

                Assert.Fail("successful invocation of GetSubString(long,int) after Free()");
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
                long pos = testSubject.Position("234", 1);

                Assert.Fail("successful invocation of Position(string,long) after Free()");
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
                long pos = testSubject.Position(new JdbcClob("234"), 1);

                Assert.Fail("successful invocation of Position(IClob,long) after Free()");
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
                Stream stream = testSubject.SetAsciiStream(1);

                Assert.Fail("successful invocation of SetAsciiStream(long) after Free()");
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
                TextWriter writer = testSubject.SetCharacterStream(1);

                Assert.Fail("successful invocation of SetCharacterStream(long) after Free()");
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
                testSubject.SetString(1, "234");

                Assert.Fail("successful invocation of SetString(long,string) after Free()");
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
                testSubject.SetString(1, "234", 0, 3);

                Assert.Fail("successful invocation of SetString(long,string,int,int) after Free()");
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
                testSubject.Wrap("1234");
            }
            catch (Exception ex)
            {
                Assert.Fail("Wrap(object) raised exception after Free(): " + ex.Message);
            }
        }

        [Test, OfMember("GetAsciiStream")]
        public void GetAsciiStream()
        {
            IClob testSubject = NewTestSubject();

            using (Stream stream = testSubject.GetAsciiStream())
            {
                for (int i = 0; i < LobCharsLength; i++)
                {
                    Assert.AreEqual(LobChars[i], (char)stream.ReadByte());
                }

                Assert.AreEqual(-1, stream.ReadByte());
            }
        }

        [Test, OfMember("GetCharacterStream")]
        public void GetCharacterStream()
        {
            IClob testSubject = NewTestSubject();

            using (TextReader reader = testSubject.GetCharacterStream())
            {
                for (int i = 0; i < LobCharsLength; i++)
                {
                    Assert.AreEqual(LobChars[i], (char)reader.Read());
                }

                Assert.AreEqual(-1, reader.Read());
            }
        }

        [Test, OfMember("GetSubString")]
        public void GetSubString()
        {
            IClob testSubject = NewTestSubject();

            string actual = testSubject.GetSubString(1, LobCharsLength);

            Assert.AreEqual(LobChars, actual);

            for (int i = 0; i < testSubject.Length; i++)
            {
                string substring = testSubject.GetSubString(i + 1, (int)testSubject.Length - i);

                Assert.AreEqual((int)testSubject.Length - i, substring.Length);
                Assert.AreEqual(LobChars.Substring(i, (int)testSubject.Length - i), substring);
            }

            try
            {
                // CHECKME:  determine if this degnenerate borderline case should really be allowed....
                actual = testSubject.GetSubString(LobCharsLength + 1, 0);
            }
            catch (Exception ex)
            {
                Assert.Fail(
                    "failed invocation of GetSubString(pos,len) with zero len and pos one greater than max valid data position: "
                    + ex.Message);
            }

            try
            {
                actual = testSubject.GetSubString(0, LobCharsLength);

                Assert.Fail("successful invocation of GetSubString(pos,len) with pos value too small (< 1)");
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
                actual = testSubject.GetSubString(LobCharsLength + 1, 1);

                Assert.Fail("successful invocation of GetSubString(pos,len) with pos value too large (> LobCharsLength)");
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
                actual = testSubject.GetSubString(LobCharsLength + 1, 1);

                Assert.Fail("successful invocation of GetSubString(pos,len) with pos value too large (> LobCharsLength) for non-zero len");
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
                actual = testSubject.GetSubString(1, -1);

                Assert.Fail("successful invocation of GetSubString(pos,len) with negative len");
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
                actual = testSubject.GetSubString(1, LobCharsLength + 1);

                Assert.Fail("successful invocation of GetSubString(pos,len) with len > LobCharsLength");
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
            IClob testSubject = NewTestSubject();

            for (int i = 1; i <= LobCharsLength; i++)
            {
                long p1 = testSubject.Position("" + i, 1);

                Assert.AreEqual(i, p1);
            }

            for (int i = 1; i <= LobCharsLength; i++)
            {
                long p2 = testSubject.Position(new JdbcClob("" + i), 1);

                Assert.AreEqual(i, p2);
            }

            long p3 = testSubject.Position("42", 1);

            Assert.AreEqual(-1, p3);

            long p4 = testSubject.Position(new JdbcClob("42"), 1);

            Assert.AreEqual(-1, p4);
        }

        [Test, OfMember("SetAsciiStream")]
        public void SetAsciiStream()
        {
            IClob testSubject = NewTestSubject();

            if (testSubject.CanWrite)
            {
                Stream stream = testSubject.SetAsciiStream(1);

                stream.WriteByte((byte)'a');
            }

            try
            {
                Stream stream = testSubject.SetAsciiStream(0);

                Assert.Fail("successful invocation of SetAsciiStream(pos) with pos too small (<1)");
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
                Stream stream = testSubject.SetAsciiStream(LobCharsLength + 1);

                Assert.Fail("successful invocation of SetAsciiStream(pos) with pos too large (LobCharsLength + 1)");
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

        [Test, OfMember("SetCharacterStream")]
        public void SetCharacterStream()
        {
            IClob testSubject = NewTestSubject();

            if (testSubject.CanWrite)
            {
                TextWriter writer = testSubject.SetCharacterStream(1);

                writer.Write('a');
            }

            try
            {
                TextWriter writer = testSubject.SetCharacterStream(0);

                Assert.Fail("successful invocation of SetCharacterStream(pos) with pos too small (<1)");
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
                TextWriter writer = testSubject.SetCharacterStream(LobCharsLength + 1);

                Assert.Fail("successful invocation of SetCharacterStream(pos) with pos too large (LobCharsLength + 1)");
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

        [Test, OfMember("SetString")]
        public void SetString()
        {
            IClob testSubject = NewTestSubject();

            if (testSubject.CanWrite)
            {
                testSubject.SetString(1, "123");
                testSubject.SetString(1, "123", 0, 3);
            }

            try
            {
                testSubject.SetString(0, "123");

                Assert.Fail("successful invocation of SetString(pos,str) with pos too small (<1)");
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
                testSubject.SetString(LobCharsLength + 1, "123");

                Assert.Fail("successful invocation of SetChars(pos,str) with "
                    + "pos too large (> LobCharsLength)");
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
                testSubject.SetString(1, null);

                Assert.Fail("successful invocation of SetString(pos,str) with null str");
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
                testSubject.SetString(1, "123", -1, 3);

                Assert.Fail("successful invocation of SetString(pos,str,offs,len) with offs < 0");
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
                testSubject.SetString(1, "123", 4, 3);

                Assert.Fail("successful invocation of SetString(pos,str,offs,len) "
                    + "with offs > str.length");
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
                testSubject.SetString(1, "123", 0, -1);

                Assert.Fail("successful invocation of SetString(pos,str,offs,len) with len < 0");
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
                testSubject.SetString(1, "123", 0, 4);

                Assert.Fail("successful invocation of SetString(pos,str,offs,len) "
                    + "with len > str.length");
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
            IClob testSubject = NewTestSubject();

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
                testSubject.Truncate(testSubject.Length + 10);

                Assert.Fail("successful invocation of Truncate(len) "
                    + "with len > testSubject.Length");
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
            }
        }

        [Test, OfMember("UnWrap")]
        public void UnWrap()
        {
            IClob testSubject = NewTestSubject();

            object wrapped = testSubject.UnWrap();

            Assert.IsInstanceOfType(typeof(org.hsqldb.jdbc.jdbcClob), wrapped);
        }

        [Test, OfMember("Wrap")]
        public void Wrap()
        {
            IBlob testSubject = new JdbcBlob();

            if (testSubject.CanWrap)
            {
                if (testSubject.CanWrapType(typeof(Stream)))
                {
                    testSubject.Wrap(new MemoryStream(new byte[] {
                        (byte)'1', (byte)'2', (byte)'3', (byte)'4' }));
                }

                testSubject = new JdbcBlob();

                if (testSubject.CanWrapType(typeof(TextReader)))
                {
                    testSubject.Wrap(new StringReader("1234"));
                }

                testSubject = new JdbcBlob();

                if (testSubject.CanWrapType(typeof(string)))
                {
                    testSubject.Wrap("1234");
                }

                testSubject = new JdbcBlob();

                if (testSubject.CanWrapType(typeof(java.sql.Clob)))
                {
                    java.sql.Clob clob = new org.hsqldb.jdbc.jdbcClob("1234");

                    testSubject.Wrap(clob);
                }
            }
        }
    }
}
