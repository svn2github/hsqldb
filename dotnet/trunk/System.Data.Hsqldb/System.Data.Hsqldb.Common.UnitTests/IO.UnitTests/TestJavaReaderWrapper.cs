using System;
using TestCoverage;
using NUnit.Framework;

namespace System.Data.Hsqldb.Common.IO.UnitTests
{
    [TestFixture()]
    [TestSubjectClassAttribute(TestSubject=typeof(System.Data.Hsqldb.Common.IO.JavaReaderWrapper))]
    public class TestJavaReaderWrapper
    {
        
        [TestSubjectMemberAttribute(MemeberName="Peek")]
        [Test()]
        public virtual void Peek()
        {
            // Create Constructor Parameters
            RecorderReader readerRecording = new RecorderReader();

            System.Data.Hsqldb.Common.IO.JavaReaderWrapper TestSubject = new System.Data.Hsqldb.Common.IO.JavaReaderWrapper(readerRecording);


            TestSubject.Peek();

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="Read")]
        [Test()]
        public virtual void Read()
        {
            // Create Constructor Parameters
            RecorderReader readerRecording = new RecorderReader();

            System.Data.Hsqldb.Common.IO.JavaReaderWrapper TestSubject = new System.Data.Hsqldb.Common.IO.JavaReaderWrapper(readerRecording);

            TestSubject.Recordings.Reader.readRecording.ReturnValue = "Please set the return value.";

            TestSubject.Read();

            // 
            // Write your assertions here.
            // 
            Assert.IsTrue(TestSubject.Recordings.Reader.readRecording.Called);
        }
        
        [TestSubjectMemberAttribute(MemeberName="ReadLine")]
        [Test()]
        public virtual void ReadLine()
        {
            // Create Constructor Parameters
            RecorderReader readerRecording = new RecorderReader();

            System.Data.Hsqldb.Common.IO.JavaReaderWrapper TestSubject = new System.Data.Hsqldb.Common.IO.JavaReaderWrapper(readerRecording);

            TestSubject.Recordings.LineReader.readLineRecording.ReturnValue = "Please set the return value.";

            TestSubject.ReadLine();

            // 
            // Write your assertions here.
            // 
            Assert.IsTrue(TestSubject.Recordings.LineReader.readLineRecording.Called);
        }
        
        [TestSubjectMemberAttribute(MemeberName="ReadToEnd")]
        [Test()]
        public virtual void ReadToEnd()
        {
            // Create Constructor Parameters
            RecorderReader readerRecording = new RecorderReader();

            System.Data.Hsqldb.Common.IO.JavaReaderWrapper TestSubject = new System.Data.Hsqldb.Common.IO.JavaReaderWrapper(readerRecording);


            TestSubject.ReadToEnd();

            // 
            // Write your assertions here.
            // 
        }
    }
}
