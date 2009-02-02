using System;
using System.Reflection;

namespace System.Data.Hsqldb.TestCoverage
{
    [AttributeUsage(AttributeTargets.Method, AllowMultiple=false, Inherited=true)]
    public class OfMemberAttribute : Attribute
    {
        private string m_memberName;

        public OfMemberAttribute()
        {
        }

        public OfMemberAttribute(string memberName)
        {
            m_memberName = memberName;
        }

        public string MemberName
        {
            get
            {
                return m_memberName;
            }
            set
            {
                m_memberName = value;
            }
        }
    }
}

