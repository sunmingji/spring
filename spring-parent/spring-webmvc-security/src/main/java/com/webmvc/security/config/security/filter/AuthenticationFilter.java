package com.webmvc.security.config.security.filter;

import com.google.common.net.HttpHeaders;
import com.webmvc.security.controlelr.LoginController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2020-04-28
 */
@Slf4j
public class AuthenticationFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (authorization == null || !authorization.startsWith("Bearer ")) {
			chain.doFilter(request, response);
			return;
		}

		String token = authorization.replace("Bearer ", "");
		//取出user
		Authentication authentication = LoginController.loginMap.get(token);

		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(request, response);
	}
}
