package com.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 重写request方法，是请求参数可自定义
 *
 * @author sunmj
 * @since 2019-05-07
 */
@Component
@Order(1)
@Slf4j
public class WebFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


		CustomHttpServletRequestWrapper sensitiveHttpServletRequestWrapper = new CustomHttpServletRequestWrapper(request);

		filterChain.doFilter(sensitiveHttpServletRequestWrapper, response);
	}
}
