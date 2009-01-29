using System;
using TestCoverage;
using NUnit.Framework;

namespace System.Data.Hsqldb.Common.IO.UnitTests
{
    [TestFixture()]
    [TestSubjectClassAttribute(TestSubject=typeof(System.Data.Hsqldb.Common.IO.JavaInputStreamAdapter))]
    public class TestJavaInputStreamAdapter
    {
        
        [TestSubjectMemberAttribute(MemeberName="close")]
        [Test()]
        public virtual void close()
        {
            // Create Constructor Parameters
            RecorderStream streamRecording = new RecorderStream();

            System.Data.Hsqldb.Common.IO.JavaInputStreamAdapter TestSubject = new System.Data.Hsqldb.Common.IO.JavaInputStreamAdapter(streamRecording);


            TestSubject.close();

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="Dispose")]
        [Test()]
        public virtual void Dispose()
        {
            // Create Constructor Parameters
            RecorderStream streamRecording = new RecorderStream();

            System.Data.Hsqldb.Common.IO.JavaInputStreamAdapter TestSubject = new System.Data.Hsqldb.Common.IO.JavaInputStreamAdapter(streamRecording);


            TestSubject.Dispose();

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="read")]
        [Test()]
        public virtual void read()
        {
            // Create Constructor Parameters
            RecorderStream streamRecording = new RecorderStream();

            System.Data.Hsqldb.Common.IO.JavaInputStreamAdapter TestSubject = new System.Data.Hsqldb.Common.IO.JavaInputStreamAdapter(streamRecording);


            TestSubject.read();

            // 
            // Write your assertions here.
            // 
        }
    }
}
