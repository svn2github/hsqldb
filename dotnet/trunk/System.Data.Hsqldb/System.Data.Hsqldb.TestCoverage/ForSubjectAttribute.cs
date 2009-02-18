using System;

namespace System.Data.Hsqldb.TestCoverage
{
    /// <summary>
    /// Indicates the subject type being tested within the test fixture.
    /// </summary>
    [AttributeUsage(AttributeTargets.Class, AllowMultiple=false, Inherited=true)]
    public class ForSubjectAttribute : Attribute
    {
        private Type m_testSubject;

        /// <summary>
        /// Constructs a new instance for which the test subject type is null.
        /// </summary>
        public ForSubjectAttribute()
        {
        }

        /// <summary>
        /// Constructs a new instance with the given test subject type.
        /// </summary>
        /// <param name="testSubject">The test subject.</param>
        public ForSubjectAttribute(Type testSubject)
        {
            m_testSubject = testSubject;
        }


        /// <summary>
        /// Gets or sets the test subject.
        /// </summary>
        /// <value>the type of subject being tested within a test fixture</value>
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

