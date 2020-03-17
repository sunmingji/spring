package com.webflux.security;

import com.webflux.controller.SecurityController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.DelegatingReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.authorization.AuthorityReactiveAuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.authorization.DelegatingReactiveAuthorizationManager;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcherEntry;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SecurityConfig
 *
 * @author sunmingji
 * @date 2019-11-19
 */
@Slf4j
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

	//可换为redis存储
	private final Map<String, SecurityContext> tokenCache = new ConcurrentHashMap<>();

	private static final String BEARER = "Bearer ";

	private static final String[] AUTH_WHITELIST = new String[]{"/login"};

	public static final String PASSWD = "weiyan!";//固定密码

	/**
	 * 认证管理
	 *
	 * @author sunmj
	 * @date 2019/11/19
	 */
	@Bean
	ReactiveAuthenticationManager reactiveAuthenticationManager() {
		final ReactiveUserDetailsService detailsService = userDetailsService();
		LinkedList<ReactiveAuthenticationManager> managers = new LinkedList<>();
		managers.add(authentication -> {
			//其他登陆方式(比如手机号验证码登陆)可在此设置不得抛出异常或者Mono.error
			return Mono.empty();
		});
		//必须放最后不然会优先使用用户名密码校验但是用户名密码不对时此AuthenticationManager会调用Mono.error造成后面的AuthenticationManager不生效
		managers.add(new UserDetailsRepositoryReactiveAuthenticationManager(detailsService));
		return new DelegatingReactiveAuthenticationManager(managers);
	}

	/**
	 * 认证缓存(token缓存)
	 *
	 * @author sunmj
	 * @date 2019/11/19
	 */
	@Bean
	ServerSecurityContextRepository serverSecurityContextRepository() {
		return new ServerSecurityContextRepository() {
			@Override
			public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
				if (context.getAuthentication() instanceof TokenAuthentication) {
					TokenAuthentication authentication = (TokenAuthentication) context.getAuthentication();
					tokenCache.put(authentication.getToken(), context);
				}
				return Mono.empty();
			}

			@Override
			public Mono<SecurityContext> load(ServerWebExchange exchange) {
				ServerHttpRequest request = exchange.getRequest();
				String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
				if (StringUtils.isEmpty(authorization) || !tokenCache.containsKey(authorization)) {
					return Mono.empty();
				}
				return Mono.just(tokenCache.get(authorization));
			}
		};
	}

	/**
	 * security 配置
	 *
	 * @author sunmj
	 * @date 2019/11/19
	 */
	@Bean
	SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
		return http
				.csrf().disable()
				.formLogin().disable()
				.httpBasic().disable()
				.exceptionHandling()
				.accessDeniedHandler(accessDeniedHandler())
				.authenticationEntryPoint(serverAuthenticationEntryPoint())//处理认证异常
				.and()
				.addFilterAt(authenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)//认证
				.addFilterAt(accessWebFilter(), SecurityWebFiltersOrder.AUTHORIZATION)//动态鉴权
				.authorizeExchange()
				.pathMatchers(AUTH_WHITELIST).permitAll()
				.pathMatchers("/loginAdmin").hasRole("ADMIN")//固定权限
				.pathMatchers("/loginRob").hasAuthority("/loginRob")//固定权限
				.anyExchange().authenticated()//其他所有的都要认证
				.and()
				.build();
	}

	//修改为访问数据库的UserDetailsService即可
	/*@Bean
	ReactiveUserDetailsService userDetailsService() {
		User.UserBuilder userBuilder = User.withDefaultPasswordEncoder();
		UserDetails rob = userBuilder.username("rob").password("rob").roles("USER").authorities("/loginRob").build();
		UserDetails admin = userBuilder.username("admin").password("admin").roles("USER", "ADMIN").build();
		UserDetails sunmj = userBuilder.username("sunmj").password("sunmj").authorities("/ucenter-web/oneself/permissions").build();
		return new MapReactiveUserDetailsService(rob, admin, sunmj);
	}*/

	//UserDetailService动态获取
	@Bean
	ReactiveUserDetailsService userDetailsService(){

		return username -> {

			/*
				正常步骤 根据username从数据库查询对应的user
				因为这边是给ucenter-web认证 只有正确的情况才会存储认证信息所以可以不校验密码信息(这里使用固定密码)
			 */
			//TODO 这里是固定权限需要从redis拉取权限
			String[] authorityArray = Arrays.asList("/loginRob", "/getAuthorities", "/ucenter-web/oneself/permissions").toArray(new String[]{});

			log.info(" username : {} ", username);

			PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
			User.UserBuilder userBuilder = User.builder();

			//根据username获取用户信息

			String passwd = SecurityController.loginMap.get(username);
			UserDetails result = userBuilder.username(username).password(PASSWD).authorities(authorityArray).passwordEncoder(encoder::encode).build();
			return result == null ? Mono.empty() : Mono.just(User.withUserDetails(result).build());
		};
	}

	/**
	 * 认证具体实现(判断token真实性)
	 *
	 * @author sunmj
	 * @date 2019/11/19
	 */
	@Bean
	ServerAuthenticationConverter serverAuthenticationConverter() {
		final AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken("key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
		return exchange -> {
			String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
			if (StringUtils.isEmpty(token)) {
				return Mono.just(anonymous);
			}
			if (!token.startsWith(BEARER) || token.length() <= BEARER.length() || !tokenCache.containsKey(token.substring(BEARER.length()))) {
				return Mono.just(anonymous);
			}
			return Mono.just(tokenCache.get(token.substring(BEARER.length())).getAuthentication());
		};
	}

	/**
	 * 认证
	 *
	 * @author sunmj
	 * @date 2019/11/19
	 */
	@Bean
	AuthenticationWebFilter authenticationWebFilter() {
		AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(reactiveAuthenticationManager());

		NegatedServerWebExchangeMatcher negateWhiteList = new NegatedServerWebExchangeMatcher(ServerWebExchangeMatchers.pathMatchers(AUTH_WHITELIST));
		authenticationWebFilter.setRequiresAuthenticationMatcher(negateWhiteList);

		authenticationWebFilter.setServerAuthenticationConverter(serverAuthenticationConverter());
		authenticationWebFilter.setSecurityContextRepository(serverSecurityContextRepository());

		authenticationWebFilter.setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(serverAuthenticationEntryPoint()));
		return authenticationWebFilter;
	}

	/*@Bean
	ExceptionTranslationWebFilter exceptionTranslationWebFilter(){

		ExceptionTranslationWebFilter exceptionTranslationWebFilter = new ExceptionTranslationWebFilter();

		exceptionTranslationWebFilter.setAccessDeniedHandler((exchange, denied) -> Mono.error(new AccessDeniedException("访问禁止")));
		return exceptionTranslationWebFilter;
	}*/


	/*@Bean
	ServerAccessDeniedHandler serverAccessDeniedHandler(){

		return (exchange, denied) -> Mono.error(new AccessDeniedException("访问禁止"));
	}*/

	/**
	 * url动态拦截鉴权
	 *
	 * @author sunmj
	 * @date 2019/11/21
	 */
	@Bean
	WebFilter accessWebFilter(){

		return (exchange, chain) -> {
			String path = exchange.getRequest().getPath().pathWithinApplication().value();
//			log.info("path : {}", path);

//			Arrays.stream(AUTH_WHITELIST).map(s -> new StringBuffer(s));

			ServerWebExchangeMatchers.pathMatchers(AUTH_WHITELIST)
					.matches(exchange)
					.map(matchResult -> matchResult.isMatch())
					.subscribe(IgnoreAuthWhitelist::set);

			//日志跟踪
			/*ServerWebExchangeMatchers.pathMatchers(AUTH_WHITELIST)
					.matches(exchange)
					.map(matchResult -> matchResult.isMatch())
					.subscribe(System.out::println);*/

//			log.info(" Auth.get() : {}", IgnoreAuthWhitelist.get());
			//排除.pathMatchers(AUTH_WHITELIST).permitAll()
			if(IgnoreAuthWhitelist.get()){
				return chain.filter(exchange);
			}

			DelegatingReactiveAuthorizationManager.Builder builder = DelegatingReactiveAuthorizationManager.builder();

			AuthorityReactiveAuthorizationManager authorityManager = AuthorityReactiveAuthorizationManager.hasAuthority(path);

			//参考 org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec.Access
			builder.add(new ServerWebExchangeMatcherEntry<>(
					ServerWebExchangeMatchers.pathMatchers(path), authorityManager));


//			builder.add((new ServerWebExchangeMatcherEntry<>(
//					ServerWebExchangeMatchers.pathMatchers(AUTH_WHITELIST), (a, e) -> Mono.just(new AuthorizationDecision(true)))));

			DelegatingReactiveAuthorizationManager manager = builder.build();

			//参考 org.springframework.security.web.server.authorization.AuthorizationWebFilter.filter
			return ReactiveSecurityContextHolder.getContext()
					.filter(c -> c.getAuthentication() != null)
					.map(SecurityContext::getAuthentication)
					.as(authentication -> manager.verify(authentication, exchange))
					.switchIfEmpty(chain.filter(exchange));
		};
	}

	/**
	 * accessDeniedHandler
	 *
	 * @author sunmj
	 * @date 2019/11/26
	 */
	@Bean
	ServerAccessDeniedHandler accessDeniedHandler(){

		return (exchange, denied) -> {
			return Mono.defer(() -> Mono.just(exchange.getResponse()))
					.flatMap(response -> {
						response.setStatusCode(HttpStatus.FORBIDDEN);
						response.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
						DataBufferFactory dataBufferFactory = response.bufferFactory();
						DataBuffer buffer = dataBufferFactory.wrap(String.format("{\"msg\" : \"%s\"}", HttpStatus.FORBIDDEN.getReasonPhrase()).getBytes(
								Charset.defaultCharset()));
						return response.writeWith(Mono.just(buffer))
								.doOnError( error -> DataBufferUtils.release(buffer));
					});
		};
	}

	/**
	 * serverAuthenticationEntryPoint
	 *
	 * @author sunmj
	 * @date 2019/11/26
	 */
	@Bean
	ServerAuthenticationEntryPoint serverAuthenticationEntryPoint(){

		return (exchange, e) -> {
			return Mono.defer(() -> Mono.just(exchange.getResponse()))
					.flatMap(response -> {
						response.setStatusCode(HttpStatus.UNAUTHORIZED);
						response.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
						DataBufferFactory dataBufferFactory = response.bufferFactory();
						DataBuffer buffer = dataBufferFactory.wrap(String.format("{\"msg\" : \"%s\"}", HttpStatus.UNAUTHORIZED.getReasonPhrase()).getBytes(
								Charset.defaultCharset()));
						return response.writeWith(Mono.just(buffer))
								.doOnError( error -> DataBufferUtils.release(buffer));
					});
		};
	}

	/**
	 * 是否需要认证
	 *
	 * @author sunmj
	 * @date 2019/11/22
	 */
	@Slf4j
	public static class IgnoreAuthWhitelist{

		private static ThreadLocal<Boolean> threadLocal = new ThreadLocal<>();

		private IgnoreAuthWhitelist(){}

		public static void set(boolean auth){
			threadLocal.set(auth);
		}

		public static boolean get(){
			return threadLocal.get();
		}

	}

}


