using System;
using NUnit.Framework;
using System.Collections;

namespace System.Data.Hsqldb.Common.IO.UnitTests
{
    public class IOTest
    {
        [Suite]
        public static IEnumerable Suite
        {
            get
            {
                return new Type[]
                {
                    typeof(JavaInputStreamAdapter),
                    typeof(JavaInputStreamWrapper),
                    typeof(JavaOutputStreamAdapter),
                    typeof(JavaOutputStreamWrapper),
                    typeof(JavaReaderWrapper),
                    typeof(JavaWriterWrapper)
                };
            }
        }
    }
}
