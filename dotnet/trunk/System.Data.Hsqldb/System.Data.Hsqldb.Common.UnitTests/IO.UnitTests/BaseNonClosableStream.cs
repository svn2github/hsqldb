using System;
using System.Collections.Generic;
using System.Text;

namespace System.Data.Hsqldb.Common.IO.UnitTests
{
    internal class BaseNonClosableStream : BaseStreamAdapter
    {
        public override void Close()
        {
            throw new Exception("Method or operation not implemented");
        }
    }
}
