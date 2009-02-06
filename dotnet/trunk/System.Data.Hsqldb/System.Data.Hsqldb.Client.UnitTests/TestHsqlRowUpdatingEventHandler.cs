#region Using
using System;
using System.Data.Hsqldb.TestCoverage;
using NUnit.Framework; 
#endregion

namespace System.Data.Hsqldb.Client.UnitTests
{
    [TestFixture, ForSubject(typeof(HsqlRowUpdatingEventHandler))]
    public class TestHsqlRowUpdatingEventHandler
    {
        
        [Test, OfMember("BeginInvoke")]
        public void BeginInvoke()
        {
        }

        [Test, OfMember("EndInvoke")]
        public virtual void EndInvoke()
        {
        }

        [Test, OfMember("Invoke")]
        public virtual void Invoke()
        {
        }
    }
}
