#region Using
using System;
using System.Data.Hsqldb.Common;
using System.Data.Hsqldb.Common.Enumeration;
using System.Data.Hsqldb.Common.Sql;
using System.Data.Hsqldb.TestCoverage;
using NUnit.Framework;
#endregion

namespace UnitTests
{
    [TestFixture, ForSubject(typeof(Tokenizer))]
    public class TestTokenizer
    {
        [Test, OfMember("GetNextAsBigint")]
        public void GetNextAsBigint()
        {
            Tokenizer testSubject = new Tokenizer("123456789123456789");

            long expected = 123456789123456789L;
            long actual = testSubject.GetNextAsBigint();

            Assert.AreEqual(expected, actual);
        }
        
        [Test, OfMember("GetNextAsInt")]
        public void GetNextAsInt()
        {
            Tokenizer testSubject = new Tokenizer("123456789");

            int expected = 123456789;
            int actual = testSubject.GetNextAsInt();

            Assert.AreEqual(expected, actual); 
        }
        
        [Test, OfMember("GetNextAsLiteralValue")]
        public void GetNextAsLiteralValue()
        {
            // Create Constructor Parameters

            Tokenizer testSubject = new Tokenizer();


            testSubject.Reset("123456789123456789"); 

            try
            {
                testSubject.GetNextAsLiteralValue(HsqlProviderType.Array);

                Assert.Fail("SQL ARRAY literal tokens are not supposed to be supported");
            }
            catch (HsqlDataSourceException)
            {
            }
            
            long bigint = (long) testSubject.GetNextAsLiteralValue(HsqlProviderType.BigInt);
            byte[] bytes = testSubject.GetNextAsLiteralValue(HsqlProviderType.Binary) as byte[];

            try
            {
                testSubject.GetNextAsLiteralValue(HsqlProviderType.Blob);

                Assert.Fail("SQL BLOB literal tokens are not supposed to be supported at this time");
            }
            catch (HsqlDataSourceException)
            {
            }

            //testSubject.GetNextAsLiteralValue(HsqlProviderType.Boolean);
            //testSubject.GetNextAsLiteralValue(HsqlProviderType.Char);
            try
            {
                testSubject.GetNextAsLiteralValue(HsqlProviderType.Clob);

                Assert.Fail("SQL CLOB literal tokens are not supposed to be supported at this time");
            }
            catch (HsqlDataSourceException)
            {
            }
            try
            {
                testSubject.GetNextAsLiteralValue(HsqlProviderType.DataLink);

                Assert.Fail("SQL DATALINK literal tokens are not supposed to be supported at this time");
            }
            catch (HsqlDataSourceException)
            {
            }
            //testSubject.GetNextAsLiteralValue(HsqlProviderType.Date);
            //testSubject.GetNextAsLiteralValue(HsqlProviderType.Decimal);
            try
            {
                testSubject.GetNextAsLiteralValue(HsqlProviderType.Distinct);

                Assert.Fail("SQL DISTINCT literal tokens are not supposed to be supported");
            }
            catch (HsqlDataSourceException)
            {
            }
            //testSubject.GetNextAsLiteralValue(HsqlProviderType.Double);
            //testSubject.GetNextAsLiteralValue(HsqlProviderType.Float);
            //testSubject.GetNextAsLiteralValue(HsqlProviderType.Integer);
            try
            {
                testSubject.GetNextAsLiteralValue(HsqlProviderType.JavaObject);

                Assert.Fail("SQL JAVA_OBJECT literal tokens are not supposed to be supported at this time");
            }
            catch (HsqlDataSourceException)
            {
            }
            //testSubject.GetNextAsLiteralValue(HsqlProviderType.LongVarBinary);
            //testSubject.GetNextAsLiteralValue(HsqlProviderType.LongVarChar);
            //testSubject.GetNextAsLiteralValue(HsqlProviderType.Null);
            //testSubject.GetNextAsLiteralValue(HsqlProviderType.Numeric);
            //testSubject.GetNextAsLiteralValue(HsqlProviderType.Object);
            //testSubject.GetNextAsLiteralValue(HsqlProviderType.Real);
            try
            {
                testSubject.GetNextAsLiteralValue(HsqlProviderType.Ref);

                Assert.Fail("SQL REF literal tokens are not supposed to be supported");
            }
            catch (HsqlDataSourceException)
            {
            }
            //testSubject.GetNextAsLiteralValue(HsqlProviderType.SmallInt);
            try
            {
                testSubject.GetNextAsLiteralValue(HsqlProviderType.Struct);

                Assert.Fail("SQL STRUCT literal tokens are not supposed to be supported");
            }
            catch (HsqlDataSourceException)
            {
            }
            //testSubject.GetNextAsLiteralValue(HsqlProviderType.Time);
            //testSubject.GetNextAsLiteralValue(HsqlProviderType.TimeStamp);
            //testSubject.GetNextAsLiteralValue(HsqlProviderType.TinyInt);
            //testSubject.GetNextAsLiteralValue(HsqlProviderType.VarBinary);
            //testSubject.GetNextAsLiteralValue(HsqlProviderType.VarChar);
            try
            {
                testSubject.GetNextAsLiteralValue(HsqlProviderType.Xml);

                Assert.Fail("SQL XML literal tokens are not supposed to be supported at this time");
            }
            catch (HsqlDataSourceException)
            {
            }
        }
        
