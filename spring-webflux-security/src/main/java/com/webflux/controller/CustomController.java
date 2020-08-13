package com.webflux.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 获取当前登录用户名
 *
 * @author sunmingji
 * @date 2019-11-15
 */
@Slf4j
@RestController
public class CustomController{

	private WebFilterChain webFilterChain;

	@RequestMapping("/loginAdmin")
	public Mono loginGateWay(String userName, ServerWebExchange exchange){

		//获取当前登录用户
		Mono str = ReactiveSecurityContextHolder.getContext()
				.filter(c -> c.getAuthentication() != null)
				.map(SecurityContext::getAuthentication)
				.map(Authentication::getPrincipal)
				.cast(User.class)
				.map(User::getUsername); // 自定义的Context

		return str;
	}

	@RequestMapping("/loginRob")
	public Mono loginRob(String userName, ServerWebExchange exchange){

		//获取当前登录用户
		Mono str = ReactiveSecurityContextHolder.getContext()
				.filter(c -> c.getAuthentication() != null)
				.map(SecurityContext::getAuthentication)
				.map(Authentication::getPrincipal)
				.cast(User.class)
				.map(User::getUsername)
				.switchIfEmpty(Mono.just("JoeJoke")); // 自定义的Context

		return str;
	}

	@RequestMapping("/getAuthorities")
	public Mono getAuthorities(String userName, ServerWebExchange exchange){

		return ReactiveSecurityContextHolder.getContext()
				.filter(c -> c.getAuthentication() != null)
				.map(SecurityContext::getAuthentication)
				.map(Authentication::getPrincipal)
				.cast(User.class)
				.map(User::getAuthorities);
	}

}
