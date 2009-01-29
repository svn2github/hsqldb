using System;
using TestCoverage;
using NUnit.Framework;

namespace System.Data.Hsqldb.Common.IO.UnitTests
{
    [TestFixture()]
    [TestSubjectClassAttribute(TestSubject=typeof(System.Data.Hsqldb.Common.IO.JavaOutputStreamWrapper))]
    public class TestJavaOutputStreamWrapper
    {
        
        [TestSubjectMemberAttribute(MemeberName="Flush")]
        [Test()]
        public virtual void Flush()
        {
            // Create Constructor Parameters
            RecorderOutputStream outputStreamRecording = new RecorderOutputStream();

            System.Data.Hsqldb.Common.IO.JavaOutputStreamWrapper TestSubject = new System.Data.Hsqldb.Common.IO.JavaOutputStreamWrapper(outputStreamRecording);


            TestSubject.Flush();

            // 
            // Write your assertions here.
            // 
            Assert.IsTrue(TestSubject.Recordings.OutputStream.flushRecording.Called);
        }
        
        [TestSubjectMemberAttribute(MemeberName="Read")]
        [Test()]
        public virtual void Read()
        {
            // Create Constructor Parameters
            RecorderOutputStream outputStreamRecording = new RecorderOutputStream();

            System.Data.Hsqldb.Common.IO.JavaOutputStreamWrapper TestSubject = new System.Data.Hsqldb.Common.IO.JavaOutputStreamWrapper(outputStreamRecording);

            // Create Test Method Parameters
        }
        
        [TestSubjectMemberAttribute(MemeberName="Seek")]
        [Test()]
        public virtual void Seek()
        {
            // Create Constructor Parameters
            RecorderOutputStream outputStreamRecording = new RecorderOutputStream();

            System.Data.Hsqldb.Common.IO.JavaOutputStreamWrapper TestSubject = new System.Data.Hsqldb.Common.IO.JavaOutputStreamWrapper(outputStreamRecording);

            // Create Test Method Parameters

            // There is no default constuctor for the parameter offset type Int64.
            long offset;


            // There is no default constuctor for the parameter origin type SeekOrigin.
            System.IO.SeekOrigin origin;


            TestSubject.Seek(offset, origin);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="SetLength")]
        [Test()]
        public virtual void SetLength()
        {
            // Create Constructor Parameters
            RecorderOutputStream outputStreamRecording = new RecorderOutputStream();

            System.Data.Hsqldb.Common.IO.JavaOutputStreamWrapper TestSubject = new System.Data.Hsqldb.Common.IO.JavaOutputStreamWrapper(outputStreamRecording);

            // Create Test Method Parameters

            // There is no default constuctor for the parameter value type Int64.
            long value;


            TestSubject.SetLength(value);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="Write")]
        [Test()]
        public virtual void Write()
        {
            // Create Constructor Parameters
            RecorderOutputStream outputStreamRecording = new RecorderOutputStream();

            System.Data.Hsqldb.Common.IO.JavaOutputStreamWrapper TestSubject = new System.Data.Hsqldb.Common.IO.JavaOutputStreamWrapper(outputStreamRecording);

            // Create Test Method Parameters
        }
    }
}
