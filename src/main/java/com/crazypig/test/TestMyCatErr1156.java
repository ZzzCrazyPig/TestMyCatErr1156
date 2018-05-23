package com.crazypig.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 为了复现MyCat1.4版本 报错 errno1156 got packets out of order而写的测试程序
 * @author CrazyPig
 *
 */
@Component
public class TestMyCatErr1156 {
    
    private static Logger logger = Logger.getLogger(TestMyCatErr1156.class);
    
    @Autowired
    private SystemConfig systemConfig;
    @Autowired
    private DataSource dataSource;
    
    private ExecutorService executor;
    
	public static void main(String[] args) {
	    ApplicationContext ctx = startSpringApplicationContext();
	    TestMyCatErr1156 target = ctx.getBean(TestMyCatErr1156.class);
	    try {
	        target.startTest();
	    } finally {
	        target.destory();
	    }
	}
	
	private static ApplicationContext startSpringApplicationContext() {
	    final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        ctx.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            
            @Override
            public void run() {
                ctx.close();
            }
        }));
        
        return ctx;
	}
	
	
	public void startTest() {
	    
		executor = Executors.newFixedThreadPool(systemConfig.getThreadPoolSize(), new ThreadFactory() {
            
		    private AtomicLong cnt = new AtomicLong(0L);
		    
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "executor" + cnt.incrementAndGet());
            }
            
        });
		
		long count = 1;
		for (;;) {
			logger.info("No." + count + " test");
			
			try {
			    // single sql test
			    testSingleSql();
			    // multi sql test
	            testMultiSql();
	            count++;
	            Thread.sleep(3000);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
			
		}
	}
	
	private void testSingleSql() throws InterruptedException {
	    int concurrentSize = systemConfig.getConcurrentSize();
        for (int i = 0; i < concurrentSize; i++) {
            executor.execute(new SingleSqlRunner(dataSource));
        }
	}
	
	private void testMultiSql() throws InterruptedException {
	    int concurrentSize = systemConfig.getConcurrentSize();
        for (int i = 0; i < concurrentSize; i++) {
            executor.execute(new MultiSqlRunner(dataSource, 5));
        }
	}
	
	public void destory() {
	    
	    if (executor != null) {
	        executor.shutdown();
	    }
	    
	}
	
}

