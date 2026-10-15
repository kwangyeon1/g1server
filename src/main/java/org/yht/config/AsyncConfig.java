package org.yht.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

	
    /**
     * Excel Parser ThreadPool Executor 생성
     * @return
     */
	@Bean
    public Executor getZipcode() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        
        taskExecutor.setCorePoolSize(5);
        taskExecutor.setMaxPoolSize(500);
        taskExecutor.setQueueCapacity(0);
        taskExecutor.setThreadNamePrefix("getZipcode - ");
        taskExecutor.setKeepAliveSeconds(30);
        taskExecutor.setAllowCoreThreadTimeOut(true);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);

        taskExecutor.initialize();
        
        return taskExecutor;
    }

}
