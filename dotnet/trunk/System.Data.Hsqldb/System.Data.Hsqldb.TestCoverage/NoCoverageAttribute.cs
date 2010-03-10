using System;
using System.Collections.Generic;
using System.Text;

namespace System.Data.Hsqldb.TestCoverage
{
    /// <summary>
    /// 
    /// </summary>
    [AttributeUsage(AttributeTargets.All)]
    [NoCoverage]
    public class NoCoverageAttribute : Attribute { }
}
