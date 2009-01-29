using System;
using TestCoverage;
using NUnit.Framework;

namespace System.Data.Hsqldb.Common.Lob.UnitTests
{
    [TestFixture()]
    [TestSubjectClassAttribute(TestSubject=typeof(System.Data.Hsqldb.Common.Lob.JdbcClob))]
    public class TestJdbcClob
    {

        [TestSubjectMemberAttribute(MemeberName = "Free")]
        [Test()]
        public virtual void Free()
        {
        }

        [TestSubjectMemberAttribute(MemeberName = "GetAsciiStream")]
        [Test()]
        public virtual void GetAsciiStream()
        {
        }

        [TestSubjectMemberAttribute(MemeberName = "GetCharacterStream")]
        [Test()]
        public virtual void GetCharacterStream()
        {
        }

        [TestSubjectMemberAttribute(MemeberName = "GetSubString")]
        [Test()]
        public virtual void GetSubString()
        {
        }

        [TestSubjectMemberAttribute(MemeberName = "Position")]
        [Test()]
        public virtual void Position_T1()
        {
        }

        [TestSubjectMemberAttribute(MemeberName = "SetAsciiStream")]
        [Test()]
        public virtual void SetAsciiStream()
        {
        }

        [TestSubjectMemberAttribute(MemeberName = "SetCharacterStream")]
        [Test()]
        public virtual void SetCharacterStream()
        {
        }

        [TestSubjectMemberAttribute(MemeberName = "SetString")]
        [Test()]
        public virtual void SetString()
        {
        }

        [TestSubjectMemberAttribute(MemeberName = "Truncate")]
        [Test()]
        public virtual void Truncate()
        {
        }

        [TestSubjectMemberAttribute(MemeberName = "UnWrap")]
        [Test()]
        public virtual void UnWrap()
        {
        }

        [TestSubjectMemberAttribute(MemeberName = "Wrap")]
        [Test()]
        public virtual void Wrap()
        {
        }
    }
}
