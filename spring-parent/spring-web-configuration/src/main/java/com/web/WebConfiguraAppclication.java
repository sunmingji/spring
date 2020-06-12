package com.web;

import com.web.config.error.CustomErrorAttributes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2020-04-25
 */
@Slf4j
@SpringBootApplication
public class WebConfiguraAppclication {

	public static void main(String[] args){
		SpringApplication.run(WebConfiguraAppclication.class, args);
	}


	@Bean
	public ErrorAttributes errorAttributes(){


		return new CustomErrorAttributes();
	}
}
