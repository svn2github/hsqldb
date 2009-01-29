using System;
using TestCoverage;
using NUnit.Framework;

namespace System.Data.Hsqldb.Common.IO.UnitTests
{
    [TestFixture()]
    [TestSubjectClassAttribute(TestSubject=typeof(System.Data.Hsqldb.Common.IO.JavaInputStreamWrapper))]
    public class TestJavaInputStreamWrapper
    {
        
        [TestSubjectMemberAttribute(MemeberName="Flush")]
        [Test()]
        public virtual void Flush()
        {
            // Create Constructor Parameters
            RecorderInputStream inputStreamRecording = new RecorderInputStream();

            System.Data.Hsqldb.Common.IO.JavaInputStreamWrapper TestSubject = new System.Data.Hsqldb.Common.IO.JavaInputStreamWrapper(inputStreamRecording);


            TestSubject.Flush();

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="Read")]
        [Test()]
        public virtual void Read()
        {
            // Create Constructor Parameters
            RecorderInputStream inputStreamRecording = new RecorderInputStream();

            System.Data.Hsqldb.Common.IO.JavaInputStreamWrapper TestSubject = new System.Data.Hsqldb.Common.IO.JavaInputStreamWrapper(inputStreamRecording);

            // Create Test Method Parameters
        }
        
        [TestSubjectMemberAttribute(MemeberName="Seek")]
        [Test()]
        public virtual void Seek()
        {
            // Create Constructor Parameters
            RecorderInputStream inputStreamRecording = new RecorderInputStream();

            System.Data.Hsqldb.Common.IO.JavaInputStreamWrapper TestSubject = new System.Data.Hsqldb.Common.IO.JavaInputStreamWrapper(inputStreamRecording);

            // Create Test Method Parameters

            // There is no default constuctor for the parameter offset type Int64.
            long offset;


            // There is no default constuctor for the parameter origin type SeekOrigin.
            System.IO.SeekOrigin origin;

            TestSubject.Recordings.InputStream.skipInt64Recording.ReturnValue = "Please set the return value.";

            TestSubject.Seek(offset, origin);

            // 
            // Write your assertions here.
            // 
            Assert.AreEqual("<Please replace with valid value.>", TestSubject.Recordings.InputStream.skipInt64Recording.PassedInt64n);
            Assert.IsTrue(TestSubject.Recordings.InputStream.skipInt64Recording.Called);
        }
        
        [TestSubjectMemberAttribute(MemeberName="SetLength")]
        [Test()]
        public virtual void SetLength()
        {
            // Create Constructor Parameters
            RecorderInputStream inputStreamRecording = new RecorderInputStream();

            System.Data.Hsqldb.Common.IO.JavaInputStreamWrapper TestSubject = new System.Data.Hsqldb.Common.IO.JavaInputStreamWrapper(inputStreamRecording);

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
            RecorderInputStream inputStreamRecording = new RecorderInputStream();

            System.Data.Hsqldb.Common.IO.JavaInputStreamWrapper TestSubject = new System.Data.Hsqldb.Common.IO.JavaInputStreamWrapper(inputStreamRecording);

            // Create Test Method Parameters
        }
    }
}
