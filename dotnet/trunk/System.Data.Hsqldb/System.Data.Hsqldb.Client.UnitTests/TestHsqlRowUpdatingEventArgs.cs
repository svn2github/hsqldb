#region Using
using System;
using System.Data.Common;
using System.Data.Hsqldb.TestCoverage;
using NUnit.Framework; 
#endregion

namespace System.Data.Hsqldb.Client.UnitTests
{
    [TestFixture, ForSubject(typeof(HsqlRowUpdatingEventArgs))]
    public class TestHsqlRowUpdatingEventArgs
    {
        [Test, OfMember("Command")]
        public void Command() {
            HsqlRowUpdatingEventArgs testSubject = new HsqlRowUpdatingEventArgs(
                null, null, StatementType.Update, new DataTableMapping());

            Assert.Fail("TODO");
        }
    }
}
