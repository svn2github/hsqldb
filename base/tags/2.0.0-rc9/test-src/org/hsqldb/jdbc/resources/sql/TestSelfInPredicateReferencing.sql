DROP TABLE dups IF EXISTS;
CREATE TABLE dups(pk INTEGER NOT NULL PRIMARY KEY,val VARCHAR(10) NOT NULL);
INSERT INTO dups VALUES (1, 'first');
INSERT INTO dups VALUES (2, 'second');
INSERT INTO dups VALUES (3, 'third');
INSERT INTO dups VALUES (4, 'first');
INSERT INTO dups VALUES (5, 'first');
INSERT INTO dups VALUES (6, 'second');
SELECT a.pk, a.val FROM dups a WHERE a.pk in (SELECT sa.pk FROM dups sa, dups sb WHERE sa.val=sb.val AND sa.pk!=sb.pk);
SELECT sa.pk FROM dups sa, dups sb WHERE sa.val=sb.val AND sa.pk!=sb.pk;
/*c5*/SELECT a.pk, a.val FROM dups a WHERE a.pk IN (1, 2, 4, 5, 6);
/*c2*/SELECT a.pk, a.val FROM dups a WHERE (a.pk, a.val) IN ((1, 'first'), (5, 'first'));

-- IN with nulls
/*c0*/SELECT * FROM dups WHERE null IN (1, 2)
/*c0*/SELECT * FROM dups WHERE null IN (null, 2)
/*c0*/SELECT * FROM dups WHERE null IN (null, 2)
/*e*/SELECT * FROM dups WHERE null IN (null, null)
/*c0*/SELECT * FROM dups WHERE cast(null AS INT) IN (null, null)
/*c1*/SELECT * FROM dups WHERE pk IN (null, 1)
/*c0*/SELECT * FROM dups WHERE pk IN (null, null)

-- ALL
/*r
 1,first
 4,first
 5,first
*/SELECT a.pk, a.val FROM dups a WHERE a.val =
  ALL(SELECT sa.val FROM dups sa WHERE sa.val = 'first') ORDER BY a.pk;
/*r
 1,first
 2,second
 3,third
 4,first
 5,first
 6,second
*/SELECT a.pk, a.val FROM dups a WHERE a.val >=  ALL(SELECT sa.val FROM dups sa WHERE sa.val = 'first') ORDER BY a.pk;
/*r
 2,second
 3,third
 6,second
*/SELECT a.pk, a.val FROM dups a WHERE a.val > ALL(SELECT sa.val FROM dups sa WHERE sa.val = 'first') ORDER BY a.pk;
/*r
 1,first
 2,second
 4,first
 5,first
 6,second
*/SELECT a.pk, a.val FROM dups a WHERE a.val
  <= ALL(SELECT sa.val FROM dups sa WHERE sa.val = 'second') ORDER BY a.pk;
/*r
 1,first
 2,second
 4,first
 5,first
 6,second
*/SELECT a.pk, a.val FROM dups a WHERE a.val
 < ALL(SELECT sa.val FROM dups sa WHERE sa.val = 'third') ORDER BY a.pk;

/*r
 1,first
 4,first
 5,first
*/SELECT a.pk, a.val FROM dups a WHERE a.val
 <= ALL(SELECT sa.val FROM dups sa) ORDER BY a.pk;

-- ANY

/*r
 1,first
 2,second
 4,first
 5,first
 6,second
*/SELECT a.pk, a.val FROM dups a WHERE a.val
 < ANY(SELECT sa.val FROM dups sa) ORDER BY a.pk;

/*r
 2,second
 3,third
 6,second
*/SELECT a.pk, a.val FROM dups a WHERE a.val
 > ANY(SELECT sa.val FROM dups sa) ORDER BY a.pk;

/*r
 1,first
 2,second
 3,third
 4,first
 5,first
 6,second
*/SELECT a.pk, a.val FROM dups a WHERE a.val
 <= ANY(SELECT sa.val FROM dups sa) ORDER BY a.pk;

/*c0*/SELECT a.pk, a.val FROM dups a WHERE a.val
 <= ANY(SELECT CAST(NULL AS CHAR) FROM dups sa) ORDER BY a.pk;

/*e*/SELECT a.pk, a.val FROM dups a WHERE a.val
 <= ANY(SELECT NULL FROM dups sa) ORDER BY a.pk;

/*c0*/SELECT a.pk, a.val FROM dups a WHERE a.val
 <= ANY(SELECT CAST(NULL AS CHAR) FROM dups sa WHERE sa.val ='fourth') ORDER BY a.pk;

/*e*/SELECT a.pk, a.val FROM dups a WHERE a.val
 <= ANY(SELECT NULL FROM dups sa WHERE sa.val ='fourth') ORDER BY a.pk;

-- non-correlated single value
/*r
 1,first
 4,first
 5,first
*/SELECT a.pk, a.val FROM dups a WHERE a.val
 = (SELECT sa.val FROM dups sa WHERE sa.pk = 4) ORDER BY a.pk;
-- bug item #1100384
drop table a if exists;
drop table b if exists;
drop table m if exists;
create table a(a_id integer);
create table b(b_id integer);
create table m(m_a_id integer, m_b_id integer);
insert into a(a_id) values(1);
insert into b(b_id) values(10);
insert into m(m_a_id, m_b_id) values(1, 5);
insert into m(m_a_id, m_b_id) values(1, 10);
insert into m(m_a_id, m_b_id) values(1, 20);
/*r1,10*/select a.a_id, b.b_id from a, b
 where a.a_id in (select m.m_a_id from m where b.b_id = m.m_b_id);
/*r1,10*/select a.a_id, b.b_id from a join b on
 a.a_id in (select m.m_a_id from m where b.b_id = m.m_b_id);
/*u1*/update m set m_a_id = 2 where m_b_id in (1, 10, 100);
/*r
 1,5
 2,10
 1,20
*/select * from m order by m_b_id

--
DROP TABLE T IF EXISTS;
CREATE TABLE T(C VARCHAR_IGNORECASE(10));
INSERT INTO T VALUES ('felix');
/*r
 felix
*/SELECT * FROM T WHERE C IN ('Felix', 'Feline');
/*c0*/SELECT * FROM T WHERE C IN ('Pink', 'Feline');
DROP TABLE T;
CREATE TABLE T(C CHAR(10));
INSERT INTO T VALUES ('felix');
/*c1*/SELECT * FROM T WHERE C IN ('felix', 'pink');
DROP TABLE T;

