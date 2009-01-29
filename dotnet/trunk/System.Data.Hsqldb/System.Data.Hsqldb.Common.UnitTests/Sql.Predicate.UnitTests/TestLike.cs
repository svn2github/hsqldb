using System;
using TestCoverage;
using NUnit.Framework;

namespace System.Data.Hsqldb.Common.Sql.Predicate.UnitTests
{
    [TestFixture()]
    [TestSubjectClassAttribute(TestSubject=typeof(System.Data.Hsqldb.Common.Sql.Predicate.Like))]
    public class TestLike
    {
        
        [TestSubjectMemberAttribute(MemeberName="Matches")]
        [Test()]
        public virtual void Matches()
        {
            // Create Constructor Parameters

            // There is no default constuctor for the parameter ignoreCase type Boolean.
            bool ignoreCase;


            // There is no default constuctor for the parameter escapeCharacter type Nullable.
            System.Nullable escapeCharacter;


            System.Data.Hsqldb.Common.Sql.Predicate.Like TestSubject = new System.Data.Hsqldb.Common.Sql.Predicate.Like(ignoreCase, escapeCharacter);

            // Create Test Method Parameters

            // There is no default constuctor for the parameter value type String.
            string value;


            TestSubject.Matches(value);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="SetPattern")]
        [Test()]
        public virtual void SetPattern()
        {
            // Create Constructor Parameters

            // There is no default constuctor for the parameter ignoreCase type Boolean.
            bool ignoreCase;


            // There is no default constuctor for the parameter escapeCharacter type Nullable.
            System.Nullable escapeCharacter;


            System.Data.Hsqldb.Common.Sql.Predicate.Like TestSubject = new System.Data.Hsqldb.Common.Sql.Predicate.Like(ignoreCase, escapeCharacter);

            // Create Test Method Parameters

            // There is no default constuctor for the parameter pattern type String.
            string pattern;


            TestSubject.SetPattern(pattern);

            // 
            // Write your assertions here.
            // 
        }
    }
}
