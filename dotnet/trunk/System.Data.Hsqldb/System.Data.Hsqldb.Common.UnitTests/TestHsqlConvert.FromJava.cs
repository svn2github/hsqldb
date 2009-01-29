using System;
using TestCoverage;
using NUnit.Framework;

namespace System.Data.Hsqldb.Common.UnitTests
{
    [TestFixture()]
    [TestSubjectClassAttribute(TestSubject=typeof(System.Data.Hsqldb.Common.HsqlConvert.FromJava))]
    public class TestHsqlConvertFromJava
    {
        
        [TestSubjectMemberAttribute(MemeberName="ToBigInt")]
        [Test()]
        public virtual void ToBigInt()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            RecorderNumber nRecording = new RecorderNumber();
            nRecording.Recordings.doubleValueRecording.ReturnValue = "Please set the return value.";
            nRecording.Recordings.longValueRecording.ReturnValue = "Please set the return value.";

            TestSubject.ToBigInt(nRecording);

            // 
            // Write your assertions here.
            // 
            Assert.IsTrue(nRecording.Recordings.doubleValueRecording.Called);
            Assert.IsTrue(nRecording.Recordings.longValueRecording.Called);
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToBigInt")]
        [Test()]
        public virtual void ToBigInt()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            object o = new object();

            TestSubject.ToBigInt(o);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToBinary")]
        [Test()]
        public virtual void ToBinary()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            object o = new object();

            TestSubject.ToBinary(o);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToBoolean")]
        [Test()]
        public virtual void ToBoolean()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            RecorderNumber nRecording = new RecorderNumber();

            TestSubject.ToBoolean(nRecording);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToBoolean")]
        [Test()]
        public virtual void ToBoolean()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            object o = new object();

            TestSubject.ToBoolean(o);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToDate")]
        [Test()]
        public virtual void ToDate()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters

            // There is no default constuctor for the parameter javaTimeInMillis type Int64.
            long javaTimeInMillis;


            TestSubject.ToDate(javaTimeInMillis);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToDate")]
        [Test()]
        public virtual void ToDate()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            object o = new object();

            TestSubject.ToDate(o);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToDecimal")]
        [Test()]
        public virtual void ToDecimal()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            RecorderNumber nRecording = new RecorderNumber();

            TestSubject.ToDecimal(nRecording);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToDecimal")]
        [Test()]
        public virtual void ToDecimal()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters

            // There is no default constuctor for the parameter bigDecimalValue type BigDecimal.
            java.math.BigDecimal bigDecimalValue;


            TestSubject.ToDecimal(bigDecimalValue);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToDecimal")]
        [Test()]
        public virtual void ToDecimal()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            object o = new object();

            TestSubject.ToDecimal(o);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToDouble")]
        [Test()]
        public virtual void ToDouble()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            RecorderNumber nRecording = new RecorderNumber();
            nRecording.Recordings.doubleValueRecording.ReturnValue = "Please set the return value.";

            TestSubject.ToDouble(nRecording);

            // 
            // Write your assertions here.
            // 
            Assert.IsTrue(nRecording.Recordings.doubleValueRecording.Called);
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToDouble")]
        [Test()]
        public virtual void ToDouble()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            object o = new object();

            TestSubject.ToDouble(o);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToGuid")]
        [Test()]
        public virtual void ToGuid()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters

            // There is no default constuctor for the parameter uuid type UUID.
            java.util.UUID uuid;


            TestSubject.ToGuid(uuid);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToInteger")]
        [Test()]
        public virtual void ToInteger()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            RecorderNumber nRecording = new RecorderNumber();
            nRecording.Recordings.intValueRecording.ReturnValue = "Please set the return value.";
            nRecording.Recordings.longValueRecording.ReturnValue = "Please set the return value.";
            nRecording.Recordings.doubleValueRecording.ReturnValue = "Please set the return value.";
            nRecording.Recordings.longValueRecording.ReturnValue = "Please set the return value.";

            TestSubject.ToInteger(nRecording);

            // 
            // Write your assertions here.
            // 
            Assert.IsTrue(nRecording.Recordings.intValueRecording.Called);
            Assert.IsTrue(nRecording.Recordings.longValueRecording.Called);
            Assert.IsTrue(nRecording.Recordings.doubleValueRecording.Called);
            Assert.IsTrue(nRecording.Recordings.longValueRecording.Called);
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToInteger")]
        [Test()]
        public virtual void ToInteger()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            object o = new object();

            TestSubject.ToInteger(o);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToObject")]
        [Test()]
        public virtual void ToObject()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            object objectValue = new object();

            // There is no default constuctor for the parameter type type Int32.
            int type;


