using System;
using System.Reflection;

namespace System.Data.Hsqldb.TestCoverage
{
    /// <summary>
    /// Indicates the name of the test subject member being tested within the method of the test fixture.
    /// </summary>
    [AttributeUsage(AttributeTargets.Method, AllowMultiple=true, Inherited=true)]
    [NoCoverage]
    public class OfMemberAttribute : Attribute
    {
        private string m_memberName;

        /// <summary>
        /// Constructs a new instance for which the name of the member is null.
        /// </summary>
        public OfMemberAttribute()
        {
        }

        /// <summary>
        /// Constructs a new instance with the given member name.
        /// </summary>
        /// <param name="memberName">Name of the member.</param>
        public OfMemberAttribute(string memberName)
        {
            m_memberName = memberName;
        }

        /// <summary>
        /// Gets or sets the name of the member.
        /// </summary>
        /// <value>
        /// the name of the subject member being tested within a test fixture
        /// </value>
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

