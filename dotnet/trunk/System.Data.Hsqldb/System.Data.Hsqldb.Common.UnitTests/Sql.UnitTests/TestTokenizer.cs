using System;
using TestCoverage;
using NUnit.Framework;

namespace System.Data.Hsqldb.Common.Sql.UnitTests
{
    [TestFixture()]
    [TestSubjectClassAttribute(TestSubject=typeof(System.Data.Hsqldb.Common.Sql.Tokenizer))]
    public class TestTokenizer
    {
        
        [TestSubjectMemberAttribute(MemeberName="GetNextAsBigint")]
        [Test()]
        public virtual void GetNextAsBigint()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.Sql.Tokenizer TestSubject = new System.Data.Hsqldb.Common.Sql.Tokenizer();


            TestSubject.GetNextAsBigint();

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="GetNextAsInt")]
        [Test()]
        public virtual void GetNextAsInt()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.Sql.Tokenizer TestSubject = new System.Data.Hsqldb.Common.Sql.Tokenizer();


            TestSubject.GetNextAsInt();

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="GetNextAsLiteralValue")]
        [Test()]
        public virtual void GetNextAsLiteralValue()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.Sql.Tokenizer TestSubject = new System.Data.Hsqldb.Common.Sql.Tokenizer();

            // Create Test Method Parameters

            // There is no default constuctor for the parameter requestedDataType type HsqlProviderType.
            System.Data.Hsqldb.Common.Enumeration.HsqlProviderType requestedDataType;


            TestSubject.GetNextAsLiteralValue(requestedDataType);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="GetNextAsName")]
        [Test()]
        public virtual void GetNextAsName()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.Sql.Tokenizer TestSubject = new System.Data.Hsqldb.Common.Sql.Tokenizer();


            TestSubject.GetNextAsName();

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="GetNextAsSimpleName")]
        [Test()]
        public virtual void GetNextAsSimpleName()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.Sql.Tokenizer TestSubject = new System.Data.Hsqldb.Common.Sql.Tokenizer();


            TestSubject.GetNextAsSimpleName();

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="GetNextAsSimpleToken")]
        [Test()]
        public virtual void GetNextAsSimpleToken()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.Sql.Tokenizer TestSubject = new System.Data.Hsqldb.Common.Sql.Tokenizer();


            TestSubject.GetNextAsSimpleToken();

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="GetNextAsString")]
        [Test()]
        public virtual void GetNextAsString()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.Sql.Tokenizer TestSubject = new System.Data.Hsqldb.Common.Sql.Tokenizer();


            TestSubject.GetNextAsString();

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="GetPart")]
        [Test()]
        public virtual void GetPart()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.Sql.Tokenizer TestSubject = new System.Data.Hsqldb.Common.Sql.Tokenizer();

            // Create Test Method Parameters

            // There is no default constuctor for the parameter startIndex type Int32.
            int startIndex;


            // There is no default constuctor for the parameter endIndex type Int32.
            int endIndex;


            TestSubject.GetPart(startIndex, endIndex);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="GetThis")]
        [Test()]
        public virtual void GetThis()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.Sql.Tokenizer TestSubject = new System.Data.Hsqldb.Common.Sql.Tokenizer();

            // Create Test Method Parameters

            // There is no default constuctor for the parameter match type String.
            string match;


            TestSubject.GetThis(match);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="IdentiferChainLengthExceeded")]
        [Test()]
        public virtual void IdentiferChainLengthExceeded()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.Sql.Tokenizer TestSubject = new System.Data.Hsqldb.Common.Sql.Tokenizer();


            TestSubject.IdentiferChainLengthExceeded();

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="IllegalWaitState")]
        [Test()]
        public virtual void IllegalWaitState()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.Sql.Tokenizer TestSubject = new System.Data.Hsqldb.Common.Sql.Tokenizer();


            TestSubject.IllegalWaitState();

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="InvalidIdentifier")]
        [Test()]
        public virtual void InvalidIdentifier()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.Sql.Tokenizer TestSubject = new System.Data.Hsqldb.Common.Sql.Tokenizer();

            // Create Test Method Parameters
            object token = new object();

            TestSubject.InvalidIdentifier(token);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="IsGetThis")]
        [Test()]
        public virtual void IsGetThis()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.Sql.Tokenizer TestSubject = new System.Data.Hsqldb.Common.Sql.Tokenizer();

            // Create Test Method Parameters

            // There is no default constuctor for the parameter match type String.
            string match;


            TestSubject.IsGetThis(match);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="MatchFailed")]
        [Test()]
        public virtual void MatchFailed()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.Sql.Tokenizer TestSubject = new System.Data.Hsqldb.Common.Sql.Tokenizer();

            // Create Test Method Parameters
            object token = new object();
            object match = new object();

            TestSubject.MatchFailed(token, match);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="Reset")]
        [Test()]
        public virtual void Reset()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.Sql.Tokenizer TestSubject = new System.Data.Hsqldb.Common.Sql.Tokenizer();

            // Create Test Method Parameters

            // There is no default constuctor for the parameter chars type String.
            string chars;


            TestSubject.Reset(chars);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="SetPartMarker")]
        [Test()]
        public virtual void SetPartMarker()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.Sql.Tokenizer TestSubject = new System.Data.Hsqldb.Common.Sql.Tokenizer();


            TestSubject.SetPartMarker();

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="UnexpectedEndOfCommand")]
        [Test()]
        public virtual void UnexpectedEndOfCommand()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.Sql.Tokenizer TestSubject = new System.Data.Hsqldb.Common.Sql.Tokenizer();


            TestSubject.UnexpectedEndOfCommand();

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="UnexpectedToken")]
        [Test()]
        public virtual void UnexpectedToken()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.Sql.Tokenizer TestSubject = new System.Data.Hsqldb.Common.Sql.Tokenizer();

            // Create Test Method Parameters
            object token = new object();

            TestSubject.UnexpectedToken(token);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="WasThis")]
        [Test()]
        public virtual void WasThis()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.Sql.Tokenizer TestSubject = new System.Data.Hsqldb.Common.Sql.Tokenizer();

            // Create Test Method Parameters

            // There is no default constuctor for the parameter match type String.
            string match;


            TestSubject.WasThis(match);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="WrongDataType")]
        [Test()]
        public virtual void WrongDataType()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.Sql.Tokenizer TestSubject = new System.Data.Hsqldb.Common.Sql.Tokenizer();

            // Create Test Method Parameters

            // There is no default constuctor for the parameter type type HsqlProviderType.
            System.Data.Hsqldb.Common.Enumeration.HsqlProviderType type;


            TestSubject.WrongDataType(type);

            // 
            // Write your assertions here.
            // 
        }
    }
}
