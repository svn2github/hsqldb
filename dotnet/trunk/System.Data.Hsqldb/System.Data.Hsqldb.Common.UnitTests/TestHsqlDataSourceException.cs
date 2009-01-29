using System;
using TestCoverage;
using NUnit.Framework;

namespace System.Data.Hsqldb.Common.UnitTests
{
    [TestFixture()]
    [TestSubjectClassAttribute(TestSubject=typeof(System.Data.Hsqldb.Common.HsqlDataSourceException))]
    public class TestHsqlDataSourceException
    {
        
        [TestSubjectMemberAttribute(MemeberName="GetObjectData")]
        [Test()]
        public virtual void GetObjectData()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.HsqlDataSourceException TestSubject = new System.Data.Hsqldb.Common.HsqlDataSourceException();

            // Create Test Method Parameters

            // There is no default constuctor for the parameter info type SerializationInfo.
            System.Runtime.Serialization.SerializationInfo info;


            // There is no default constuctor for the parameter context type StreamingContext.
            System.Runtime.Serialization.StreamingContext context;


            TestSubject.GetObjectData(info, context);

            // 
            // Write your assertions here.
            // 
        }
    }
}
