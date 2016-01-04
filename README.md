# 项目描述
该项目用于测试MyCat1.4版本AIO网络模型并发异常errno 1156 Got Packets out of order

# 错误描述
在对MyCat1.4版本AIO网络模型并发测试下，发现经常发生Mysql errno 1156 Got packets out of order

以下是MyCat日志报告信息:

    WARN [$_AIO3] (SingleNodeHandler.java:222) -execute  sql err : errno:1156 Got packets out of order con:MySQLConnection [id=19, lastTime=1451891972458, user=root, schema=test, old shema=test, borrowed=true, fromSlaveDB=false, threadId=205, charset=utf8, txIsolation=3, autocommit=true, attachment=dn1{SHOW WARNINGS}, respHandler=SingleNodeHandler [node=dn1{SHOW WARNINGS}, packetId=1], host=localhost, port=3306, statusSync=null, writeQueue=0, modifiedSQLExecuted=false] frontend host:127.0.0.1/63900/root

# 错误复现

## 测试表结构

    mysql> use test;
    Database changed
    mysql> desc employee;
    +---------+--------------+------+-----+---------+----------------+
    | Field   | Type | Null | Key | Default | Extra  |
    +---------+--------------+------+-----+---------+----------------+
    | id  | int(11)  | NO   | PRI | NULL| auto_increment |
    | name| varchar(20)  | YES  | | NULL||
    | address | varchar(255) | YES  | | NULL||
    | phone   | varchar(50)  | YES  | | NULL||
    +---------+--------------+------+-----+---------+----------------+
    4 rows in set (0.00 sec)

## MyCat配置
(1) schema.xml配置

    <?xml version="1.0"?>
    <!DOCTYPE mycat:schema SYSTEM "schema.dtd">
    <mycat:schema xmlns:mycat="http://org.opencloudb/">
    
    	<schema name="testdb" checkSQLschema="false" sqlMaxLimit="100">
    		<table name="employee" primaryKey="id" autoIncrement="true" dataNode="dn1" />
    	</schema>
    	
    	<dataNode name="dn1" dataHost="localhost" database="test" />
    
    
    	<dataHost name="localhost" maxCon="1000" minCon="5" balance="0" writeType="0" dbType="mysql" dbDriver="native">
    		<heartbeat>select user()</heartbeat>
    		<writeHost host="hostM1" url="localhost:3306" user="root" password="mysql">
    		</writeHost>
    	</dataHost>
    	
    </mycat:schema>

(2) server.xml配置

    <?xml version="1.0" encoding="UTF-8"?>
    
    <!DOCTYPE mycat:server SYSTEM "server.dtd">
    <mycat:server xmlns:mycat="http://org.opencloudb/">
    	<system>
    		<property name="defaultSqlParser">druidparser</property>
    		<property name="serverPort">8066</property>
    		<property name="managerPort">9066</property>
    		<property name="usingAIO">1</property>
    	</system>
    	<user name="root">
    		<property name="password">mysql</property>
    		<property name="schemas">testdb</property>
    	</user>
    
    
    </mycat:server>

(3) sequence_conf.properties配置

    #default global sequence
    GLOBAL.HISIDS=
    GLOBAL.MINID=10001
    GLOBAL.MAXID=20000
    GLOBAL.CURID=10000
    
    # self define sequence
    COMPANY.HISIDS=
    COMPANY.MINID=1001
    COMPANY.MAXID=2000
    COMPANY.CURID=1000
    
    CUSTOMER.HISIDS=
    CUSTOMER.MINID=1001
    CUSTOMER.MAXID=2000
    CUSTOMER.CURID=1000
    
    ORDER.HISIDS=
    ORDER.MINID=1001
    ORDER.MAXID=2000
    ORDER.CURID=1000
    
    HOTNEWS.HISIDS=
    HOTNEWS.MINID=1001
    HOTNEWS.MAXID=2000
    HOTNEWS.CURID=1000
    
    EMPLOYEE.HISIDS=
    EMPLOYEE.MINID=100000000
    EMPLOYEE.MAXID=200000000
    EMPLOYEE.CURID=100099999

## 测试程序配置

测试程序下载地址 : https://github.com/ZzzCrazyPig/TestMyCatErr1156.git

(1) 测试程序配置随机sql

config目录 : randomSql.txt

    [insert into employee(name,address,phone) values('unknown','unknown','unknown')]
    [select * from employee where id = 1]
    [insert into employee(name,address,phone) values('UNKNOWN','UNKNOWN','UNKNOWN')]
    [select id,name from employee limit 1]
    [insert into employee(name,address,phone) values('EMP','GZ','unknown')]
    [update employee set address = 'UNKNOWN' where id = 4]
    [select * from employee limit 5]
    [update employee set address = 'UNKNOWN' where id = 1234]
    [select id,name,address from employee limit 10]

(2) 配置TestMyCatErr1156.java启动参数示例:

    -uroot -pmysql -Dtestdb -P8066 -c100 -t20

    -u :  MyCat登录用户
    -p : MyCat登录密码
    -D : 需要连接的MyCat数据库
    -P : MyCat端口
    -c : 测试的并发访问数量，即一次有多少个sql语句同时通往MyCat
    -t : 测试维护的线程池大小


**【注意】**测试程序一直循环测试，直到遇到错误码1156即停止测试