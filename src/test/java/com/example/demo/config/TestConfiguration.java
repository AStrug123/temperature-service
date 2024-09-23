package com.example.demo.config;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.example.demo.utils.filereader.FileReaderStrategy;

@Configuration
class TestConfiguration {

	@Bean
	@Primary
	public FileReaderStrategy fileListService() {
		return mock(FileReaderStrategy.class);
	}
}
