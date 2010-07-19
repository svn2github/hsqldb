--
-- TestSelfJoins.txt
--
create table tsj1 (a integer primary key, b integer);
insert into tsj1 values(5,5);
insert into tsj1 values(6,6);
insert into tsj1 values(11,21);
insert into tsj1 values(12,22);
insert into tsj1 values(13,23);
insert into tsj1 values(14,24);
insert into tsj1 values(15,25);
insert into tsj1 values(16,26);
insert into tsj1 values(17,27);
/*r3*/select count(*) from tsj1 where a > 14
/*r5*/select count(*) from tsj1 where cast(a as character(2)) > '14'
/*r81*/select count(*) from tsj1 a cross join tsj1 b

/*e*/select count(*) from tsj1 a cross join tsj1 b on a.a = b.a
/*r9*/select count(*) from tsj1 a inner join tsj1 b on a.a = b.a

drop table table1 if exists
create table table1(column1 int not null, column2 varchar(255) not null,
 primary key(column1))
insert into table1(column1,column2) values(100,'string1')
insert into table1(column1,column2) values(200,'string2')
insert into table1(column1,column2) values(300,'string3')
/*r3*/select count(1) rows from table1
/*r0*/select count(1) rows from table1 where column1<300 and column1>=200 and column1<1
/*r0*/select count(1) rows from table1 where column1<1 and column1<300 and column1>=200

drop table t1 if exists;
drop table t2 if exists;
drop table t3 if exists;
create table t1(id int primary key, c varchar(20));
create table t2(id int primary key, rid int, c varchar(20));
create table t3(id int primary key, rid int, c varchar(20));
insert into t1 values(1, 'one'), (2, 'two'), (10, 'ten'), (11, 'eleven');
insert into t2 values(1, 1, 'one'), (2, 2, 'two'), (3, 10, 'ten'), (4, 11, 'eleven');
insert into t3 values(1, 1, 'one'), (2, 2, 'two'), (3, 10, 'ten'), (4, 11, 'eleven');
