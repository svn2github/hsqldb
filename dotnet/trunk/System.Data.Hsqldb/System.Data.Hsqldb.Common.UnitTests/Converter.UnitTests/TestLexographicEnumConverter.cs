using System;
using System.Data.Hsqldb.TestCoverage;
using System.ComponentModel;
using System.Collections;
using NUnit.Framework;

namespace System.Data.Hsqldb.Common.Converter.UnitTests
{
    [TestFixture, ForSubject(typeof(LexographicEnumConverter))]
    public class TestLexographicEnumConverter
    {
        [Test, OfMember("GetStandardValues")]        
        public virtual void GetStandardValues()
        {
            LexographicEnumConverter TestSubject = new LexographicEnumConverter(typeof(Base64FormattingOptions));

            Base64FormattingOptions[] expected = new Base64FormattingOptions[] 
            {
                Base64FormattingOptions.InsertLineBreaks, 
                Base64FormattingOptions.None
            };
            
            Base64FormattingOptions[] actual = new Base64FormattingOptions[2];
            
            TestSubject.GetStandardValues().CopyTo(actual,0);

            for (int i = 0; i < expected.Length; i++)
            {
                Assert.AreEqual(expected[i], actual[i]);
            }
        }

        [Test, OfMember("GetStandardValuesSupported")]
        public virtual void GetStandardValuesSupported()
        {
            LexographicEnumConverter TestSubject = new LexographicEnumConverter(typeof(Base64FormattingOptions));

            bool expected = true;
            bool actual = TestSubject.GetStandardValuesSupported(null);

            Assert.AreEqual(expected, actual); 
        }
    }
}
