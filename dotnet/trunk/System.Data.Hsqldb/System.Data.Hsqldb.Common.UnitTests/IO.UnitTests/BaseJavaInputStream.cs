using System;
using System.Collections.Generic;
using System.Text;

namespace System.Data.Hsqldb.Common.IO.UnitTests
{
    internal class BaseJavaInputStream : java.io.InputStream
    {        
        public override int read()
        {
            throw new java.io.IOException("The method or operation is not implemented.");
        }

        public override long skip(long n)
        {
            throw new java.io.IOException("The method or operation is not implemented.");
        }

        public override void close()
        {
            throw new java.io.IOException("The method or operation is not implemented.");
        }
    }
}
