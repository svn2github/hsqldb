using System;
using TestCoverage;
using NUnit.Framework;

namespace System.Data.Hsqldb.Common.UnitTests
{
    [TestFixture()]
    [TestSubjectClassAttribute(TestSubject=typeof(System.Data.Hsqldb.Common.HsqlDataSourceExceptionCollection))]
    public class TestHsqlDataSourceExceptionCollection
    {        
        [TestSubjectMemberAttribute(MemeberName="CopyTo")]
        [Test()]
        public virtual void CopyTo()
        {

            // Create Constructor Parameters

            System.Data.Hsqldb.Common.HsqlDataSourceExceptionCollection TestSubject = new System.Data.Hsqldb.Common.HsqlDataSourceExceptionCollection();

            // Create Test Method Parameters
            RecorderArray arrayRecording = new RecorderArray();

            // There is no default constuctor for the parameter index type Int32.
            int index;


            TestSubject.CopyTo(arrayRecording, index);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="GetEnumerator")]
        [Test()]
        public virtual void GetEnumerator()
        {
            // Create Constructor Parameters

            System.Data.Hsqldb.Common.HsqlDataSourceExceptionCollection TestSubject = new System.Data.Hsqldb.Common.HsqlDataSourceExceptionCollection();


            TestSubject.GetEnumerator();

            // 
            // Write your assertions here.
            // 
        }
    }
}
