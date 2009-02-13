using System;
using NUnit.Framework;
using System.Data.Hsqldb.TestCoverage;
using System.Data.Hsqldb.Common.Enumeration;

namespace System.Data.Hsqldb.Common.Sql.UnitTests
{
    [TestFixture, ForSubject(typeof(Token))]
    public class TestToken
    {
        [Test, OfMember("Equals")]
        public void Equals()
        {
            Token testSubject = new Token(Token.ValueFor.ACTION, TokenType.Null);

            Assert.AreEqual(Token.ValueFor.ACTION, testSubject.Value);
            Assert.AreEqual(TokenType.Null, testSubject.Type);

            Assert.AreEqual(new Token(Token.ValueFor.ACTION, TokenType.Null), testSubject);
            Assert.AreNotEqual(new Token(Token.ValueFor.ADD, TokenType.Null), testSubject);
            Assert.AreNotEqual(new Token(Token.ValueFor.ACTION, TokenType.StringLiteral), testSubject);
        }

        
        [Test, OfMember("GetHashCode")]
        new public void GetHashCode()
        {
            Token testSubject = new Token(Token.ValueFor.ACTION, TokenType.Null);

            Assert.AreEqual(new Token(Token.ValueFor.ACTION, 
                TokenType.Null).GetHashCode(),testSubject.GetHashCode());
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
            Token testSubject = new Token("1234", TokenType.BigIntLiteral);

            Assert.IsInstanceOfType(typeof(Int64), testSubject.LiteralValue);
            Assert.AreEqual(1234D, testSubject.LiteralValue);

            testSubject = new Token("TRUE", TokenType.BooleanLiteral);

            Assert.IsInstanceOfType(typeof(bool), testSubject.LiteralValue);
            Assert.AreEqual(true, testSubject.LiteralValue);

            testSubject = new Token("2009-2-8", TokenType.DateLiteral);

            Assert.IsInstanceOfType(typeof(java.sql.Date), testSubject.LiteralValue);
            Assert.AreEqual(java.sql.Date.valueOf("2009-2-8"), testSubject.LiteralValue);

            testSubject = new Token("12345678987654321.12345678987654321", TokenType.DecimalLiteral);

            Assert.IsInstanceOfType(typeof(decimal), testSubject.LiteralValue);
            Assert.AreEqual(12345678987654321.12345678987654321M, testSubject.LiteralValue);

            testSubject = new Token("123.456", TokenType.FloatLiteral);

            Assert.IsInstanceOfType(typeof(double), testSubject.LiteralValue);
            Assert.AreEqual(123.456, testSubject.LiteralValue);

            testSubject = new Token("FOO", TokenType.Null);
            Assert.AreEqual("FOO", testSubject.LiteralValue);

            testSubject = new Token("1234567898765432123456712345678987654321234567.1234567898765432123456712345678987654321234567", TokenType.NumberLiteral);

            Assert.IsInstanceOfType(typeof(java.math.BigDecimal), testSubject.LiteralValue);
            Assert.AreEqual(new java.math.BigDecimal("1234567898765432123456712345678987654321234567.1234567898765432123456712345678987654321234567"), testSubject.LiteralValue);

            testSubject = new Token("'He said ''high five!'''", TokenType.StringLiteral);
            Assert.IsInstanceOfType(typeof(string), testSubject.LiteralValue);
            Assert.AreEqual("He said 'high five!'", testSubject.LiteralValue);

            testSubject = new Token("12:01:01", TokenType.TimeLiteral);
            Assert.IsInstanceOfType(typeof(java.sql.Time), testSubject.LiteralValue);
            Assert.AreEqual(java.sql.Time.valueOf("12:01:01"), testSubject.LiteralValue);

            testSubject = new Token("2009-02-09 12:01:01.001", TokenType.TimestampLiteral);
            Assert.IsInstanceOfType(typeof(java.sql.Timestamp), testSubject.LiteralValue);
            Assert.AreEqual(java.sql.Timestamp.valueOf("2009-02-09 12:01:01.001"), testSubject.LiteralValue);
        }

        [Test, OfMember("ToString")]
        new public void ToString()
        {
            Token testSubject = new Token(Token.ValueFor.ACTION, TokenType.NamedParameter);
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
            Token testSubject = new Token(Token.ValueFor.ACTION, TokenType.Null);

            Assert.AreEqual(Token.ValueFor.ACTION, testSubject.Value);
        }

        [Test, OfMember("Type")]
        public void Type()
        {
            Token testSubject = new Token(Token.ValueFor.ACTION, TokenType.Null);

            Assert.AreEqual(TokenType.Null, testSubject.Type);
        }
    }
}
