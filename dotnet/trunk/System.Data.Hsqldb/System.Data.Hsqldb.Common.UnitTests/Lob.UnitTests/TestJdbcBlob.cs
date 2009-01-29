using System;
using TestCoverage;
using NUnit.Framework;

namespace System.Data.Hsqldb.Common.Lob.UnitTests
{
    [TestFixture()]
    [TestSubjectClassAttribute(TestSubject=typeof(System.Data.Hsqldb.Common.Lob.JdbcBlob))]
    public class TestJdbcBlob
    {

        [TestSubjectMemberAttribute(MemeberName = "Free")]
        [Test()]
        public virtual void Free()
        {
        }

        [TestSubjectMemberAttribute(MemeberName = "GetBinaryStream")]
        [Test()]
        public virtual void GetBinaryStream()
        {
        }

        [TestSubjectMemberAttribute(MemeberName = "GetBytes")]
        [Test()]
        public virtual void GetBytes()
        {
        }

        [TestSubjectMemberAttribute(MemeberName = "Position")]
        [Test()]
        public virtual void Position_T1()
        {
        }

        [TestSubjectMemberAttribute(MemeberName = "Position")]
        [Test()]
        public virtual void Position_T2()
        {
        }

        [TestSubjectMemberAttribute(MemeberName = "SetBinaryStream")]
        [Test()]
        public virtual void SetBinaryStream()
        {
        }

        [TestSubjectMemberAttribute(MemeberName = "SetBytes")]
        [Test()]
        public virtual void SetBytes_T1()
        {
        }

        [TestSubjectMemberAttribute(MemeberName = "SetBytes")]
        [Test()]
        public virtual void SetBytes_T2()
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
