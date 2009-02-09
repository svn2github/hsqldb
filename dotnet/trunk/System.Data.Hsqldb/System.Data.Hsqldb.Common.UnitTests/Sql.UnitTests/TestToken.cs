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

            Assert.AreEqual("foo", testSubject.IdentifierChainFirst);
        }

        [Test, OfMember("IdentifierChainLast")]
        public void IdentifierChainLast()
        {
            Token testSubject = new Token("\"foo\".\"bar\"", "foo", "bar");

            Assert.AreEqual("bar", testSubject.IdentifierChainLast);
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
            Assert.AreEqual(Token.dateValueOf("2009-2-8"), testSubject.LiteralValue);

            testSubject = new Token("12345678987654321.12345678987654321", TokenType.DecimalLiteral);

            Assert.IsInstanceOfType(typeof(decimal), testSubject.LiteralValue);
            Assert.AreEqual(12345678987654321.12345678987654321M, testSubject.LiteralValue);
        }

        [Test, OfMember("ToString")]
        new public void ToString()
        {
            Token testSubject = new Token(Token.ValueFor.ACTION, TokenType.NamedParameter);
            string expected = "System.Data.Hsqldb.Common.Sql.Token[value=ACTION,type=NamedParameter]";
            string actual = testSubject.ToString();

            testSubject = new Token("\".foo.\".\".bar.\"", ".foo.", ".bar.");

            expected = "System.Data.Hsqldb.Common.Sql.Token[value=\".foo.\".\".bar.\",type=IdentifierChain,identifierChainFirst=.foo.,identifierChainLast=.bar.]";
            actual = testSubject.ToString();

            Assert.AreEqual(expected, actual);
        }

        [Test, OfMember("Value")]
        public void Value()
        {
            Assert.Fail("TODO");
        }

        [Test, OfMember("Type")]
        public void Type()
        {
            Assert.Fail("TODO");
        }
    }
}
