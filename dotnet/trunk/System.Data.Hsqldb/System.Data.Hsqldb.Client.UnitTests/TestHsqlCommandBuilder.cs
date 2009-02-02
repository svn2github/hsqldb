using System;
using NUnit.Framework;
using System.Data.Hsqldb.TestCoverage;
using System.Data.Hsqldb.Client;

namespace System.Data.Hsqldb.Client.UnitTests
{
    [TestFixture, ForSubject(typeof(HsqlCommandBuilder))]
    public class TestHsqlCommandBuilder
    {
        [Test, OfMember("DeriveParameters")]
        public virtual void DeriveParameters()
        {
            //HsqlCommand command = 
            //HsqlCommandBuilder.DeriveParameters(command);

            // 
            Assert.Fail("TODO");
            // 
        }
        
        [Test, OfMember("GetDeleteCommand")]
        public virtual void GetDeleteCommand()
        {
            HsqlCommandBuilder testSubject = new HsqlCommandBuilder();
            testSubject.DataAdapter = new HsqlDataAdapter(
                "select * from information_schema.system_tables",
                "Protocol=Mem;Path=Test;User Id=SA;");
            bool useColumnsForParameterNames = true;

            HsqlCommand command = testSubject.GetDeleteCommand(useColumnsForParameterNames);

            Assert.Fail("TODO");
        }
        
        [Test, OfMember("GetInsertCommand")]
        public virtual void GetInsertCommand()
        {
            HsqlCommandBuilder testSubject = new HsqlCommandBuilder();
            testSubject.DataAdapter = new HsqlDataAdapter(
                "select * from information_schema.system_tables",
                "Protocol=Mem;Path=Test;User Id=SA;");
            bool useColumnsForParameterNames = true;

            HsqlCommand command = testSubject.GetInsertCommand(useColumnsForParameterNames);

            Assert.Fail("TODO");
        }
        
        [Test, OfMember("GetUpdateCommand")]
        public virtual void GetUpdateCommand()
        {
            HsqlCommandBuilder testSubject = new HsqlCommandBuilder();
            testSubject.DataAdapter = new HsqlDataAdapter(
                "select * from information_schema.system_tables", 
                "Protocol=Mem;Path=Test;User Id=SA;");
            bool useColumnsForParameterNames = true;

            HsqlCommand command = testSubject.GetUpdateCommand(useColumnsForParameterNames);

            Assert.Fail("TODO");
        }
        
        [Test, OfMember("QuoteIdentifier")]
        public virtual void QuoteIdentifier()
        {
            HsqlCommandBuilder testSubject = new HsqlCommandBuilder();
            string unquotedIdentifier = "foo";

            string expected = "\"foo\"";
            string actual = testSubject.QuoteIdentifier(unquotedIdentifier);

            Assert.AreEqual(expected, actual);
        }
        
        [Test, OfMember("UnquoteIdentifier")]
        public virtual void UnquoteIdentifier()
        {
            HsqlCommandBuilder testSubject = new HsqlCommandBuilder();
            string quotedIdentifier = "\"foo\"";

            string expected = "foo";
            string actual = testSubject.UnquoteIdentifier(quotedIdentifier);

            Assert.AreEqual(expected, actual);
        }
    }
}
