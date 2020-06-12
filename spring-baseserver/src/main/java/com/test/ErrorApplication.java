package com.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.StringHttpMessageConverter;

import java.nio.charset.Charset;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2020-06-08
 */
@Slf4j
@SpringBootApplication
public class ErrorApplication {

	public static void main(String[] args){
		SpringApplication.run(ErrorApplication.class, args);
	}

	@Bean
	public StringHttpMessageConverter stringHttpMessageConverter(){


		return new StringHttpMessageConverter(Charset.forName("UTF-8"));
	}
}
