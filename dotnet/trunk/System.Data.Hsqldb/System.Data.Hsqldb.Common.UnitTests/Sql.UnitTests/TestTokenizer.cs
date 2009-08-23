#region Using
using System;
using System.Data.Hsqldb.Common;
using System.Data.Hsqldb.Common.Enumeration;
using System.Data.Hsqldb.Common.Sql;
using System.Data.Hsqldb.TestCoverage;
using NUnit.Framework;
using System.Text;
using System.Collections.Generic;

using HsqlBinary = org.hsqldb.types.Binary;
using HsqlStringConverter = org.hsqldb.lib.StringConverter;
#endregion

namespace System.Data.Hsqldb.Common.Sql.UnitTests
{
    [TestFixture, ForSubject(typeof(Tokenizer))]
    public class TestTokenizer
    {
        [Test, OfMember("EnforceTwoPartIdentifierChain")]
        public void EnforceTwoPartIdentifierChain()
        {
            Tokenizer testSubject = new Tokenizer();

            Assert.AreEqual(false, testSubject.EnforceTwoPartIdentifierChain);

            testSubject.EnforceTwoPartIdentifierChain = true;

            Assert.AreEqual(true, testSubject.EnforceTwoPartIdentifierChain);

            testSubject.Reset("foo.bar.baz");

            try
            {
                testSubject.GetNextAsName();
            }
            catch (HsqlDataSourceException hdse)
            {
                Assert.AreEqual(org.hsqldb.Trace.THREE_PART_IDENTIFIER, -hdse.ErrorCode);
            }

            testSubject.EnforceTwoPartIdentifierChain = false;

            testSubject.Reset("foo.\"bar\".baz.null.true.false");

            testSubject.GetNextAsName();

            Token[] expected = new Token[]
            {
                new Token("FOO",SqlTokenType.Name),
                new Token("bar",SqlTokenType.DelimitedIdentifier),
                new Token("BAZ",SqlTokenType.Name),
                new Token("NULL",SqlTokenType.Name),
                new Token("TRUE",SqlTokenType.Name),
                new Token("FALSE",SqlTokenType.Name),
            };

            int i = 0;

            foreach (Token token in testSubject.IdentifierChain)
            {
                Console.WriteLine(token);
                Console.WriteLine(expected[i]);
                Assert.AreEqual(expected[i], token);

                i++;
            }
        }

