package com.test.gateway.controller;

import com.test.gateway.security.Login;
import com.test.gateway.security.SecurityConfig;
import com.test.gateway.security.TokenAuthentication;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.RequestPath;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;


/**
 * /login
 *
 * @author sunmingji
 * @date 2019-11-19
 */
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SecurityLoginController {

	private final ReactiveAuthenticationManager authenticationManager;

	private final ServerSecurityContextRepository contextRepository;

	@PostMapping("/login")
	public Mono login(@RequestBody Login login, ServerWebExchange exchange) {
		final String token = UUID.randomUUID().toString().replaceAll("-", "");
		TokenAuthentication authenticationToken = new TokenAuthentication(token, login.getUsername(), SecurityConfig.PASSWD);

		return authenticationManager.authenticate(authenticationToken)
				.doOnSuccess(auth -> contextRepository.save(exchange, new SecurityContextImpl(authenticationToken)))
				.then(Mono.just(token));
	}
}
