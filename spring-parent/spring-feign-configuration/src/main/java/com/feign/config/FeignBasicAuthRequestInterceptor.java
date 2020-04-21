package com.feign.config;

import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;

/**
 * feign拦截器
 *
 * @author sunmingji
 * @date 2019-12-26
 */
@Slf4j
public class FeignBasicAuthRequestInterceptor implements RequestInterceptor {

	@Override
	public void apply(RequestTemplate requestTemplate) {

		Request request = requestTemplate.request();


		Map<String, Collection<String>> queries = requestTemplate.queries();

		byte[] body = request.body();

		String bodyStr = "";
		if(body != null){
			 bodyStr = new String(body);
		}

//		log.info("url : {}, queries : {}, bodyStr : {}", request.url(), JSON.toJSONString(queries), bodyStr);
	}
}
