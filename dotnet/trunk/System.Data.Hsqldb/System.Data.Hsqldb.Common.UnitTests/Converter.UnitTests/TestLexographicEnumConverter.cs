using System;
using TestCoverage;
using NUnit.Framework;

namespace System.Data.Hsqldb.Common.Converter.UnitTests
{
    [TestFixture()]
    [TestSubjectClassAttribute(TestSubject=typeof(System.Data.Hsqldb.Common.Converter.LexographicEnumConverter))]
    public class TestLexographicEnumConverter
    {
        
        [TestSubjectMemberAttribute(MemeberName="GetStandardValues")]
        [Test()]
        public virtual void GetStandardValues()
        {
            // Create Constructor Parameters
            RecorderType typeRecording = new RecorderType();

            System.Data.Hsqldb.Common.Converter.LexographicEnumConverter TestSubject = new System.Data.Hsqldb.Common.Converter.LexographicEnumConverter(typeRecording);

            // Create Test Method Parameters
            RecorderITypeDescriptorContext contextRecording = new RecorderITypeDescriptorContext();

            TestSubject.GetStandardValues(contextRecording);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="GetStandardValuesSupported")]
        [Test()]
        public virtual void GetStandardValuesSupported()
        {
            // Create Constructor Parameters
            RecorderType typeRecording = new RecorderType();

            System.Data.Hsqldb.Common.Converter.LexographicEnumConverter TestSubject = new System.Data.Hsqldb.Common.Converter.LexographicEnumConverter(typeRecording);

            // Create Test Method Parameters
            RecorderITypeDescriptorContext contextRecording = new RecorderITypeDescriptorContext();

            TestSubject.GetStandardValuesSupported(contextRecording);

            // 
            // Write your assertions here.
            // 
        }
    }
}
