using System;
using NUnit.Framework;
using System.Data.Hsqldb.TestCoverage;
using System.Data.Hsqldb.Common.Enumeration;
using org.hsqldb;
using System.Collections.Generic;
using HsqlValuePool = org.hsqldb.store.ValuePool;
using JavaBoolean = java.lang.Boolean;
using JavaDecimal = java.math.BigDecimal;
using JavaDouble = java.lang.Double;
using System.Text;

namespace System.Data.Hsqldb.Common.Sql.UnitTests
{
    [TestFixture, ForSubject(typeof(Token))]
    public class TestToken
    {
        class LiteralValueTestParameters
        {
            public LiteralValueTestParameters(
                string chars,
                SqlTokenType tokenType,
                HsqlProviderType providerType,
                object value,
                int? errorCode,
                string errorMessage)
            {
                this.Chars = chars;
                this.TokenType = tokenType;
                this.ProviderType = providerType;
                this.Value = value;
                this.ErrorCode = errorCode;
                this.ErrorMessage = errorMessage;
            }

            public readonly string Chars;
            public readonly SqlTokenType TokenType;
            public readonly HsqlProviderType ProviderType;
            public readonly object Value;
            public readonly int? ErrorCode;
            public readonly string ErrorMessage;

            public override string ToString()
            {
                StringBuilder sb = new StringBuilder(base.ToString());

                sb.Append("[Chars: \"" + this.Chars + "\"");
                sb.Append(", TokenType: " + this.TokenType);
                sb.Append(", ProviderType: " + this.ProviderType);
                sb.Append(", Value: " + (this.Value == null ? "<NULL>" : this.Value));
                sb.Append(", ErrorCode: " + (this.ErrorCode == null ? "<NULL>" : this.ErrorCode.ToString()));
                sb.Append(", ErrorMessage: " + (this.ErrorMessage == null ? "<NULL>" : "\"" + this.ErrorMessage + "\""));
                sb.Append("]");

                return sb.ToString();
            }
        }

        [Test, OfMember("Equals")]
        public void Equals()
        {
            Token testSubject = new Token(Token.ValueFor.ACTION, SqlTokenType.Null);

            Assert.AreEqual(Token.ValueFor.ACTION, testSubject.Value);
            Assert.AreEqual(SqlTokenType.Null, testSubject.Type);

            Assert.AreEqual(new Token(Token.ValueFor.ACTION, SqlTokenType.Null), testSubject);
            Assert.AreNotEqual(new Token(Token.ValueFor.ADD, SqlTokenType.Null), testSubject);
            Assert.AreNotEqual(new Token(Token.ValueFor.ACTION, SqlTokenType.StringLiteral), testSubject);
        }

        
        [Test, OfMember("GetHashCode")]
        new public void GetHashCode()
        {
            Token testSubject = new Token(Token.ValueFor.ACTION, SqlTokenType.Null);

            Assert.AreEqual(new Token(Token.ValueFor.ACTION, 
                SqlTokenType.Null).GetHashCode(),testSubject.GetHashCode());
        }

        [Test, OfMember("IdentifierChainFirst")]
        public void IdentifierChainFirst()
        {
            Token testSubject = new Token("\"foo\".\"bar\"", "foo", "bar");

            Assert.AreEqual("foo", testSubject.QualifierPart);
        }

        [Test, OfMember("IdentifierChainLast")]
        public void IdentifierChainLast()
        {
            Token testSubject = new Token("\"foo\".\"bar\"", "foo", "bar");

            Assert.AreEqual("bar", testSubject.SubjectPart);
        }

