using System;
using TestCoverage;
using NUnit.Framework;

namespace System.Data.Hsqldb.Common.IO.UnitTests
{
    [TestFixture()]
    [TestSubjectClassAttribute(TestSubject=typeof(System.Data.Hsqldb.Common.IO.JavaOutputStreamAdapter))]
    public class TestJavaOutputStreamAdapter
    {
        
        [TestSubjectMemberAttribute(MemeberName="close")]
        [Test()]
        public virtual void close()
        {
            // Create Constructor Parameters
            RecorderStream streamRecording = new RecorderStream();

            System.Data.Hsqldb.Common.IO.JavaOutputStreamAdapter TestSubject = new System.Data.Hsqldb.Common.IO.JavaOutputStreamAdapter(streamRecording);


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

            System.Data.Hsqldb.Common.IO.JavaOutputStreamAdapter TestSubject = new System.Data.Hsqldb.Common.IO.JavaOutputStreamAdapter(streamRecording);


            TestSubject.Dispose();

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="flush")]
        [Test()]
        public virtual void flush()
        {
            // Create Constructor Parameters
            RecorderStream streamRecording = new RecorderStream();

            System.Data.Hsqldb.Common.IO.JavaOutputStreamAdapter TestSubject = new System.Data.Hsqldb.Common.IO.JavaOutputStreamAdapter(streamRecording);


            TestSubject.flush();

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="write")]
        [Test()]
        public virtual void write()
        {
            // Create Constructor Parameters
            RecorderStream streamRecording = new RecorderStream();

            System.Data.Hsqldb.Common.IO.JavaOutputStreamAdapter TestSubject = new System.Data.Hsqldb.Common.IO.JavaOutputStreamAdapter(streamRecording);

            // Create Test Method Parameters

            // There is no default constuctor for the parameter b type Int32.
            int b;


            TestSubject.write(b);

            // 
            // Write your assertions here.
            // 
        }
    }
}
