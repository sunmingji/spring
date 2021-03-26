package com.test.gateway;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.filter.reactive.HiddenHttpMethodFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2019-11-14
 */
@Slf4j
@SpringBootApplication
public class GatewayApplication  {

	public static void main(String[] args){
//		Hooks.onOperatorDebug();
		SpringApplication.run(GatewayApplication.class, args);
	}

	/**
	 * spring-boot 2.0.5.RELEASE
	 * 解决java.lang.IllegalStateException: Only one connection receive subscriber allowed.
	 *
	 * @author sunmj
	 * @date 2019/11/14
	 */
	@Bean
	public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
		return new HiddenHttpMethodFilter() {
			@Override
			public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
				return chain.filter(exchange);
			}
		};
	}

	@RequestMapping("/fallback")
	public Mono<String> fallback() {
		return Mono.just("fallback");
	}

}
