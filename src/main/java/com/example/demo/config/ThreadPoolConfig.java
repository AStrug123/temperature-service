package com.example.demo.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.demo.utils.Constants;

@Configuration
public class ThreadPoolConfig {

	@Bean
	public ExecutorService temperatureExecutorService() {
		return Executors.newFixedThreadPool(Constants.THREAD_POOL_SIZE);
	}
}
