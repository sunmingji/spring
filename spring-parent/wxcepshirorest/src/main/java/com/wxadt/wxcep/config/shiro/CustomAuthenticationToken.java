package com.wxadt.wxcep.config.shiro;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2021-03-05
 */
public class CustomAuthenticationToken implements AuthenticationToken {

	private String token;

	public CustomAuthenticationToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public Object getPrincipal() {
		return token;
	}

	@Override
	public Object getCredentials() {
		return token;
	}
}