        [Test, OfMember("LiteralValue")]
        public void LiteralValue()
        {
            List<LiteralValueTestParameters> testParameters = new List<LiteralValueTestParameters>();

            testParameters.Add(new LiteralValueTestParameters(
                "1234", 
                SqlTokenType.BigIntLiteral, 
                HsqlProviderType.BigInt,
                HsqlValuePool.getLong(1234L), 
                null, 
                null));

            testParameters.Add(new LiteralValueTestParameters(
                "TRUE", 
                SqlTokenType.BooleanLiteral,
                HsqlProviderType.Boolean,
                JavaBoolean.TRUE,
                null,
                null));

            testParameters.Add(new LiteralValueTestParameters(
                "2009-2-8",
                SqlTokenType.DateLiteral,
                HsqlProviderType.Date,
                HsqlDateTime.dateValue("2009-2-8"),
                null,
                null));

            testParameters.Add(new LiteralValueTestParameters(
                "12345678987654321.12345678987654321",
                SqlTokenType.DecimalLiteral,
                HsqlProviderType.Decimal,
                new JavaDecimal("12345678987654321.12345678987654321"),
                null,
                null));

            testParameters.Add(new LiteralValueTestParameters(
                "123.456",
                SqlTokenType.FloatLiteral,
                HsqlProviderType.Double,
                new JavaDouble(123.456D),
                null,
                null));

            testParameters.Add(new LiteralValueTestParameters(
                "FOO",
                SqlTokenType.Null,
                HsqlProviderType.Null,
                null,
                null,
                null));

            testParameters.Add(new LiteralValueTestParameters(
                "1234567898765432123456712345678987654321234567.1234567898765432123456712345678987654321234567",
                SqlTokenType.NumberLiteral,
                HsqlProviderType.Numeric,
                new JavaDecimal("1234567898765432123456712345678987654321234567.1234567898765432123456712345678987654321234567"),
                null,
                null));

            testParameters.Add(new LiteralValueTestParameters(
                "He said 'high five!'",
                SqlTokenType.StringLiteral,
                HsqlProviderType.VarChar,
                "He said 'high five!'", 
                null, 
                null));

            testParameters.Add(new LiteralValueTestParameters(
                "12:01:01", 
                SqlTokenType.TimeLiteral,
                HsqlProviderType.Time,
                HsqlDateTime.timeValue("12:01:01"), 
                null, 
                null));

            testParameters.Add(new LiteralValueTestParameters(
                "2009-02-09 12:01:01.123456", 
                SqlTokenType.TimestampLiteral,
                HsqlProviderType.TimeStamp,
                HsqlDateTime.timestampValue("2009-02-09 12:01:01.123456"),
                null,
                null));

            foreach (LiteralValueTestParameters item in testParameters)
            {
                Console.WriteLine(item);

                try
                {
                    Token token = new Token(item.Chars, item.TokenType);

                    object value = token.LiteralValue;

                    if (item.ErrorMessage != null)
                    {
                        Assert.Fail(item.ErrorMessage);
                    }

                    System.Type expectedType = HsqlConvert.ToProviderSpecificDataType(item.ProviderType);

                    if (expectedType != null && !(typeof(void).Equals(expectedType)))
                    {
                        Assert.IsInstanceOfType(expectedType, value);
                    }
                    
                    Assert.AreEqual(item.Value, value);
                }
                catch (AssertionException)
                {
                    throw;
                }
                catch (HsqlDataSourceException hdse)
                {
                    Assert.AreEqual(item.ErrorCode, -hdse.ErrorCode);
                }
            }
        }

        [Test, OfMember("ToString")]
        new public void ToString()
        {
            Token testSubject = new Token(Token.ValueFor.ACTION, SqlTokenType.NamedParameter);
            string expected = "System.Data.Hsqldb.Common.Sql.Token[value=ACTION,type=NamedParameter]";
            string actual = testSubject.ToString();

            testSubject = new Token("\".foo.\".\".bar.\"", ".foo.", ".bar.");

            expected = "System.Data.Hsqldb.Common.Sql.Token[value=\".foo.\".\".bar.\",type=IdentifierChain,qualifierPart=.foo.,subjectPart=.bar.]";
            actual = testSubject.ToString();

            Assert.AreEqual(expected, actual);
        }

        [Test, OfMember("Value")]
        public void Value()
        {
            Token testSubject = new Token(Token.ValueFor.ACTION, SqlTokenType.Null);

            Assert.AreEqual(Token.ValueFor.ACTION, testSubject.Value);
        }

        [Test, OfMember("Type")]
        public void Type()
        {
            Token testSubject = new Token(Token.ValueFor.ACTION, SqlTokenType.Null);

            Assert.AreEqual(SqlTokenType.Null, testSubject.Type);
        }
    }
}
