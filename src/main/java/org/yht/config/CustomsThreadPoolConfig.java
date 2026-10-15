package org.yht.config;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
public class CustomsThreadPoolConfig {
	
	@Value("${customId.thread.pool.core-size}")
	public int coreSize;
	@Value("${customId.thread.pool.max-size}")
	public int maxSize;	
	
	@Autowired
	RequestContextFilter filter;
	
	@Autowired
	DispatcherServlet servlet;
		
	@Bean
	@Primary
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
    	ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize); //최초 쓰레드 사이즈
        executor.setMaxPoolSize(maxSize);   //최대 쓰레드 사이즈
        executor.setQueueCapacity(0);      // Max쓰레드가 동작하는 경우 대기하는 queue 사이즈   //쓰레드와 큐가 모두 꽉차게 되면 Exception이 발생
        executor.setThreadNamePrefix("multi-thread-");
        executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
        executor.initialize();
        
        return executor;
    }	
	
	@PostConstruct
	public void init() {
		
		//ThreadPool에서 관리하는 쓰레드의 경우 쓰레드를 new 해서 새로운 객체안에서 db처리할때 mapper 호출하면 outside 에서 접근에러남.
        //소스 추적해보니 DispatcherServlet 접근할때 this.threadContextInheritable 값 셋팅하는데 default 가 false 인데  true 로 셋팅해주면 outside에서도 접근가능함.
		//@PostConstruct 어노테이션 사용해서 빈 초기화후 초기 셋팅한다 was 기동될때 한번 실행됨.
	    // Normal mode
	    filter.setThreadContextInheritable(true);
	    // Debug mode
	    servlet.setThreadContextInheritable(true);
	}
}
