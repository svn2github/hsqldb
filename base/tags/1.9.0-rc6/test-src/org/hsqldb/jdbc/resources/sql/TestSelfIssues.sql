--
-- TestSelfIssues.txt
--
-- Tests demonstrating remaining issues
--
-- bug #901313 - fixed - subselect results in error when it returns no rows
-- corrected for 1.8.0 RC10 - scalar subquery should return null if no rows are returned
drop table table2 if exists;
drop table table1 if exists;
create table table1 (col1 integer not null, col2 integer not null);
create table table2 (col1 integer, col2 integer);
insert into table1 (col1, col2) values (1, 1)
insert into table1 (col1, col2) values (2, 2)
insert into table2 (col1, col2) values (1, 3)
insert into table2 (col1, col2) values (2, 4)
/*c2*/select t1.col1, t1.col2, t2.col2 from table1 t1, table2 t2 where t1.col1 = t2.col1
/*u2*/update table2 set col2=(select table1.col2 from table1 where table1.col1 = table2.col1)
/*c2*/select t1.col1, t1.col2, t2.col2 from table1 t1, table2 t2 where t1.col1 = t2.col1
/*u1*/insert into table2 (col1, col2) values (null, 5)
/*u3*/update table2 set col2=(select table1.col2 from table1 where table1.col1 = table2.col1)
-- support for aliases in UPDATE has been added
/*u3*/update table2 b set col2 = (select a.col2 from table1 a where a.col1 = b.col1)
/*c2*/select t1.col1, t1.col2, t2.col2 from table1 t1, table2 t2 where t1.col1 = t2.col1
-- query returns only rows for which the correlated subquery returns a value
/*c3*/select table2.col2,(select table1.col2 from table1 where table1.col1 = table2.col1) from table2
-- add row so that subquery returns two rows
/*u1*/insert into table1 (col1, col2) values (1, 6)
/*e*/select table2.col2,(select table1.col2 from table1 where table1.col1 = table2.col1) from table2

-- reported bug - fixed - first bigint in value list was ignored
DROP TABLE T IF EXISTS;
CREATE TABLE T(A BIGINT);
INSERT INTO T VALUES(7223955391172290801);
INSERT INTO T VALUES(8124225344737252039);
INSERT INTO T VALUES(8112873400132257948);
INSERT INTO T VALUES(100);
/*c4*/SELECT * FROM T WHERE A IN
 (7223955391172290801,8124225344737252039,8112873400132257948,100);
-- check with different types
/*c4*/SELECT * FROM T WHERE A IN
 (7223955391172290801,8124225344737252039,8112873400132257948,100.00E0);

-- reported bug - fixed - TRUE and FALSE in conditions
/*c4*/SELECT * FROM T WHERE A IN
 (7223955391172290801,8124225344737252039,8112873400132257948,100) AND TRUE;

/*c0*/SELECT * FROM T WHERE A IN
 (7223955391172290801,8124225344737252039,8112873400132257948,100) AND FALSE;

-- demonstration of contatenation from Blaine
CREATE TABLE tsttbl(
    id IDENTITY,
    FirstName VARCHAR(10),
    MiddleName VARCHAR(10),
    LastName VARCHAR(10),
    CommonLink VARCHAR(20)
 );
INSERT INTO tsttbl(firstname, middlename, lastname, commonlink)
 VALUES ('John', 'Paul', 'Jones', 'http://cnn.com');
INSERT INTO tsttbl(firstname, middlename, lastname, commonlink)
 VALUES ('Bridget', 'Paula', 'Murphy', 'http://cnn.com');
--  Raw SELECT using ||:
SELECT FirstName || MiddleName || LastName, CommonLink
 FROM tsttbl;
--  Creating View using ||:
CREATE VIEW tstview AS SELECT
    FirstName || MiddleName || LastName, CommonLink FROM tsttbl;
-- Executing View Query:
SELECT * FROM tstview;
--  Raw SELECT using +:
SELECT FirstName + ' ' + MiddleName + ' ' + LastName, CommonLink
 FROM tsttbl;

-- reported bug #1487591
CREATE TABLE TESTTABLE(ID IDENTITY, MY_VALUE VARCHAR(10), MY_ROW_TYPE VARCHAR(10));
INSERT INTO TESTTABLE (MY_VALUE,MY_ROW_TYPE) VALUES ('A', 'B');
SELECT ID, MY_VALUE FROM TESTTABLE
 AS TBL1 WHERE ID = (SELECT MAX(ID) AS ID FROM TESTTABLE
 AS TBL2 WHERE TBL1.MY_VALUE=TBL2.MY_VALUE);

-- reported bug

drop table PROCESSDETAIL if exists;

create table PROCESSDETAIL (
        ID bigint not null,
        ALERTLEVEL integer not null,
        VALUE varchar(255) not null,
        TSTAMP timestamp(0) not null,
        ATTRIBUTEKEY integer not null,
        PROCESSID bigint not null,
        primary key (ID) );


insert into
        PROCESSDETAIL
        (ALERTLEVEL, VALUE, TSTAMP, ATTRIBUTEKEY,
         PROCESSID, ID)
 values(1, 66, '2007-01-01 08:00:00', 28, 9, 1);

insert into
        PROCESSDETAIL
        (ALERTLEVEL, VALUE, TSTAMP, ATTRIBUTEKEY,
         PROCESSID, ID)
 values(0, 67, '2007-01-01 07:59:40', 28, 9, 2);

/*r
 1,1,66,2007-01-01 08:00:00,28,9
*/select *  from
        PROCESSDETAIL t1
    where exists (
            select
                t2.PROCESSID,
                t2.ATTRIBUTEKEY
            from
                PROCESSDETAIL t2
            where
                t2.PROCESSID=t1.PROCESSID
                and t2.ATTRIBUTEKEY=t1.ATTRIBUTEKEY
                and t2.PROCESSID=9
                and t2.TSTAMP<='2007-01-01 08:00:00'
            group by
                t2.PROCESSID ,
                t2.ATTRIBUTEKEY
            having  max(t2.TSTAMP)=t1.TSTAMP );
--
--
create table element (deadline timestamp);
SELECT DISTINCT convert(element.deadline,Date) FROM element ORDER BY
 convert(element.deadline,Date) ASC

