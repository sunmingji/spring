package com.webflux.controller;

import com.webflux.security.Login;
import com.webflux.security.TokenAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2020-03-07
 */
@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SecurityController {

	//暂时存放登录用户
	public static ConcurrentMap<String, String> loginMap = new ConcurrentHashMap();
	private final ReactiveAuthenticationManager authenticationManager;

	private final ServerSecurityContextRepository contextRepository;

	@RequestMapping("/login")
	private Mono login(@RequestBody Login login, ServerWebExchange exchange){

		final String token = UUID.randomUUID().toString().replaceAll("-", "");
		TokenAuthentication authenticationToken = new TokenAuthentication(token, login.getUsername(), login.getPassword());

		loginMap.put(login.getUsername(), login.getPassword());

		return authenticationManager.authenticate(authenticationToken)
				.doOnSuccess(auth -> contextRepository.save(exchange, new SecurityContextImpl(authenticationToken)))
				.doOnError(throwable -> {
					//处理异常
					if(throwable instanceof BadCredentialsException){

						log.info(" message {}", throwable.getMessage());
					}
				})
				.then(Mono.just(token));
	}
}