        [Test, OfMember("GetNextAsBigint")]
        public void GetNextAsBigint()
        {
            Tokenizer testSubject = new Tokenizer(long.MinValue.ToString());

            long expected = long.MinValue;
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

        class NextAsLiteralValueTestParameters
        {
            public NextAsLiteralValueTestParameters(
                string chars, 
                HsqlProviderType providerType, 
                object value, 
                int? errorCode, 
                string errorMessage)
            {
                this.Chars = chars;
                this.ProviderType = providerType;
                this.Value = value;
                this.ErrorCode = errorCode;
                this.ErrorMessage = errorMessage;
            }

            public readonly string Chars;
            public readonly HsqlProviderType ProviderType;
            public readonly object Value;
            public readonly int? ErrorCode;
            public readonly string ErrorMessage;

            public override string ToString()
            {
                StringBuilder sb = new StringBuilder(base.ToString());

                sb.Append("[Chars: \"" + this.Chars + "\"");
                sb.Append(", ProviderType: " + this.ProviderType);
                sb.Append(", Value: " + (this.Value == null ? "<NULL>" : this.Value));
                sb.Append(", ErrorCode: " + (this.ErrorCode == null ? "<NULL>" : this.ErrorCode.ToString()));
                sb.Append(", ErrorMessage: " + (this.ErrorMessage == null ? "<NULL>" : "\"" + this.ErrorMessage + "\""));
                sb.Append("]");

                return sb.ToString();
            }
        }

        [Test, OfMember("GetNextAsLiteralValue")]
        public void GetNextAsLiteralValue()
        {
            // Create Constructor Parameters

            Tokenizer testSubject = new Tokenizer();

            List<NextAsLiteralValueTestParameters> testParameters = 
                new List<NextAsLiteralValueTestParameters>();

            // Array Literal

            testParameters.Add(new NextAsLiteralValueTestParameters(
                "foo",
                HsqlProviderType.Array,
                null,
                org.hsqldb.Trace.UNEXPECTED_TOKEN,
                "SQL ARRAY literal tokens are not supposed to be supported"));

            // BigInt Literal

            testParameters.Add(new NextAsLiteralValueTestParameters(
                "-1",
                HsqlProviderType.BigInt,
                null,
                org.hsqldb.Trace.UNEXPECTED_TOKEN,
                "Atomic retrieval of a negative BIGINT literal is not supposed to be supported."));

            testParameters.Add(new NextAsLiteralValueTestParameters(
                "0",
                HsqlProviderType.BigInt,
                new java.lang.Long(0),
                null,
                null));

            testParameters.Add(new NextAsLiteralValueTestParameters(
                "1",
                HsqlProviderType.BigInt,
                new java.lang.Long(1),
                null,
                null));

            testParameters.Add(new NextAsLiteralValueTestParameters(
                long.MaxValue.ToString(),
                HsqlProviderType.BigInt,
                new java.lang.Long(long.MaxValue),
                null,
                null));

            testParameters.Add(new NextAsLiteralValueTestParameters(
                long.MinValue.ToString(),
                HsqlProviderType.BigInt,
                null,
                org.hsqldb.Trace.UNEXPECTED_TOKEN,
                "Atomic retrieval of a negative BIGINT literal is not supposed to be supported."));

            // Binary Literal

            testParameters.Add(new NextAsLiteralValueTestParameters(
                "/* a binary literal value */ 'AFD14E7B9F82' ", 
                HsqlProviderType.Binary,
                new org.hsqldb.types.Binary(HsqlStringConverter.hexToByte("AFD14E7B9F82"),false),
                null, 
                null));

            
            foreach (NextAsLiteralValueTestParameters parameters in 
                testParameters)
            {
                Console.WriteLine(parameters);

                testSubject.Reset(parameters.Chars);

                try
                {
                    object value = testSubject.GetNextAsLiteralValue(
                        parameters.ProviderType);

                    if (parameters.ErrorMessage != null) {
                        Assert.Fail(parameters.ErrorMessage);
                    }

                    System.Type expectedValueType = 
                        HsqlConvert.ToProviderSpecificDataType(
                        parameters.ProviderType);

                    Assert.IsAssignableFrom(expectedValueType, 
                        parameters.Value);
                    Assert.IsAssignableFrom(expectedValueType, 
                        value);
                    
                    Assert.AreEqual(parameters.Value, value);
                }
                catch(AssertionException) 
                {
                    throw;
                }
                catch(HsqlDataSourceException hdse) 
                {
                    Assert.AreEqual(parameters.ErrorCode, -hdse.ErrorCode);
                }
            }

            //testSubject.Reset("'AFD14E7B9F82'");

            //object bytes = testSubject.GetNextAsLiteralValue(HsqlProviderType.Binary);

            //Assert.IsInstanceOfType(typeof(org.hsqldb.types.Binary), bytes);

            //testSubject.Reset("'CAFEBABE'");

            //try
            //{
            //    testSubject.GetNextAsLiteralValue(HsqlProviderType.Blob);

            //    Assert.Fail("SQL BLOB literal tokens are not supposed to be supported at this time");
            //}
            //catch (HsqlDataSourceException)
            //{
            //}

            //testSubject.Reset("TRUE");

            //testSubject.GetNextAsLiteralValue(HsqlProviderType.Boolean);

            //testSubject.Reset("FALSE");

            //testSubject.GetNextAsLiteralValue(HsqlProviderType.Boolean);

            //testSubject.Reset("NULL");

            //testSubject.GetNextAsLiteralValue(HsqlProviderType.Boolean);

            ////testSubject.GetNextAsLiteralValue(HsqlProviderType.Char);
            //try
            //{
            //    testSubject.GetNextAsLiteralValue(HsqlProviderType.Clob);

            //    Assert.Fail("SQL CLOB literal tokens are not supposed to be supported at this time");
            //}
            //catch (HsqlDataSourceException)
            //{
            //}
            //try
            //{
            //    testSubject.GetNextAsLiteralValue(HsqlProviderType.DataLink);

            //    Assert.Fail("SQL DATALINK literal tokens are not supposed to be supported at this time");
            //}
            //catch (HsqlDataSourceException)
            //{
            //}
            ////testSubject.GetNextAsLiteralValue(HsqlProviderType.Date);
            ////testSubject.GetNextAsLiteralValue(HsqlProviderType.Decimal);
            //try
            //{
            //    testSubject.GetNextAsLiteralValue(HsqlProviderType.Distinct);

            //    Assert.Fail("SQL DISTINCT literal tokens are not supposed to be supported");
            //}
            //catch (HsqlDataSourceException)
            //{
            //}
            ////testSubject.GetNextAsLiteralValue(HsqlProviderType.Double);
            ////testSubject.GetNextAsLiteralValue(HsqlProviderType.Float);
            ////testSubject.GetNextAsLiteralValue(HsqlProviderType.Integer);
            //try
            //{
            //    testSubject.GetNextAsLiteralValue(HsqlProviderType.JavaObject);

            //    Assert.Fail("SQL JAVA_OBJECT literal tokens are not supposed to be supported at this time");
            //}
            //catch (HsqlDataSourceException)
            //{
            //}
            ////testSubject.GetNextAsLiteralValue(HsqlProviderType.LongVarBinary);
            ////testSubject.GetNextAsLiteralValue(HsqlProviderType.LongVarChar);
            ////testSubject.GetNextAsLiteralValue(HsqlProviderType.Null);
            ////testSubject.GetNextAsLiteralValue(HsqlProviderType.Numeric);
            ////testSubject.GetNextAsLiteralValue(HsqlProviderType.Object);
            ////testSubject.GetNextAsLiteralValue(HsqlProviderType.Real);
            //try
            //{
            //    testSubject.GetNextAsLiteralValue(HsqlProviderType.Ref);

            //    Assert.Fail("SQL REF literal tokens are not supposed to be supported");
            //}
            //catch (HsqlDataSourceException)
            //{
            //}
            ////testSubject.GetNextAsLiteralValue(HsqlProviderType.SmallInt);
            //try
            //{
            //    testSubject.GetNextAsLiteralValue(HsqlProviderType.Struct);

            //    Assert.Fail("SQL STRUCT literal tokens are not supposed to be supported");
            //}
            //catch (HsqlDataSourceException)
            //{
            //}
            ////testSubject.GetNextAsLiteralValue(HsqlProviderType.Time);
            ////testSubject.GetNextAsLiteralValue(HsqlProviderType.TimeStamp);
            ////testSubject.GetNextAsLiteralValue(HsqlProviderType.TinyInt);
            ////testSubject.GetNextAsLiteralValue(HsqlProviderType.VarBinary);
            ////testSubject.GetNextAsLiteralValue(HsqlProviderType.VarChar);
            //try
            //{
            //    testSubject.GetNextAsLiteralValue(HsqlProviderType.Xml);

            //    Assert.Fail("SQL XML literal tokens are not supposed to be supported at this time");
            //}
            //catch (HsqlDataSourceException)
            //{
            //}
        }
        
        [Test, OfMember("GetNextAsName")]
        public void GetNextAsName()
        {
            Tokenizer testSubject = new Tokenizer("CREATE TABLE \"PUBLIC\".\"Foo \"\"BarBaz\"\"\"");

            testSubject.GetThis("CREATE");
            testSubject.GetThis("TABLE");
            
            string expectedSubjectPart = "Foo \"BarBaz\"";
            string expectedQualifierPart = "PUBLIC";
            string actualSubjectPart = testSubject.GetNextAsName();
            string actualQualifierPart = testSubject.IdentifierChainPredecessor;

            Assert.AreEqual(expectedQualifierPart, actualQualifierPart, "schema qualifier" );
            Assert.AreEqual(expectedSubjectPart, actualSubjectPart, "object name"); 
        }
        
        [Test, OfMember("GetNextAsSimpleName")]
        public void GetNextAsSimpleName()
        {
            // Create Constructor Parameters

            Tokenizer testSubject = new Tokenizer("SIMPLE");

            string expected = "SIMPLE";
            string actual = testSubject.GetNextAsSimpleName();

            Assert.AreEqual(HsqlProviderType.Null, testSubject.LiteralValueDataType, 
                "literal value data type");
            Assert.AreEqual(false, testSubject.WasDelimitedIdentifier);
            Assert.AreEqual(false, testSubject.WasIdentifierChain);

            Assert.AreEqual(expected, actual);
        }
        
        [Test, OfMember("GetNextAsSimpleToken")]
        public void GetNextAsSimpleToken()
        {
            Tokenizer testSubject = new Tokenizer("FOO");

            string actual = testSubject.GetNextAsSimpleToken();

            Console.WriteLine(actual);
        }
        
        [Test, OfMember("GetNextAsString")]
        public void GetNextAsString()
        {
            Tokenizer testSubject = new Tokenizer("FOO.BAR");

            string actual = testSubject.GetNextAsString();

            Assert.AreEqual("BAR", actual);
            Assert.AreEqual(true, testSubject.WasIdentifierChain);
            Assert.AreEqual("FOO", testSubject.IdentifierChainPredecessor);
        }
        
        [Test, OfMember("GetPart")]
        public void GetPart()
        {
            Tokenizer testSubject = new Tokenizer("FOO.BAR AS BAR.FOO");

            string actual = testSubject.GetPart(0, "FOO.BAR AS BAR.FOO".Length);

            Assert.AreEqual("FOO.BAR AS BAR.FOO", actual);

            testSubject.SetPartMarker();

            Assert.AreEqual(0, testSubject.PartMarker);
            Assert.AreEqual(0, testSubject.Position);
            
            actual = testSubject.GetNextAsString();

            Assert.AreEqual("BAR", actual);
            Assert.AreEqual(SqlTokenType.IdentifierChain, testSubject.TokenType);
            Assert.That(!testSubject.WasIdentifierChainPredecessorDelimited);
            Assert.AreEqual("FOO", testSubject.IdentifierChainPredecessor);

            Assert.AreEqual("FOO.BAR ".IndexOf(' '), testSubject.Position);

            actual = testSubject.GetPart(testSubject.PartMarker, testSubject.Position);

            Assert.AreEqual("FOO.BAR", actual);

            testSubject.SetPartMarker();

            actual = testSubject.GetNextAsString();

            Assert.AreEqual(Token.ValueFor.AS, actual);
            Assert.AreEqual("FOO.BAR_AS ".IndexOf(' '), testSubject.Position);

            actual = testSubject.GetPart(testSubject.PartMarker, testSubject.Position);

            Assert.AreEqual(" AS", actual);
        }
        
        [Test, OfMember("GetThis")]
        public void GetThis()
        {
            Tokenizer testSubject = new Tokenizer("create table test(id int, \"val\" varchar(12));");

            Assert.AreEqual(Token.ValueFor.CREATE, testSubject.GetThis(Token.ValueFor.CREATE));
            Assert.AreEqual(Token.ValueFor.TABLE, testSubject.GetThis(Token.ValueFor.TABLE));

            try
            {
                string actual = testSubject.GetThis("test");

                Assert.Fail("successful invocation of GetThis(string) with non-match value");
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
            }

            Assert.That(testSubject.WasThis("TEST"));
            Assert.That(testSubject.TokenType == SqlTokenType.Name);

            Assert.AreEqual(Token.ValueFor.OPENBRACKET, testSubject.GetThis(Token.ValueFor.OPENBRACKET));            
            Assert.AreEqual("ID", testSubject.GetThis("ID"));
            Assert.AreEqual(Token.ValueFor.INT, testSubject.GetThis(Token.ValueFor.INT));
            Assert.AreEqual(Token.ValueFor.COMMA, testSubject.GetThis(Token.ValueFor.COMMA));

            try
            {
                Assert.AreEqual("val", testSubject.GetThis("val"));
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
            }
            Assert.That(testSubject.WasDelimitedIdentifier);
            Assert.AreEqual("\"val\"", testSubject.NormalizedToken);

            Assert.AreEqual(Token.ValueFor.VARCHAR, testSubject.GetThis(Token.ValueFor.VARCHAR));
            Assert.AreEqual(Token.ValueFor.OPENBRACKET, testSubject.GetThis(Token.ValueFor.OPENBRACKET));
            Assert.AreEqual("12", testSubject.GetThis("12"));
            Assert.That(testSubject.TokenType == SqlTokenType.IntegerLiteral);
            Assert.That(testSubject.LiteralValueDataType == HsqlProviderType.Integer);
            Assert.AreEqual(Token.ValueFor.CLOSEBRACKET, testSubject.GetThis(Token.ValueFor.CLOSEBRACKET));
            Assert.AreEqual(Token.ValueFor.CLOSEBRACKET, testSubject.GetThis(Token.ValueFor.CLOSEBRACKET));
            Assert.AreEqual(Token.ValueFor.SEMICOLON, testSubject.GetThis(Token.ValueFor.SEMICOLON));
        }

        [Test, OfMember("IdentiferChain")]
        public void IdentiferChain()
        {
            object[] input = new object[]
            {
                new object [] 
                {
                    "foo.bar.baz", 
                    new Token[]{
                        new Token("FOO",SqlTokenType.Name),
                        new Token("BAR",SqlTokenType.Name),
                        new Token("BAZ",SqlTokenType.Name)
                    }
                },
                new object [] 
                {
                    "\"foo\".bar.\"baz\"", 
                    new Token[]{
                        new Token("foo",SqlTokenType.DelimitedIdentifier),
                        new Token("BAR",SqlTokenType.Name),
                        new Token("baz",SqlTokenType.DelimitedIdentifier)
                    }
                },
                new object [] 
                {
                    "\"foo.bar\".baz", 
                    new Token[]{
                        new Token("foo.bar",SqlTokenType.DelimitedIdentifier),
                        new Token("BAZ",SqlTokenType.Name)
                    }
                }
            };

            foreach (object[] item in input)
            {
                string toTokenize = item[0] as string;
                Token[] expectedTokens = item[1] as Token[];

                Tokenizer testSubject = new Tokenizer(toTokenize);

                string result = testSubject.GetNextAsName();
                Token[] actualTokens = new List<Token>(testSubject.IdentifierChain).ToArray();

                Assert.AreEqual(expectedTokens.Length, actualTokens.Length);

                for (int i = 0; i < expectedTokens.Length; i++)
                {
                    Token expectedToken = expectedTokens[i];
                    Token actualToken = actualTokens[i];

                    Assert.AreEqual(expectedToken, actualToken);
                }

            }
        }

        [Test, OfMember("IdentiferChainPredecessor")]
        public void IdentiferChainPredecessor()
        {
            Tokenizer testSubject = new Tokenizer("foo.bar.baz");

            string token = testSubject.GetNextAsString();

            Assert.AreEqual("BAR", testSubject.IdentifierChainPredecessor);

            testSubject.Reset("bing.foo.\"bar\".baz");

            token = testSubject.GetNextAsString();

            Assert.AreEqual("bar", testSubject.IdentifierChainPredecessor);
        }
        
        [Test, OfMember("IdentiferChainLengthExceeded")]
        public void IdentiferChainLengthExceeded()
        {
            try
            {
                throw Tokenizer.IdentiferChainLengthExceeded();
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
                Assert.AreEqual(org.hsqldb.Trace.THREE_PART_IDENTIFIER, -((HsqlDataSourceException)ex).ErrorCode);
            }
            
            Tokenizer testSubject = new Tokenizer("foo.bar.baz");

            testSubject.EnforceTwoPartIdentifierChain = true;

            try
            {
                string name = testSubject.GetNextAsName();

                Assert.Fail("successful invocation of ReadToken() with greater than 2-part identifier token");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
                Assert.AreEqual(org.hsqldb.Trace.THREE_PART_IDENTIFIER, -((HsqlDataSourceException)ex).ErrorCode);
            }
        }
        
        [Test, OfMember("IllegalWaitState")]
        public void IllegalWaitState()
        {
            try
            {
                throw Tokenizer.IllegalWaitState();
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
                Assert.AreEqual(org.hsqldb.Trace.ASSERT_FAILED, -((HsqlDataSourceException)ex).ErrorCode);
            }

            Tokenizer testSubject = new Tokenizer("t1 t2 t3");

            testSubject.GetThis("T1");
            object rval;

            if (!testSubject.IsGetThis("T3"))
            {
                try
                {
                    rval = testSubject.IdentifierChainPredecessor;
                }
                catch (Exception ex)
                {
                    Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
                    Assert.AreEqual(org.hsqldb.Trace.ASSERT_FAILED, -((HsqlDataSourceException)ex).ErrorCode);
                }
            }
        }
        
        [Test, OfMember("InvalidIdentifier")]
        public void InvalidIdentifier()
        {
            try
            {
                throw Tokenizer.InvalidIdentifier("fuddle duddle");
            }
            catch (Exception ex)
            {
                Assert.IsInstanceOfType(typeof(HsqlDataSourceException), ex);
                Assert.AreEqual(org.hsqldb.Trace.INVALID_IDENTIFIER, -((HsqlDataSourceException)ex).ErrorCode);
            }
        }
        
        [Test, OfMember("IsGetThis")]
        public void IsGetThis()
        {
            Tokenizer testSubject = new Tokenizer("create table test(id int, \"val\" varchar(12));");

            if (testSubject.IsGetThis(Token.ValueFor.ALTER))
            {
                Assert.AreEqual(Token.ValueFor.CREATE, Token.ValueFor.ALTER);
            }
            else if (testSubject.IsGetThis(Token.ValueFor.CREATE)) 
            {
                if (testSubject.IsGetThis(Token.ValueFor.VIEW))
                {
                    Assert.AreEqual(Token.ValueFor.TABLE, Token.ValueFor.VIEW);
                }
                else if (testSubject.IsGetThis(Token.ValueFor.TABLE))
                {
                    string table_name = testSubject.GetNextAsName();
                }
                else
                {
                    Assert.AreNotEqual(Token.ValueFor.TABLE, testSubject.GetNextAsString());
                }
            }
            else if (testSubject.IsGetThis(Token.ValueFor.DROP))
            {
                Assert.AreEqual(Token.ValueFor.CREATE, Token.ValueFor.DROP);
            }
            else
            {
                Assert.AreNotEqual(Token.ValueFor.CREATE, testSubject.GetNextAsString());
            }
        }

        [Test, OfMember("LastPart")]
        public void LastPart()
        {
            Tokenizer testSubject = new Tokenizer("create table test(id int, \"val\" varchar(12));");

            Assert.AreEqual(Token.ValueFor.CREATE, testSubject.GetNextAsSimpleToken());
            Assert.AreEqual(6, testSubject.Position);
            Assert.AreEqual("create", testSubject.LastPart);

            Assert.AreEqual(Token.ValueFor.TABLE, testSubject.GetNextAsSimpleToken());
            Assert.AreEqual(12, testSubject.Position);
            Assert.AreEqual("create table", testSubject.LastPart);

            testSubject.PartMarker = testSubject.Position + 1;

            Assert.AreEqual("TEST", testSubject.GetNextAsSimpleName());
            Assert.AreEqual(17, testSubject.Position);
            Assert.AreEqual("test", testSubject.LastPart);

            Assert.AreEqual(Token.ValueFor.OPENBRACKET, testSubject.GetNextAsSimpleToken());
            Assert.AreEqual(18, testSubject.Position);
            Assert.AreEqual("test(", testSubject.LastPart);

            Assert.AreEqual("ID", testSubject.GetNextAsSimpleName());
            Assert.AreEqual(20, testSubject.Position);
            Assert.AreEqual("test(id", testSubject.LastPart);

            Assert.AreEqual(Token.ValueFor.INT, testSubject.GetNextAsSimpleToken());
            Assert.AreEqual(24, testSubject.Position);
            Assert.AreEqual("test(id int", testSubject.LastPart);

            testSubject.SetPartMarker();

            Assert.AreEqual(Token.ValueFor.COMMA, testSubject.GetNextAsSimpleToken());
            Assert.AreEqual(25, testSubject.Position);
            Assert.AreEqual(Token.ValueFor.COMMA, testSubject.LastPart);

            Assert.AreEqual("val", testSubject.GetNextAsName());
            Assert.AreEqual(SqlTokenType.DelimitedIdentifier, testSubject.TokenType);
            Assert.AreEqual(31,testSubject.Position);
            Assert.AreEqual(", \"val\"", testSubject.LastPart);

            Assert.AreEqual(Token.ValueFor.VARCHAR, testSubject.GetNextAsSimpleToken());
            Assert.AreEqual(39, testSubject.Position);
            Assert.AreEqual(", \"val\" varchar", testSubject.LastPart);

            Assert.AreEqual(Token.ValueFor.OPENBRACKET, testSubject.GetThis(Token.ValueFor.OPENBRACKET));
            Assert.AreEqual(40, testSubject.Position);
            Assert.AreEqual(", \"val\" varchar(", testSubject.LastPart);

            Assert.AreEqual(12, testSubject.GetNextAsInt());
            Assert.AreEqual(42, testSubject.Position);
            Assert.AreEqual(", \"val\" varchar(12", testSubject.LastPart);

            Assert.AreEqual(Token.ValueFor.CLOSEBRACKET, testSubject.GetThis(Token.ValueFor.CLOSEBRACKET));
            Assert.AreEqual(43, testSubject.Position);
            Assert.AreEqual(", \"val\" varchar(12)", testSubject.LastPart);

            testSubject.SetPartMarker();

            Assert.AreEqual(Token.ValueFor.CLOSEBRACKET, testSubject.GetThis(Token.ValueFor.CLOSEBRACKET));
            Assert.AreEqual(44, testSubject.Position);
            Assert.AreEqual(Token.ValueFor.CLOSEBRACKET, testSubject.LastPart);

            Assert.AreEqual(Token.ValueFor.SEMICOLON, testSubject.GetThis(Token.ValueFor.SEMICOLON));
            Assert.AreEqual(45, testSubject.Position);
            Assert.AreEqual(");", testSubject.LastPart);

            Assert.AreEqual(string.Empty, testSubject.GetNextAsString());

            Assert.AreEqual(45, testSubject.Position);
            Assert.AreEqual(");", testSubject.LastPart);

            testSubject.SetPartMarker();

            Assert.AreEqual(string.Empty, testSubject.LastPart);
        }

        [Test, OfMember("Length")]
        public void Length()
        {
            StringBuilder sb = new StringBuilder();
            Tokenizer testSubject = new Tokenizer();

            for (int i = 0; i < 30; i++)
            {
                testSubject.Reset(sb.ToString());

                Assert.AreEqual(i, testSubject.Length);

                sb.Append(' ');
            }
        }

        [Test, OfMember("LiteralValueDataType")]
        public void LiteralValueDataType()
        {
            Tokenizer testSubject = new Tokenizer("1 1.0 1.0E2 '2009-01-01' '12:00:01' '2009-01-01 12:00:01' '2009-01-01 12:00:01.123456789'");

            testSubject.GetNextAsString();

            Assert.AreEqual(SqlTokenType.IntegerLiteral, testSubject.TokenType);
            Assert.AreEqual(HsqlProviderType.Integer, testSubject.LiteralValueDataType);

            testSubject.GetNextAsString();

            Assert.AreEqual(SqlTokenType.DecimalLiteral, testSubject.TokenType);
            Assert.AreEqual(HsqlProviderType.Decimal, testSubject.LiteralValueDataType);

            testSubject.GetNextAsString();

            Assert.AreEqual(SqlTokenType.FloatLiteral, testSubject.TokenType);
            Assert.AreEqual(HsqlProviderType.Double, testSubject.LiteralValueDataType);

            testSubject.GetNextAsString();

            Assert.AreEqual(SqlTokenType.DateLiteral, testSubject.TokenType);
            Assert.AreEqual(HsqlProviderType.Date, testSubject.LiteralValueDataType);

            testSubject.GetNextAsString();

            Assert.AreEqual(SqlTokenType.TimeLiteral, testSubject.TokenType);
            Assert.AreEqual(HsqlProviderType.Time, testSubject.LiteralValueDataType);

            testSubject.GetNextAsString();

            Assert.AreEqual(SqlTokenType.TimestampLiteral, testSubject.TokenType);
            Assert.AreEqual(HsqlProviderType.TimeStamp, testSubject.LiteralValueDataType);

            testSubject.GetNextAsString();

            Assert.AreEqual(SqlTokenType.TimestampLiteral, testSubject.TokenType);
            Assert.AreEqual(HsqlProviderType.TimeStamp, testSubject.LiteralValueDataType);
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
                // TODO
               //Assert.IsTrue(hdse.Message.Contains(org.hsqldb.Trace.getMessage(
               //    org.hsqldb.Trace.TOKEN_REQUIRED)));
            }

            Tokenizer testSubject = new Tokenizer("notwhatismatched");

            try
            {
                testSubject.GetThis("whatismatched");

                Assert.Fail("Unexpected successful match of 'whatismatched' to 'notwhatismatched'");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (HsqlDataSourceException ex)
            {
                Assert.AreEqual(org.hsqldb.Trace.UNEXPECTED_TOKEN, -ex.ErrorCode);
            }
        }

        [Test, OfMember("NormalizedToken")]
        public void NormalizedToken()
        {
            Tokenizer testSubject = new Tokenizer("select 'Let''s Have a nice day!' from DUAL");

            testSubject.GetNextAsString();

            Assert.AreEqual("SELECT", testSubject.NormalizedToken);

            testSubject.GetNextAsString();

            Assert.AreEqual("'Let''s Have a nice day!'", testSubject.NormalizedToken);

        }

        [Test, OfMember("ParameterName")]
        public void ParameterName()
        {
            Assert.Fail("TODO");
        }

        [Test, OfMember("PartMarker")]
        public void PartMarker()
        {
            Assert.Fail("TODO");
        }

        [Test, OfMember("Position")]
        public void Position()
        {
            Assert.Fail("TODO");
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

        [Test, OfMember("TokenType")]
        public void TokenTypeTest()
        {
            Tokenizer testSubject = new Tokenizer();

            Assert.AreEqual(SqlTokenType.None, testSubject.TokenType);

            object[][] values = new object[][]
            {
            new object[] {"2147483649", SqlTokenType.BigIntLiteral},
            new object[] {Token.ValueFor.TRUE, SqlTokenType.BooleanLiteral},
            new object[] {"'2009-01-01'", SqlTokenType.DateLiteral},
            new object[] {"123.0123", SqlTokenType.DecimalLiteral},
            new object[] {"\"foo\"", SqlTokenType.DelimitedIdentifier},
            new object[] {"123.1e-23", SqlTokenType.FloatLiteral},
            new object[] {"foo.bar", SqlTokenType.IdentifierChain},
            new object[] {int.MaxValue.ToString(), SqlTokenType.IntegerLiteral},
            new object[] {"column_one", SqlTokenType.Name},
            new object[] {"@MyNamedParameter", SqlTokenType.NamedParameter},            
            new object[] {"?", SqlTokenType.ParameterMarker},
            new object[] {"<", SqlTokenType.Special},
            new object[] {"'''foo'' they said.'", SqlTokenType.StringLiteral},
            new object[] {"'12:23:02'", SqlTokenType.TimeLiteral},
            new object[] {"'2009-01-01 12:23:02.123456789'", SqlTokenType.TimestampLiteral}
            };

            foreach (object[] row in values)
            {
                testSubject.Reset(row[0].ToString());

                testSubject.GetNextAsString();

                Assert.AreEqual((SqlTokenType)row[1], testSubject.TokenType);
            }
        }

        [Test, OfMember("TokenTypeName")]
        public void TokenTypeName()
        {
            Tokenizer testSubject = new Tokenizer();

            Assert.AreEqual("None", testSubject.TokenTypeName);

            object[][] values = new object[][]
            {
            new object[] {"2147483649", "BigInt"},
            new object[] {Token.ValueFor.TRUE, "Boolean"},
            new object[] {"'2009-01-01'", "Date"},
            new object[] {"123.0123", "Decimal"},
            new object[] {"\"foo\"", "Delimited Identifier"},
            new object[] {"123.1e-23", "Float"},
            new object[] {"foo.bar", "Identifier Chain"},
            new object[] {int.MaxValue.ToString(), "Integer"},
            new object[] {"column_one", "Name"},
            new object[] {"@MyNamedParameter", "Named Parameter"},            
            new object[] {"?", "Parameter Marker"},
            new object[] {"<", "Special"},
            new object[] {"'''foo'' they said.'","String"},
            new object[] {"'12:23:02'", "Time"},
            new object[] {"'2009-01-01 12:23:02.123456789'", "Timestamp"}
            };

            foreach (object[] row in values)
            {
                testSubject.Reset(row[0].ToString());

                testSubject.GetNextAsString();

                Assert.AreEqual((string)row[1], testSubject.TokenTypeName);
            }
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

        [Test, OfMember("WasDelimitedIdentifier")]
        public void WasDelimitedIdentifier()
        {
            Tokenizer testSubject = new Tokenizer("+ join foo \"foo\" bar.\"foo\"");

            testSubject.GetThis(Token.ValueFor.PLUS);

            Assert.AreEqual(false, testSubject.WasDelimitedIdentifier);

            Assert.AreEqual(Token.ValueFor.JOIN, testSubject.GetNextAsSimpleToken());

            Assert.AreEqual(false, testSubject.WasDelimitedIdentifier);

            Assert.AreEqual("FOO", testSubject.GetNextAsName());

            Assert.AreEqual(false, testSubject.WasDelimitedIdentifier);

            Assert.AreEqual("foo", testSubject.GetNextAsName());

            Assert.AreEqual(true, testSubject.WasDelimitedIdentifier);
            Assert.AreEqual(false, testSubject.WasIdentifierChain);
            Assert.AreEqual(null, testSubject.IdentifierChainPredecessor);

            Assert.AreEqual("foo", testSubject.GetNextAsName());

            Assert.AreEqual(true, testSubject.WasDelimitedIdentifier);
            Assert.AreEqual(true, testSubject.WasIdentifierChain);
            Assert.AreEqual(false, testSubject.WasIdentifierChainPredecessorDelimited);
            Assert.AreEqual("BAR", testSubject.IdentifierChainPredecessor);
        }

        [Test, OfMember("WasIdentifierChain")]
        public void WasIdentifierChain()
        {
            Tokenizer testSubject = new Tokenizer("foo foo.bar \"foo\".bar foo.\"bar\" \"foo\".\"bar\" \"foo\".\"bar\"");

            testSubject.GetNextAsName();

            Assert.AreEqual(false, testSubject.WasIdentifierChain);

            string tokenValue;

            while (string.Empty != (tokenValue = testSubject.GetNextAsName()))
            {
                Assert.AreEqual(true, testSubject.WasIdentifierChain);
            }
        }

        [Test, OfMember("WasIdentifierChainPredecessorDelimited")]
        public void WasIdentifierChainPredecessorDelimited()
        {
            Tokenizer testSubject = new Tokenizer("foo foo.bar \"foo\".bar");

            testSubject.GetNextAsName();

            Assert.AreEqual(false, testSubject.WasIdentifierChain);
            Assert.AreEqual(false, testSubject.WasIdentifierChainPredecessorDelimited);

            testSubject.GetNextAsName();

            Assert.AreEqual(true, testSubject.WasIdentifierChain);
            Assert.AreEqual(false, testSubject.WasIdentifierChainPredecessorDelimited);

            testSubject.GetNextAsName();

            Assert.AreEqual(true, testSubject.WasIdentifierChain);
            Assert.AreEqual(true, testSubject.WasIdentifierChainPredecessorDelimited);
        }

        [Test, OfMember("WasNamedParameter")]
        public void WasNamedParameter()
        {
            Tokenizer testSubject = new Tokenizer("update test set id = @IdParam, val = :ValParam");

            Assert.AreEqual(false, testSubject.WasNamedParameter);
            Assert.AreEqual(string.Empty, testSubject.ParameterName);
            Assert.AreEqual(' ', testSubject.ParameterNamePrefix);

            testSubject.GetThis(Token.ValueFor.UPDATE);

            Assert.AreEqual(false, testSubject.WasNamedParameter);
            Assert.AreEqual(string.Empty, testSubject.ParameterName);
            Assert.AreEqual(' ', testSubject.ParameterNamePrefix);

            testSubject.GetThis("TEST");

            Assert.AreEqual(false, testSubject.WasNamedParameter);
            Assert.AreEqual(null, testSubject.ParameterName);
            Assert.AreEqual(' ', testSubject.ParameterNamePrefix);

            testSubject.GetThis(Token.ValueFor.SET);

            Assert.AreEqual(false, testSubject.WasNamedParameter);
            Assert.AreEqual(null, testSubject.ParameterName);
            Assert.AreEqual(' ', testSubject.ParameterNamePrefix);

            testSubject.GetThis("ID");

            Assert.AreEqual(false, testSubject.WasNamedParameter);
            Assert.AreEqual(null, testSubject.ParameterName);
            Assert.AreEqual(' ', testSubject.ParameterNamePrefix);

            testSubject.GetThis(Token.ValueFor.EQUALS);

            Assert.AreEqual(false, testSubject.WasNamedParameter);
            Assert.AreEqual(null, testSubject.ParameterName);
            Assert.AreEqual(' ', testSubject.ParameterNamePrefix);

            testSubject.GetNextAsString();

            Assert.AreEqual(true, testSubject.WasNamedParameter);
            Assert.AreEqual("IdParam", testSubject.ParameterName);
            Assert.AreEqual('@', testSubject.ParameterNamePrefix);

            testSubject.GetThis(Token.ValueFor.COMMA);

            Assert.AreEqual(false, testSubject.WasNamedParameter);
            Assert.AreEqual(null, testSubject.ParameterName);
            Assert.AreEqual(' ', testSubject.ParameterNamePrefix);

            testSubject.GetThis("VAL");

            Assert.AreEqual(false, testSubject.WasNamedParameter);
            Assert.AreEqual(null, testSubject.ParameterName);
            Assert.AreEqual(' ', testSubject.ParameterNamePrefix);

            testSubject.GetThis(Token.ValueFor.EQUALS);

            Assert.AreEqual(false, testSubject.WasNamedParameter);
            Assert.AreEqual(null, testSubject.ParameterName);
            Assert.AreEqual(' ', testSubject.ParameterNamePrefix);

            testSubject.GetNextAsString();

            Assert.AreEqual(true, testSubject.WasNamedParameter);
            Assert.AreEqual("ValParam", testSubject.ParameterName);
            Assert.AreEqual(':', testSubject.ParameterNamePrefix);
        }

        [Test, OfMember("WasParameterMarker")]
        public void WasParameterMarker()
        {
            Tokenizer testSubject = new Tokenizer("update test set id = ?, val = ?");

            testSubject.GetThis(Token.ValueFor.UPDATE);

            Assert.AreEqual(false, testSubject.WasParameterMarker);

            testSubject.GetThis("TEST");

            Assert.AreEqual(false, testSubject.WasParameterMarker);

            testSubject.GetThis(Token.ValueFor.SET);

            Assert.AreEqual(false, testSubject.WasParameterMarker);

            testSubject.GetThis("ID");

            Assert.AreEqual(false, testSubject.WasParameterMarker);

            testSubject.GetThis(Token.ValueFor.EQUALS);

            Assert.AreEqual(false, testSubject.WasParameterMarker);

            testSubject.GetThis(Token.ValueFor.QUESTION);

            Assert.AreEqual(true, testSubject.WasParameterMarker);

            testSubject.GetThis(Token.ValueFor.COMMA);

            Assert.AreEqual(false, testSubject.WasParameterMarker);

            testSubject.GetThis("VAL");

            Assert.AreEqual(false, testSubject.WasParameterMarker);

            testSubject.GetThis(Token.ValueFor.EQUALS);

            Assert.AreEqual(false, testSubject.WasParameterMarker);

            testSubject.GetThis(Token.ValueFor.QUESTION);

            Assert.AreEqual(true, testSubject.WasParameterMarker);
        }
        
        [Test, OfMember("WasThis")]
        public void WasThis()
        {
            Tokenizer testSubject = new Tokenizer("foo bar \"baz\" foo.bar");

            testSubject.GetThis("FOO");
            testSubject.GetThis("BAR");

            Assert.AreEqual(true, testSubject.WasThis("BAR"));
            Assert.AreEqual(false, testSubject.WasThis("bar"));
            Assert.AreEqual(false, testSubject.WasThis(" BAR "));
            Assert.AreEqual(false, testSubject.WasThis("NOTBAR"));

            string name = testSubject.GetNextAsName();

            // not for use with delimited identifier.

            Assert.AreEqual(false, testSubject.WasThis("baz")); 
            Assert.AreEqual(false, testSubject.WasThis("BAZ"));
            Assert.AreEqual(false, testSubject.WasThis(" baz "));
            Assert.AreEqual(false, testSubject.WasThis("notbaz"));

            string s = testSubject.GetNextAsString();

            // not for use with identifier chain.

            Assert.AreEqual(false, testSubject.WasThis("bar"));
            Assert.AreEqual(false, testSubject.WasThis("BAR"));
            Assert.AreEqual(false, testSubject.WasThis(" bar "));
            Assert.AreEqual(false, testSubject.WasThis("notbar"));
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
                // TODO
                //Assert.IsTrue(hdse.Message.Contains("JAVA_OBJECT"), "message contains JAVA_OBJECT: " + hdse.Message);
            }

            Tokenizer testSubject = new Tokenizer("'foo'");

            try
            {
                testSubject.GetNextAsBigint();

                Assert.Fail("Failed to throw wrong data type exception getting 'foo' as BigInt");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (HsqlDataSourceException hdse)
            {
                Assert.AreEqual(org.hsqldb.Trace.WRONG_DATA_TYPE, -hdse.ErrorCode);
            }

            testSubject = new Tokenizer("1.0");

            try
            {
                testSubject.GetNextAsBigint();

                Assert.Fail("Failed to throw wrong data type exception getting 1.0 as BigInt");
            }
            catch (AssertionException)
            {
                throw;
            }
            catch (HsqlDataSourceException hdse)
            {
                Assert.AreEqual(org.hsqldb.Trace.WRONG_DATA_TYPE, -hdse.ErrorCode);
            }

            testSubject = new Tokenizer(long.MinValue.ToString());

            try
            {
                testSubject.GetNextAsBigint();
            }
            catch (HsqlDataSourceException hdse)
            {
                if (org.hsqldb.Trace.WRONG_DATA_TYPE == -hdse.ErrorCode)
                {
                    Assert.Fail("");
                }
            }
        }
    }
}
