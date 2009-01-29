using System;
using TestCoverage;
using NUnit.Framework;

namespace System.Data.Hsqldb.Common.UnitTests
{
    [TestFixture()]
    [TestSubjectClassAttribute(TestSubject=typeof(System.Data.Hsqldb.Common.HsqlConvert))]
    public class TestHsqlConvert
    {
        
        [TestSubjectMemberAttribute(MemeberName="InvalidConversion")]
        [Test()]
        public virtual void InvalidConversion()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert TestSubject;

            // Create Test Method Parameters

            // There is no default constuctor for the parameter type type Int32.
            int type;


            TestSubject.InvalidConversion(type);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="NumericValueOutOfRange")]
        [Test()]
        public virtual void NumericValueOutOfRange()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert TestSubject;

            // Create Test Method Parameters
            object n = new object();

            TestSubject.NumericValueOutOfRange(n);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ParseBigInt")]
        [Test()]
        public virtual void ParseBigInt()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert TestSubject;

            // Create Test Method Parameters

            // There is no default constuctor for the parameter value type String.
            string value;


            TestSubject.ParseBigInt(value);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ParseDecimal")]
        [Test()]
        public virtual void ParseDecimal()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert TestSubject;

            // Create Test Method Parameters

            // There is no default constuctor for the parameter value type String.
            string value;


            TestSubject.ParseDecimal(value);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ParseDouble")]
        [Test()]
        public virtual void ParseDouble()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert TestSubject;

            // Create Test Method Parameters

            // There is no default constuctor for the parameter value type String.
            string value;


            TestSubject.ParseDouble(value);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ParseInteger")]
        [Test()]
        public virtual void ParseInteger()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert TestSubject;

            // Create Test Method Parameters

            // There is no default constuctor for the parameter value type String.
            string value;


            TestSubject.ParseInteger(value);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToDataType")]
        [Test()]
        public virtual void ToDataType()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert TestSubject;

            // Create Test Method Parameters

            // There is no default constuctor for the parameter dbType type HsqlProviderType.
            System.Data.Hsqldb.Common.Enumeration.HsqlProviderType dbType;


            TestSubject.ToDataType(dbType);

            // 
            // Write your assertions here.
            //

            int type;


            TestSubject.ToDataType(type);

            // 
            // Write your assertions here.
            //
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToDbType")]
        [Test()]
        public virtual void ToDbType()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert TestSubject;

            // Create Test Method Parameters

            // There is no default constuctor for the parameter type type HsqlProviderType.
            System.Data.Hsqldb.Common.Enumeration.HsqlProviderType type;


            TestSubject.ToDbType(type);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToHsqlIsolationLevel")]
        [Test()]
        public virtual void ToHsqlIsolationLevel()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert TestSubject;

            // Create Test Method Parameters

            // There is no default constuctor for the parameter isolationLevel type IsolationLevel.
            System.Data.IsolationLevel isolationLevel;


            TestSubject.ToHsqlIsolationLevel(isolationLevel);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToHsqlProviderType")]
        [Test()]
        public virtual void ToHsqlProviderType()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert TestSubject;

            // Create Test Method Parameters

            // There is no default constuctor for the parameter type type DbType.
            System.Data.DbType type;


            TestSubject.ToHsqlProviderType(type);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToIsolationLevel")]
        [Test()]
        public virtual void ToIsolationLevel()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert TestSubject;

            // Create Test Method Parameters

            // There is no default constuctor for the parameter value type HsqlIsolationLevel.
            System.Data.Hsqldb.Common.Enumeration.HsqlIsolationLevel value;


            TestSubject.ToIsolationLevel(value);

            // 
            // Write your assertions here.
            // 

            System.Transactions.IsolationLevel value;


            TestSubject.ToIsolationLevel(value);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToParameterDirection")]
        [Test()]
        public virtual void ToParameterDirection()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert TestSubject;

            // Create Test Method Parameters

            // There is no default constuctor for the parameter mode type ParameterMode.
            System.Data.Hsqldb.Common.Enumeration.ParameterMode mode;


            TestSubject.ToParameterDirection(mode);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToProviderSpecificDataType")]
        [Test()]
        public virtual void ToProviderSpecificDataType()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert TestSubject;

            // Create Test Method Parameters

            // There is no default constuctor for the parameter type type Int32.
            int type;


            TestSubject.ToProviderSpecificDataType(type);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToSqlLiteral")]
        [Test()]
        public virtual void ToSqlLiteral()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert TestSubject;

            // Create Test Method Parameters
            RecorderIDataParameter parameterRecording = new RecorderIDataParameter();

            TestSubject.ToSqlLiteral(parameterRecording);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="UnknownConversion")]
        [Test()]
        public virtual void UnknownConversion()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert TestSubject;

            // Create Test Method Parameters
            object o = new object();

            // There is no default constuctor for the parameter targetType type Int32.
            int targetType;


            TestSubject.UnknownConversion(o, targetType);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="WrongDataType")]
        [Test()]
        public virtual void WrongDataType()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert TestSubject;

            // Create Test Method Parameters
            object o = new object();

            TestSubject.WrongDataType(o);

            // 
            // Write your assertions here.
            // 
        }
    }
}
