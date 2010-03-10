using System;
using System.Data.Hsqldb.TestCoverage;
using System.Runtime.Serialization;
using NUnit.Framework;

namespace System.Data.Hsqldb.Common.UnitTests
{
    [TestFixture, ForSubject(typeof(HsqlDataSourceException))]
    public class TestHsqlDataSourceException
    {
        
        [Test, OfMember("GetObjectData")]
        public void GetObjectData()
        {
            Type type = typeof(HsqlDataSourceException);
            FormatterConverter converter = new FormatterConverter();
            SerializationInfo info = new SerializationInfo(type, converter);
            StreamingContext context = new StreamingContext();

            HsqlDataSourceException testSubject = new HsqlDataSourceException("foo", -1, "42001");

            testSubject.GetObjectData(info, context);

            Console.WriteLine("Member Count: {0}", info.MemberCount);
            
            foreach(SerializationEntry item in info)
            {
                Console.WriteLine("Name: {0},  Type: {1},  Value: {2}", item.Name, item.ObjectType, item.Value);

                if (item.Name == "m_code")
                {
                    Assert.AreEqual(-1, item.Value);
                }
                else if (item.Name == "m_state")
                {
                    Assert.AreEqual("42001", item.Value);
                }
                else if (item.Name == "Message")
                {
                    Assert.AreEqual("foo", item.Value);
                }
            }


            //Assert.Fail("TODO");
        }
    }
}