            TestSubject.ToObject(objectValue, type);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToReal")]
        [Test()]
        public virtual void ToReal()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            RecorderNumber nRecording = new RecorderNumber();
            nRecording.Recordings.doubleValueRecording.ReturnValue = "Please set the return value.";

            TestSubject.ToReal(nRecording);

            // 
            // Write your assertions here.
            // 
            Assert.IsTrue(nRecording.Recordings.doubleValueRecording.Called);
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToReal")]
        [Test()]
        public virtual void ToReal()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            object o = new object();

            TestSubject.ToReal(o);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToSmallInt")]
        [Test()]
        public virtual void ToSmallInt()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            RecorderNumber nRecording = new RecorderNumber();
            nRecording.Recordings.intValueRecording.ReturnValue = "Please set the return value.";
            nRecording.Recordings.intValueRecording.ReturnValue = "Please set the return value.";
            nRecording.Recordings.longValueRecording.ReturnValue = "Please set the return value.";
            nRecording.Recordings.doubleValueRecording.ReturnValue = "Please set the return value.";
            nRecording.Recordings.longValueRecording.ReturnValue = "Please set the return value.";

            TestSubject.ToSmallInt(nRecording);

            // 
            // Write your assertions here.
            // 
            Assert.IsTrue(nRecording.Recordings.intValueRecording.Called);
            Assert.IsTrue(nRecording.Recordings.intValueRecording.Called);
            Assert.IsTrue(nRecording.Recordings.longValueRecording.Called);
            Assert.IsTrue(nRecording.Recordings.doubleValueRecording.Called);
            Assert.IsTrue(nRecording.Recordings.longValueRecording.Called);
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToSmallInt")]
        [Test()]
        public virtual void ToSmallInt()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            object o = new object();

            TestSubject.ToSmallInt(o);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToString")]
        [Test()]
        public virtual void ToString()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            object o = new object();

            TestSubject.ToString(o);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToTime")]
        [Test()]
        public virtual void ToTime()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters

            // There is no default constuctor for the parameter javaTimeInMillis type Int64.
            long javaTimeInMillis;


            TestSubject.ToTime(javaTimeInMillis);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToTime")]
        [Test()]
        public virtual void ToTime()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            object o = new object();

            TestSubject.ToTime(o);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToTimestamp")]
        [Test()]
        public virtual void ToTimestamp()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters

            // There is no default constuctor for the parameter javaTimeInMillis type Int64.
            long javaTimeInMillis;


            TestSubject.ToTimestamp(javaTimeInMillis);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToTimestamp")]
        [Test()]
        public virtual void ToTimestamp()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            object o = new object();

            TestSubject.ToTimestamp(o);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToTinyInt")]
        [Test()]
        public virtual void ToTinyInt()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            RecorderNumber nRecording = new RecorderNumber();
            nRecording.Recordings.intValueRecording.ReturnValue = "Please set the return value.";
            nRecording.Recordings.intValueRecording.ReturnValue = "Please set the return value.";
            nRecording.Recordings.longValueRecording.ReturnValue = "Please set the return value.";
            nRecording.Recordings.doubleValueRecording.ReturnValue = "Please set the return value.";
            nRecording.Recordings.longValueRecording.ReturnValue = "Please set the return value.";

            TestSubject.ToTinyInt(nRecording);

            // 
            // Write your assertions here.
            // 
            Assert.IsTrue(nRecording.Recordings.intValueRecording.Called);
            Assert.IsTrue(nRecording.Recordings.intValueRecording.Called);
            Assert.IsTrue(nRecording.Recordings.longValueRecording.Called);
            Assert.IsTrue(nRecording.Recordings.doubleValueRecording.Called);
            Assert.IsTrue(nRecording.Recordings.longValueRecording.Called);
        }
        
        [TestSubjectMemberAttribute(MemeberName="ToTinyInt")]
        [Test()]
        public virtual void ToTinyInt()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            object o = new object();

            TestSubject.ToTinyInt(o);

            // 
            // Write your assertions here.
            // 
        }
        
        [TestSubjectMemberAttribute(MemeberName="UnWrap")]
        [Test()]
        public virtual void UnWrap()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters

            // There is no default constuctor for the parameter javaObject type JavaObject.
            org.hsqldb.types.JavaObject javaObject;

        }
        
        [TestSubjectMemberAttribute(MemeberName="UnWrap")]
        [Test()]
        public virtual void UnWrap()
        {

            // No public constructors were found, you need to get an instance of the test subject assigned to the variable TestSubject.
            System.Data.Hsqldb.Common.HsqlConvert.FromJava TestSubject;

            // Create Test Method Parameters
            object sourceObject = new object();
        }
    }
}
