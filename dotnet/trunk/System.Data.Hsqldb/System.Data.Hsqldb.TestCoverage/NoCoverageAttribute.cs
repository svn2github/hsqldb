using System;
using System.Collections.Generic;
using System.Text;

namespace System.Data.Hsqldb.TestCoverage
{
    [AttributeUsage(AttributeTargets.All)]
    [NoCoverage]
    public class NoCoverageAttribute : Attribute { }
}
