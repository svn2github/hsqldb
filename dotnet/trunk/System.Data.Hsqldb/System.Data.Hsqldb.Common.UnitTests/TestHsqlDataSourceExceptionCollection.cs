using System;
using NUnit.Framework;
using System.Data.Hsqldb.TestCoverage;
using System.Collections;

namespace System.Data.Hsqldb.Common.UnitTests
{
    [TestFixture, ForSubject(typeof(HsqlDataSourceExceptionCollection))]
    public class TestHsqlDataSourceExceptionCollection
    {
        HsqlDataSourceExceptionCollection NewTestSubject()
        {
            return new HsqlDataSourceException().Exceptions;
        }

        [Test, OfMember("CopyTo")]
        public void CopyTo()
        {
            HsqlDataSourceExceptionCollection testSubject = NewTestSubject();

            testSubject.CopyTo(new object[0], 0);
        }
        
        [Test, OfMember("GetEnumerator")]
        public void GetEnumerator()
        {
            HsqlDataSourceExceptionCollection testSubject = NewTestSubject();

            IEnumerator enumerator = testSubject.GetEnumerator();
        }
    }
}
