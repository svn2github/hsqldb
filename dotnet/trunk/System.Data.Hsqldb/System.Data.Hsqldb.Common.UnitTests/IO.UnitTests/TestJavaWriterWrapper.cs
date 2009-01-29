using System;
using TestCoverage;
using NUnit.Framework;

namespace System.Data.Hsqldb.Common.IO.UnitTests
{
    [TestFixture()]
    [TestSubjectClassAttribute(TestSubject=typeof(System.Data.Hsqldb.Common.IO.JavaWriterWrapper))]
    public class TestJavaWriterWrapper
    {
        
        [TestSubjectMemberAttribute(MemeberName="Flush")]
        [Test()]
        public virtual void Flush()
        {
            // Create Constructor Parameters
            RecorderWriter writerRecording = new RecorderWriter();

            System.Data.Hsqldb.Common.IO.JavaWriterWrapper TestSubject = new System.Data.Hsqldb.Common.IO.JavaWriterWrapper(writerRecording);


            TestSubject.Flush();

            // 
            // Write your assertions here.
            // 
            Assert.IsTrue(TestSubject.Recordings.Writer.flushRecording.Called);
        }
        
        [TestSubjectMemberAttribute(MemeberName="Write")]
        [Test()]
        public virtual void Write()
        {
            // Create Constructor Parameters
            RecorderWriter writerRecording = new RecorderWriter();

            System.Data.Hsqldb.Common.IO.JavaWriterWrapper TestSubject = new System.Data.Hsqldb.Common.IO.JavaWriterWrapper(writerRecording);

            // Create Test Method Parameters

            // There is no default constuctor for the parameter value type Char.
            char value;


            TestSubject.Write(value);

            // 
            // Write your assertions here.
            // 
            Assert.AreEqual("<Please replace with valid value.>", TestSubject.Recordings.Writer.writeInt32Recording.PassedInt32b);
            Assert.IsTrue(TestSubject.Recordings.Writer.writeInt32Recording.Called);
        }
    }
}
