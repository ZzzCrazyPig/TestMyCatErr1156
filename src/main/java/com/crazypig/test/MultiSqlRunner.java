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

public class MultiSqlRunner implements Runnable {
	
	private static Logger logger = Logger.getLogger(MultiSqlRunner.class);
	
	private static final List<String> randomSqlList = new ArrayList<String>();
	
	static {
		try {
			readRandomSqlFromFile();
		} catch (IOException e) {
		    logger.error(e.getMessage(), e);
		}
	}
	
	private static void readRandomSqlFromFile() throws IOException {
		InputStream in = MultiSqlRunner.class.getClassLoader().getResourceAsStream("randomMultiSql.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = null;
		while((line = reader.readLine()) != null) {
			String sql = line.replace("[", "").replace("]", "");
			randomSqlList.add(sql);
		}
		reader.close();
	}
	
	private int maxMultiSize;
	private DataSource dataSource;
	
	public MultiSqlRunner(DataSource ds, int maxMultiSize) {
		this.dataSource = ds;
		this.maxMultiSize = maxMultiSize;
	}

	@Override
	public void run() {
		Connection conn = null;
		String sql = null;
		try {
		    conn = dataSource.getConnection();
		    conn.setAutoCommit(false);
		    int count = getMultiSize();
    		for (int i = 0; i < count; i++) {
    		    sql = getRandomSql();
    		    logger.debug("execute sql : " + sql);
    		    Statement stmt = conn.createStatement();
	            stmt.execute(sql);
	            conn.commit();
	            stmt.close();
    		}
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
	    sb.append("execute sql : " + String.valueOf(sql) + "\r\n");
	    sb.append("errno:" + e.getErrorCode())
	            .append("(" + e.getSQLState() + ")")
	            .append(",errMsg:" + e.getMessage() + "\r\n")
	            .append(e.toString());
	    logger.error(sb.toString());
	}
	
	private int getMultiSize() {
	    Random rand = new Random(System.currentTimeMillis());
	    return rand.nextInt(maxMultiSize);
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
