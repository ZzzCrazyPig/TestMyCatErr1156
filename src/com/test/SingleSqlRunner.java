package com.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

public class SingleSqlRunner implements Runnable {
	
	private static Logger logger = Logger.getLogger(SingleSqlRunner.class);
	
	private static final List<String> randomSqlList = new ArrayList<String>();
	
	static {
		try {
			readRandomSqlFromFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void readRandomSqlFromFile() throws IOException {
		InputStream in = SingleSqlRunner.class.getClassLoader().getResourceAsStream("randomSql.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = null;
		while((line = reader.readLine()) != null) {
			String sql = line.replace("[", "").replace("]", "");
			randomSqlList.add(sql);
		}
		reader.close();
	}
	
	private CountDownLatch cdl;
	private String url;
	private String user;
	private String password;
	private ExecutorService executor;
	
	public SingleSqlRunner(CountDownLatch cdl, String url, String user, String password, ExecutorService executor) {
		this.cdl = cdl;
		this.url = url;
		this.user = user;
		this.password = password;
		this.executor = executor;
	}

	@Override
	public void run() {
		try {
			this.cdl.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Connection conn = null;
		String sql = getRandomSql();
		try {
			conn = DriverManager.getConnection(url, user, password);
			Statement stmt = conn.createStatement();
			// logger.info("execute sql : " + sql);
			stmt.execute(sql);
			stmt.close();
		} catch (SQLException e) {
//			e.printStackTrace();
			logSQLEx(sql, e);
			if(e.getErrorCode() == 1156) {
				executor.shutdownNow();
			}
		} finally {
			if(conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
//					e.printStackTrace();
				}
			}
		}
	}
	
	private void logSQLEx(String sql, SQLException e) {
		StringBuffer sb = new StringBuffer();
		sb.append("execute sql : " + sql + "\r\n");
		sb.append("errno:" + e.getErrorCode())
				.append("(" + e.getSQLState() + ")")
				.append(",errMsg:" + e.getMessage() + "\r\n")
				.append(e.toString());
		logger.error(sb.toString());
	}
	
	/**
	 * 随机获取当前需要测试的sql语句
	 * @return
	 */
	private String getRandomSql() {
		Random random = new Random();
		int size = randomSqlList.size();
		int randIndex = random.nextInt(size);
		return randomSqlList.get(randIndex);
	}
}
