using System;
using System.Data.Hsqldb.TestCoverage;
using System.Runtime.Serialization;
using NUnit.Framework;

namespace System.Data.Hsqldb.Common.UnitTests
{
    [TestFixture, ForSubject(typeof(HsqlDataSourceException))]
    public class TestHsqlDataSourceException
    {
        
        [Test, OfMember("GetObjectData")]
        public void GetObjectData()
        {
            Type type = typeof(HsqlDataSourceException);
            FormatterConverter converter = new FormatterConverter();
            SerializationInfo info = new SerializationInfo(type, converter);
            StreamingContext context = new StreamingContext();

            HsqlDataSourceException testSubject = new HsqlDataSourceException("foo", -1, "42001");

            testSubject.GetObjectData(info, context);

            Assert.Fail("TODO");
        }
    }
}
