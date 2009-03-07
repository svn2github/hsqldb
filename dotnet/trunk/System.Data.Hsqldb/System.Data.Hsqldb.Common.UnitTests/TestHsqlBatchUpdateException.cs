using System;
using NUnit.Framework;
using System.Data.Hsqldb.TestCoverage;
using System.Runtime.Serialization;
using System.Runtime.InteropServices;

namespace System.Data.Hsqldb.Common.UnitTests
{
    [TestFixture, ForSubject(typeof(HsqlBatchUpdateException))]
    public class TestHsqlBatchUpdateException
    {
        [Test, OfMember("ErrorCode")]
        public void ErrorCode()
        {
            HsqlBatchUpdateException testSubect = new HsqlBatchUpdateException();

            Assert.AreEqual(HsqlBatchUpdateException.VendorCode, testSubect.ErrorCode);

            testSubect = new HsqlBatchUpdateException(new int[] { 1 });

            Assert.AreEqual(HsqlBatchUpdateException.VendorCode, testSubect.ErrorCode);

            testSubect = new HsqlBatchUpdateException("additional message.");

            Assert.AreEqual(HsqlBatchUpdateException.VendorCode, testSubect.ErrorCode);

            testSubect = new HsqlBatchUpdateException("additional message.", new InvalidOperationException());

            Assert.AreEqual(HsqlBatchUpdateException.VendorCode, testSubect.ErrorCode);

            testSubect = new HsqlBatchUpdateException("additional message.", new int[] { 1 });

            Assert.AreEqual(HsqlBatchUpdateException.VendorCode, testSubect.ErrorCode);
        }

        [Test, OfMember("GetObjectData")]        
        public virtual void GetObjectData()
        {
            int[] expected = new int[] { 1, 1, 2, 1 };
            Type type = typeof(HsqlBatchUpdateException);
            FormatterConverter converter = new FormatterConverter();
            SerializationInfo info = new SerializationInfo(type, converter);
            StreamingContext context = new StreamingContext();
            HsqlBatchUpdateException exception = new HsqlBatchUpdateException(expected);

            exception.GetObjectData(info, context);

            int[] actual = (int[]) info.GetValue("UpdateCounts", typeof(int[]));

            Assert.AreEqual(expected.Length, actual.Length);

            for (int i = 0; i < actual.Length; i++)
            {
                Assert.AreEqual(expected[i], actual[i]);
            }
        }

        [Test, OfMember("Message")]
        public void Message()
        {
            HsqlBatchUpdateException testSubect = new HsqlBatchUpdateException();
            string baseMessage = org.hsqldb.Trace.getMessage(
                -HsqlBatchUpdateException.VendorCode) + " - Batch update failed";

            Assert.AreEqual(baseMessage, testSubect.Message);

            testSubect = new HsqlBatchUpdateException(new int[] { 1 });

            Assert.AreEqual(baseMessage, testSubect.Message);

            testSubect = new HsqlBatchUpdateException("additional message.");

            Assert.AreEqual(baseMessage + " : additional message.", testSubect.Message);

            testSubect = new HsqlBatchUpdateException("additional message.", new InvalidOperationException());

            Assert.AreEqual(baseMessage + " : additional message.", testSubect.Message);

            testSubect = new HsqlBatchUpdateException("additional message.", new int[] { 1 });

            Assert.AreEqual(baseMessage + " : additional message.", testSubect.Message);
        }

        [Test, OfMember("SqlState")]
        public void SqlState()
        {
            HsqlBatchUpdateException testSubect = new HsqlBatchUpdateException();
            string state = org.hsqldb.Trace.error(HsqlBatchUpdateException.VendorCode).getSQLState();

            Assert.AreEqual(state, testSubect.SqlState);

            testSubect = new HsqlBatchUpdateException(new int[] { 1 });

            Assert.AreEqual(state, testSubect.SqlState);

            testSubect = new HsqlBatchUpdateException("additional message.");

            Assert.AreEqual(state, testSubect.SqlState);

            testSubect = new HsqlBatchUpdateException("additional message.", new InvalidOperationException());

            Assert.AreEqual(state, testSubect.SqlState);

            testSubect = new HsqlBatchUpdateException("additional message.", new int[] { 1 });

            Assert.AreEqual(state, testSubect.SqlState);
        }

        [Test, OfMember("UpdateCounts")]
        public void UpdateCounts()
        {
            HsqlBatchUpdateException testSubect = new HsqlBatchUpdateException();

            Assert.AreEqual(0, testSubect.UpdateCounts.Length);

            testSubect = new HsqlBatchUpdateException(new int[] { 1 });

            Assert.AreEqual(1, testSubect.UpdateCounts.Length);
            Assert.AreEqual(1, testSubect.UpdateCounts[0]);

            testSubect = new HsqlBatchUpdateException("additional message.");

            Assert.AreEqual(0, testSubect.UpdateCounts.Length);

            testSubect = new HsqlBatchUpdateException("additional message.", new InvalidOperationException());

            Assert.AreEqual(0, testSubect.UpdateCounts.Length);

            testSubect = new HsqlBatchUpdateException("additional message.", new int[] { 1 });

            Assert.AreEqual(1, testSubect.UpdateCounts.Length);
            Assert.AreEqual(1, testSubect.UpdateCounts[0]);
        }
    }
}