        [Test, OfMember("GetNextAsName")]
        public void GetNextAsName()
        {
            Tokenizer testSubject = new Tokenizer("CREATE TABLE \"PUBLIC\".\"Foo \"\"BarBaz\"\"\"");

            testSubject.GetThis("CREATE");
            testSubject.GetThis("TABLE");

            string expectedChainFirst = "PUBLIC";
            string expected = "Foo \"BarBaz\"";
            string actualChainFirst = testSubject.IdentifierChainFirst;
            string actual = testSubject.GetNextAsName();

            Assert.AreEqual(expectedChainFirst, actualChainFirst, "schema qualifier" );
            Assert.AreEqual(expected, actual, "object name"); 
        }
        
        [Test, OfMember("GetNextAsSimpleName")]
        public void GetNextAsSimpleName()
        {
            // Create Constructor Parameters

            Tokenizer testSubject = new Tokenizer("SIMPLE");

            string expected = "SIMPLE";
            string actual = testSubject.GetNextAsSimpleName();

            Assert.AreEqual(HsqlProviderType.Null, testSubject.LiteralValueDataType, "literal value data type");
            Assert.AreEqual(false, testSubject.WasDelimitedIdentifier);
            Assert.AreEqual(false, testSubject.WasIdentifierChain);

            Assert.AreEqual(expected, actual);
        }
        
        [Test, OfMember("GetNextAsSimpleToken")]
        public void GetNextAsSimpleToken()
        {
            Assert.Fail("TODO"); 
        }
        
        [Test, OfMember("GetNextAsString")]
        public void GetNextAsString()
        {
            Assert.Fail("TODO"); 
        }
        
        [Test, OfMember("GetPart")]
        public void GetPart()
        {
            Assert.Fail("TODO");
        }
        
        [Test, OfMember("GetThis")]
        public void GetThis()
        {
            Assert.Fail("TODO");
        }
        
        [Test, OfMember("IdentiferChainLengthExceeded")]
        public void IdentiferChainLengthExceeded()
        {
            Assert.Fail("TODO");
        }
        
        [Test, OfMember("IllegalWaitState")]
        public void IllegalWaitState()
        {
            Assert.Fail("TODO"); 
        }
        
        [Test, OfMember("InvalidIdentifier")]
        public void InvalidIdentifier()
        {
            Assert.Fail("TODO"); 
        }
        
        [Test, OfMember("IsGetThis")]
        public void IsGetThis()
        {
            Assert.Fail("TODO"); 
        }
        
        [Test, OfMember("MatchFailed")]
        public void MatchFailed()
        {

            // Create Test Method Parameters
            object token = new object();
            object match = new object();

            try
            {
                throw Tokenizer.MatchFailed(token, match);
            }
            catch (HsqlDataSourceException hdse)
            {   
               Assert.AreEqual(org.hsqldb.Trace.UNEXPECTED_TOKEN, -hdse.ErrorCode);
               Assert.IsTrue(hdse.Message.Contains(org.hsqldb.Trace.getMessage(
                   org.hsqldb.Trace.TOKEN_REQUIRED)));
            }
        }
        
        [Test, OfMember("Reset")]
        public void Reset()
        {
            Assert.Fail("TODO"); 
        }
        
        [Test, OfMember("SetPartMarker")]
        public void SetPartMarker()
        {
            Assert.Fail("TODO");
        }
        
        [Test, OfMember("UnexpectedEndOfCommand")]
        public void UnexpectedEndOfCommand()
        {
            try
            {
                throw Tokenizer.UnexpectedEndOfCommand();
            }
            catch (HsqlDataSourceException hdse)
            {
                Assert.AreEqual(org.hsqldb.Trace.UNEXPECTED_END_OF_COMMAND, -hdse.ErrorCode);
            }
        }
        
        [Test, OfMember("UnexpectedToken")]
        public void UnexpectedToken()
        {
            try
            {
                throw Tokenizer.UnexpectedToken(42L);
            }
            catch (HsqlDataSourceException hdse)
            {
                Assert.AreEqual(org.hsqldb.Trace.UNEXPECTED_TOKEN, -hdse.ErrorCode);
            }  
        }
        
        [Test, OfMember("WasThis")]
        public void WasThis()
        {
            Tokenizer testSubject = new Tokenizer("foo bar baz");

            testSubject.GetThis("foo");
            testSubject.GetThis("bar");

            bool expected = true;
            bool actual = testSubject.WasThis("bar");

            Assert.AreEqual(expected, actual);
        }
        
        [Test, OfMember("WrongDataType")]
        public void WrongDataType()
        {
            try
            {
                throw Tokenizer.WrongDataType(HsqlProviderType.JavaObject);
            }
            catch (HsqlDataSourceException hdse)
            {
                Assert.AreEqual(org.hsqldb.Trace.WRONG_DATA_TYPE, -hdse.ErrorCode);
                Assert.IsTrue(hdse.Message.Contains("JAVA_OBJECT"));
            } 
        }
    }
}
