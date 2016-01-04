package com.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

/**
 * 为了复现MyCat1.4版本 报错 errno1156 got packets out of order而写的测试程序
 * @author CrazyPig
 *
 */
public class TestMyCatErr1156 {
	
	private static Logger logger = Logger.getLogger(TestMyCatErr1156.class);
	private static final String LINE_SEP = System.getProperty("line.separator");
	private static final int DEFAULT_PARALLEL_SIZE = 100;
	private static final int DEFAULT_THD_SIZE = 20;
	private static final String DEFAULT_IP = "localhost";
	private static final int DEFAULT_PORT = 8066;
	private String ip = DEFAULT_IP;
	private static String urlTemplate = "jdbc:mysql://#{ip}:#{port}/#{db}";
	private String db;
	private int port = DEFAULT_PORT;
	private String user;
	private String password;
	private int parallelSize = DEFAULT_PARALLEL_SIZE;
	private int thdSize = DEFAULT_THD_SIZE;
	
	
	private static String mainHelp;
	private static String wholeHelp;
	
	static {
		try {
			// 加载MySql的驱动类
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("找不到驱动程序类 ，加载驱动失败");
			e.printStackTrace();
		}
	}
	
	private static void initHelpInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("必须提供的参数: " + LINE_SEP)
		  	.append(" -u[用户名] : mycat登陆用户" + LINE_SEP)
		  	.append(" -p[密码] : mycat登陆密码" + LINE_SEP)
		  	.append(" -D[数据库] : mycat访问数据库" + LINE_SEP);
		mainHelp = sb.toString();
		sb.append("可选参数: " + LINE_SEP)
		  	.append(" -h[IP] : mycat访问ip,default = localhost" + LINE_SEP)
		  	.append(" -P[端口号] : mycat访问端口,default = 8066" + LINE_SEP)
		  	.append(" -c[并发访问数量] : 本次测试一次同时并发请求数,default = 100" + LINE_SEP)
		  	.append(" -t[线程池大小] : 本次测试维护的线程池大小, default = 20" + LINE_SEP);
		wholeHelp = sb.toString();
	}
			
	public static void main(String[] args) {
		initHelpInfo();
		if(args == null || args.length == 0) {
			System.out.println(wholeHelp);
			return;
		}
		TestMyCatErr1156 test = new TestMyCatErr1156();
		String errMsg = null;
		errMsg = test.initParam(args);
		if(errMsg == null) {
			test.doTest();
		} else {
			System.out.println(errMsg);
		}
	}
	
	private String initParam(String[] args) {
		int mainParamCount = 3;
		for(String arg : args) {
			String param = arg.trim();
			int strLen = param.length();
			if(strLen > 2) {
				char c1 = arg.charAt(0);
				char c2 = arg.charAt(1);
				if(c1 != '-') 
					return "参数给定不合法";
				switch(c2) {
				case 'u':
					initUser(arg.substring(2, strLen));
					mainParamCount--;
					break;
				case 'p':
					initPassword(arg.substring(2, strLen));
					mainParamCount--;
					break;
				case 'D':
					initDb(arg.substring(2, strLen));
					mainParamCount--;
					break;
				case 'h':
					initIp(arg.substring(2, strLen));
					break;
				case 'P':
					initPort(Integer.parseInt(arg.substring(2, strLen)));
					break;
				case 'c':
					initCdl(Integer.parseInt(arg.substring(2, strLen)));
					break;
				case 't':
					initThd(Integer.parseInt(arg.substring(2, strLen)));
					break;
					default: 
						return "非法参数 -" + c2;
				}
			} else {
				return "参数给定不合法" + LINE_SEP + wholeHelp;
			}
			
			
		}
		if(mainParamCount > 0) {
			return mainHelp;
		}
		return null;
	} 
	
	private void initUser(String userName) {
		user = userName;
	}
	
	private void initPassword(String pass) {
		password = pass;
	}
	
	private void initDb(String dbName) {
		db = dbName;
	}
	
	private void initIp(String hostIp) {
		ip = hostIp;
	}
	
	private void initPort(int _port) {
		port = _port;
	}
	
	private void initCdl(int _cdl) {
		parallelSize = _cdl;
	}
	
	private void initThd(int _thd) {
		thdSize = _thd;
	}
	
	public void doTest() {
		final String url = urlTemplate.replace("#{ip}", ip)
										.replace("#{port}", String.valueOf(port))
										.replace("#{db}", db);
		ExecutorService executor = Executors.newFixedThreadPool(thdSize);
		long count = 1;
		while(true) {
			logger.info("No." + count + " test");
			CountDownLatch cdl = new CountDownLatch(parallelSize);
			for(int i = 0; i < parallelSize; i++) {
				executor.execute(new SingleSqlRunner(cdl, url, user, password, executor));
				cdl.countDown();
			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			count++;
		}
	}
	
}

