package com.test.gateway.security;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * TokenAuthentication
 *
 * @author sunmingji
 * @date 2019-11-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TokenAuthentication extends UsernamePasswordAuthenticationToken {

	private final String token;

	public TokenAuthentication(){
		super(null, null);
		this.token = "token";
	}
	public TokenAuthentication(String token, Object principal, Object credentials) {
		super(principal, credentials);
		this.token = token;
	}

	public TokenAuthentication(String token, Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
		super(principal, credentials, authorities);
		this.token = token;
	}

}
