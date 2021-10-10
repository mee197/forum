package com.wei.forum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

@SpringBootApplication
public class ForumApplication extends SpringBootServletInitializer {

	private static final Logger logger = LoggerFactory.getLogger(ForumApplication.class);

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(ForumApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(ForumApplication.class, args);
		logger.info("项目启动成功");
	}

}

