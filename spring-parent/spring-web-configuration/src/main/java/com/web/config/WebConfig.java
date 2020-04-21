package com.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2019-12-25
 */
@Slf4j
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		WebMvcConfigurer.super.addArgumentResolvers(resolvers);

		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		//添加消息转换器
		converters.add(new MappingJackson2HttpMessageConverter());
//		converters.add(new FastJsonHttpMessageConverter());

		//消息转换器与Resolver绑定
		resolvers.add(new CustomMessageConverterMethodArgumentResolver(converters));

	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		WebMvcConfigurer.super.configureMessageConverters(converters);

	}

	@Bean
	public HttpMessageConverter<String> responseBodyConverter() {
		return new StringHttpMessageConverter(Charset.forName("UTF-8"));
	}

	//统一异常处理
	@Override
	public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
		exceptionResolvers.add((request, response, handler, ex) ->{

			if(ex instanceof MissingServletRequestParameterException){
				log.info(" parameterName {}", ((MissingServletRequestParameterException) ex).getParameterName());
			}
			return null;
		});
	}



}
