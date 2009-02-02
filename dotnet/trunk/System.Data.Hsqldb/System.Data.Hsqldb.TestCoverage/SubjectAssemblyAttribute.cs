using System;

namespace System.Data.Hsqldb.TestCoverage
{
    [AttributeUsage(AttributeTargets.Assembly, AllowMultiple=false, Inherited=true)]
    public class SubjectAssemblyAttribute : Attribute
    {
        private string m_location;

        public SubjectAssemblyAttribute()
        {
        }

        public SubjectAssemblyAttribute(string location)
        {
            m_location = location;
        }

        public string Location
        {
            get
            {
                return m_location;
            }
            set
            {
                m_location = value;
            }
        }
    }
}

