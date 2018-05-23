package com.crazypig.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 系统配置
 * @author CrazyPig
 *
 */
@Component
public class SystemConfig {
    
    @Value("${concurrentSize}")
    private int concurrentSize = 100;
    @Value("${threadPoolSize}")
    private int threadPoolSize = 20;
    

    public int getConcurrentSize() {
        return concurrentSize;
    }

    public void setConcurrentSize(int concurrentSize) {
        this.concurrentSize = concurrentSize;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }
    

}
