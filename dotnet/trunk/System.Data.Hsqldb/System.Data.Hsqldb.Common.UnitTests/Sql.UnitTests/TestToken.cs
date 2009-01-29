using System;
using TestCoverage;
using NUnit.Framework;

namespace System.Data.Hsqldb.Common.Sql.UnitTests
{
    [TestFixture()]
    [TestSubjectClassAttribute(TestSubject=typeof(System.Data.Hsqldb.Common.Sql.Token))]
    public class TestToken
    {
        
        [TestSubjectMemberAttribute(MemeberName="Equals")]
        [Test()]
        public virtual void Equals()
        {
            // Create Constructor Parameters

            // There is no default constuctor for the parameter value type String.
            string value;


            // There is no default constuctor for the parameter type type TokenType.
            System.Data.Hsqldb.Common.Enumeration.TokenType type;


            System.Data.Hsqldb.Common.Sql.Token TestSubject = new System.Data.Hsqldb.Common.Sql.Token(value, type);

            // Create Test Method Parameters

            // There is no default constuctor for the parameter token type Token.
            System.Data.Hsqldb.Common.Sql.Token token;


            TestSubject.Equals(token);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="Equals")]
        [Test()]
        public virtual void TestEquals()
        {
            // Create Constructor Parameters

            // There is no default constuctor for the parameter value type String.
            string value;


            // There is no default constuctor for the parameter type type TokenType.
            System.Data.Hsqldb.Common.Enumeration.TokenType type;


            System.Data.Hsqldb.Common.Sql.Token TestSubject = new System.Data.Hsqldb.Common.Sql.Token(value, type);

            // Create Test Method Parameters
            object obj = new object();

            TestSubject.Equals(obj);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="GetHashCode")]
        [Test()]
        public virtual void GetHashCode()
        {
            // Create Constructor Parameters

            // There is no default constuctor for the parameter value type String.
            string value;


            // There is no default constuctor for the parameter type type TokenType.
            System.Data.Hsqldb.Common.Enumeration.TokenType type;


            System.Data.Hsqldb.Common.Sql.Token TestSubject = new System.Data.Hsqldb.Common.Sql.Token(value, type);


            TestSubject.GetHashCode();

            // 
            // Write your assertions here.
            // 
        }
    }
}
