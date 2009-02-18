using System;
using System.Reflection;

namespace System.Data.Hsqldb.TestCoverage
{
    /// <summary>
    /// Indicates the assembly containing the subject type being tested within the test fixture.
    /// </summary>
    [AttributeUsage(AttributeTargets.Assembly, AllowMultiple=false, Inherited=true)]
    public class SubjectAssemblyAttribute : Attribute
    {
        private string m_fullName;

        /// <summary>
        /// Constructs a new instance for which the test subject assembly is null.
        /// </summary>
        public SubjectAssemblyAttribute()
        {
        }

        /// <summary>
        /// Constructs a new instance with the given full name of the assembly containing the subject.
        /// </summary>
        /// <param name="fullName">The full name.</param>
        public SubjectAssemblyAttribute(string fullName)
        {
            m_fullName = fullName;
        }


        /// <summary>
        /// Gets or sets the full name.
        /// </summary>
        /// <value>
        /// The full name of the assembly containing the subject type being tested within the test fixture
        /// </value>
        public string FullName
        {
            get
            {
                return m_fullName;
            }
            set
            {
                m_fullName = value;
            }
        }
    }
}

