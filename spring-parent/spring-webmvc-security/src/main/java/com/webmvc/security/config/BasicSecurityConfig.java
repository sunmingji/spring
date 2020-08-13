package com.webmvc.security.config;

import com.webmvc.security.config.security.filter.AuthenticationFilter;
import com.webmvc.security.config.security.provider.MobileCodeAuthenticationProvider;
import com.webmvc.security.config.security.provider.UsernamePasswordAuthenticationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2020-04-26
 */
@Slf4j
@EnableWebSecurity
public class BasicSecurityConfig extends WebSecurityConfigurerAdapter {


	//TODO 代替数据库加载
	private Map<String, UserDetails> userDetailsMap = new HashMap(){
		{
			List<GrantedAuthority> authorities = new ArrayList<>();

			authorities.add(new SimpleGrantedAuthority("ROLE_" + "USER"));
			put("user", new User("user", "password", authorities));
		}
	};

	/**
	 *
	 *
	 * @author sunmj
	 * @date 2020/4/29
	 */
	@Bean
	public UserDetailsService userDetailsService(){

		return (username) -> {

			PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
			//TODO 更换从数据库读取
			UserDetails user = userDetailsMap.get(username.toLowerCase());
			String password = encoder.encode(user.getPassword());

			return new User(user.getUsername(), password, user.isEnabled(),
				user.isAccountNonExpired(), user.isCredentialsNonExpired(),
				user.isAccountNonLocked(), user.getAuthorities());
		};
	}

	/**
	 * 多种登录方式(登录也走security认证方式)
	 * com.webmvc.security.controlelr.LoginController#loginJsonByAuth(com.webmvc.security.model.Login)
	 * 这里都未使用AbstractAuthenticationProcessingFilter 都是直接在method的中直接认证
	 *
	 * refer https://www.jianshu.com/p/779d3071e98d
	 *
	 * @author sunmj
	 * @date 2020/6/12
	 */
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth
				.authenticationProvider(new MobileCodeAuthenticationProvider())
				.authenticationProvider(new UsernamePasswordAuthenticationProvider());
	}

	/**
	 *
	 *
	 * @author sunmj
	 * @date 2020/6/12
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.csrf().disable()
			.formLogin().disable()
			.httpBasic().disable()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)//基于token，所以不需要session
		.and()
		.exceptionHandling()
			.authenticationEntryPoint(authenticationEntryPoint())
			.accessDeniedHandler(accessDeniedHandler())
		.and()
		.authorizeRequests()
			.antMatchers("/auth/**", "/login**", "/loginJsonByAuth").permitAll()//无需认证资源
			.antMatchers("/dept/**").hasRole("DEPT")//游客信息
			.antMatchers("/user/**").hasRole("USER")//用户信息
			.anyRequest().authenticated()//其他全部需要认证(需要token才能访问)
		.and()
//			.userDetailsService(userDetailsService())
		;
		http.addFilterAt(new AuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

//		super.configure(http);
	}

	/**
	 * authException
	 *
	 * @author sunmj
	 * @date 2020/4/30
	 */
	AuthenticationEntryPoint authenticationEntryPoint(){

		return (request, response, authException) -> {

			log.error(" authException localizedMessage {} message {} clazz {}",
					authException.getLocalizedMessage(), authException.getMessage(), authException.getClass());

			response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
		};
	}

	/**
	 * accessDeniedException
	 *
	 * @author sunmj
	 * @date 2020/4/30
	 */
	AccessDeniedHandler accessDeniedHandler(){

		return (request, response, accessDeniedException) -> {

			log.error(" accessDeniedException localizedMessage {} message {} clazz {} ",
					accessDeniedException.getLocalizedMessage(), accessDeniedException.getMessage(), accessDeniedException.getClass());

			if (!response.isCommitted()) {
				response.sendError(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase());
			}
		};
	}

}
