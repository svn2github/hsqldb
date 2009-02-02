using System;

namespace System.Data.Hsqldb.TestCoverage
{
    [AttributeUsage(AttributeTargets.Class, AllowMultiple=false, Inherited=true)]
    public class ForSubjectAttribute : Attribute
    {
        private Type m_testSubject;

        public ForSubjectAttribute()
        {
        }

        public ForSubjectAttribute(Type testSubject)
        {
            m_testSubject = testSubject;
        }

        public Type TestSubject
        {
            get
            {
                return m_testSubject;
            }
            set
            {
                m_testSubject = value;
            }
        }
    }
}

