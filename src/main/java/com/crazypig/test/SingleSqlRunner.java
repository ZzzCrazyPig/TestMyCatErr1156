package com.crazypig.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

public class SingleSqlRunner implements Runnable {
	
	private static Logger logger = Logger.getLogger(SingleSqlRunner.class);
	
	private static final List<String> randomSqlList = new ArrayList<String>();
	
	static {
		try {
			readRandomSqlFromFile();
		} catch (IOException e) {
		    logger.error(e.getMessage(), e);
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
	
	private DataSource dataSource;
	
	public SingleSqlRunner(DataSource ds) {
		this.dataSource = ds;
	}

	@Override
	public void run() {
		Connection conn = null;
		String sql = getRandomSql();
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			Statement stmt = conn.createStatement();
			 logger.debug("execute sql : " + sql);
			stmt.execute(sql);
			conn.commit();
			stmt.close();
		} catch (SQLException e) {
		    logSQLEx(sql, e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				    // ignore
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
		Random random = new Random(System.currentTimeMillis());
		int size = randomSqlList.size();
		int randIndex = random.nextInt(size);
		return randomSqlList.get(randIndex);
	}
}
