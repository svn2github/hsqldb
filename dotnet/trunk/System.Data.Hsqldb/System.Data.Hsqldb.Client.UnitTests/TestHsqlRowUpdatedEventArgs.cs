#region Using
using System;
using System.Data.Common; 
using System.Data.Hsqldb.TestCoverage;
using NUnit.Framework;
#endregion

namespace System.Data.Hsqldb.Client.UnitTests
{
    [TestFixture, ForSubject(typeof(HsqlRowUpdatedEventArgs))]
    public class TestHsqlRowUpdatedEventArgs
    {
        [Test, OfMember("Command")]
        public void Command()
        {
            HsqlRowUpdatedEventArgs testSubject = new HsqlRowUpdatedEventArgs(
                null, null, StatementType.Update, new DataTableMapping());

            Assert.Fail("TODO");
        }
    }
}
