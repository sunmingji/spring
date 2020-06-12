package com.webmvc.security.config.security.provider;

import com.webmvc.security.config.security.token.MobileCodeAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.HashSet;
import java.util.Set;

/**
 * 手机登录验证
 *
 * @author sunmingji
 * @date 2020-06-12
 */
public class MobileCodeAuthenticationProvider implements AuthenticationProvider {

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String mobile = (authentication.getPrincipal() == null) ? "NONE_PROVIDED" : authentication.getName();
		String code = (String) authentication.getCredentials();
		/*if (CheckUtils.isEmpty(code)) {
			throw new BadCredentialsException("验证码不能为空");
		}
		Safety safety = safetyService.load(null, null, mobile);
		if (null == safety) {
			throw new BadCredentialsException("用户不存在");
		}*/
		User user = null;
		if (null == user) {
			throw new BadCredentialsException("用户不存在");
		}
		// 手机号验证码业务还没有开发，先用4个0验证
		if (!code.equals("0000")) {
			throw new BadCredentialsException("验证码不正确");
		}
		UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(user.getUsername(), code, listUserGrantedAuthorities(0l));
		result.setDetails(authentication.getDetails());
		return result;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		System.out.println(this.getClass().getName() + "---supports");
		return (MobileCodeAuthenticationToken.class.isAssignableFrom(authentication));
	}

	private Set<GrantedAuthority> listUserGrantedAuthorities(Long uid) {
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		/*if (CheckUtils.isEmpty(uid)) {
			return authorities;
		}*/
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		return authorities;
	}

}
