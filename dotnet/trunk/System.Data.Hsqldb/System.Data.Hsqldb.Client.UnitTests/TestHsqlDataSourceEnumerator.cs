#region Using
using System;
using System.Data.Hsqldb.TestCoverage;
using NUnit.Framework; 
#endregion

namespace System.Data.Hsqldb.Client.UnitTests
{    
    [TestFixture, ForSubject(typeof(HsqlDataSourceEnumerator))]
    public class TestHsqlDataSourceEnumerator
    {
        static HsqlDataSourceEnumerator TestSubject
        {
            get { return HsqlDataSourceEnumerator.Instance; }
        }
        
        [Test, OfMember("GetDataSources")]
        public void GetDataSources()
        {

            DataTable actual = TestSubject.GetDataSources();

            Assert.Fail("TODO");
        }
    }
}
