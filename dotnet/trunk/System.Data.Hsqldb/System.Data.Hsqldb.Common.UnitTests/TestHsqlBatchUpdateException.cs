using System;
using NUnit.Framework;
using System.Data.Hsqldb.TestCoverage;
using System.Runtime.Serialization;

namespace System.Data.Hsqldb.Common.UnitTests
{
    [TestFixture, ForSubject(typeof(HsqlBatchUpdateException))]
    public class TestHsqlBatchUpdateException
    {
        [Test, OfMember("GetObjectData")]        
        public virtual void GetObjectData()
        {
            Type type = typeof(HsqlBatchUpdateException);
            FormatterConverter converter = new FormatterConverter();
            SerializationInfo info = new SerializationInfo(type, converter);
            StreamingContext context = new StreamingContext();
            HsqlBatchUpdateException exception = new HsqlBatchUpdateException(new int[] { 1, 1, 2, 1 });


            exception.GetObjectData(info, context);

            Assert.Fail("TODO");
        }
    }
}
