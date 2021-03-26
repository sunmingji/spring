package com.test.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.test.gateway.security.SecurityConfig;
import com.test.gateway.security.TokenAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.cloud.gateway.filter.NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2019-11-14
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AccessGatewayFilter implements GlobalFilter, Ordered{

	private static final String[] AUTH_WHITELIST = new String[]{"/ucenter-web/login"};

	private final ReactiveAuthenticationManager authenticationManager;

	private final ServerSecurityContextRepository contextRepository;

	@Override
	public int getOrder() {
		// -1 is response write filter, must be called before that
		return WRITE_RESPONSE_FILTER_ORDER - 1;
	}


	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		// 定义新的消息头
		HttpHeaders headers = new HttpHeaders();
		exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.putAll(exchange.getResponse().getHeaders());

		ServerHttpResponseDecorator responseDecorator = new ServerHttpResponseDecorator(
				exchange.getResponse()){

			//参考 https://blog.csdn.net/a294634473/article/details/90716859 解决ResponseBody不完整的问题
			@Autowired
			public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
				if (getStatusCode().equals(HttpStatus.OK) && body instanceof Flux) {
					Flux<? extends DataBuffer> fluxBody = Flux.from(body);
					return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
						List<String> list = new ArrayList();
						StringBuffer stringBuffer = new StringBuffer();
						dataBuffers.forEach(dataBuffer -> {
							byte[] content = new byte[dataBuffer.readableByteCount()];
							dataBuffer.read(content);
							DataBufferUtils.release(dataBuffer);
							try {
								list.add(new String(content, "utf-8"));
								stringBuffer.append(new String(content, "utf-8"));
							} catch (IOException e) {
								e.printStackTrace();
							}
						});
						String s = stringBuffer.toString();

						JSONObject jo = JSONObject.parseObject(s, JSONObject.class);
//						log.info(" jo : {} ", jo.toJSONString());
						ServerWebExchangeMatchers.pathMatchers(AUTH_WHITELIST)
								.matches(exchange)
								.map(matchResult -> matchResult.isMatch())
								.subscribe(SecurityConfig.IgnoreAuthWhitelist::set);

						if(SecurityConfig.IgnoreAuthWhitelist.get()){//认证用户
							//函数式编程
							/*List<String> strings = Arrays.asList(responseData);
							strings.stream()
									.map(s -> JSONObject.parseObject(responseData, JSONObject.class))
									.map(jo -> jo.getString("data")).filter(str -> StringUtils.isEmpty(str))
									.map(data -> JSONObject.parseObject(data, JSONObject.class))//获取data
									.filter(dataJO -> dataJO.getString("loginMsg").equals("pass"))
									.map(dataJO -> new TokenAuthentication(dataJO.getString("token"), "rob", "rob"))
									.map(contextRepository.save(exchange, new SecurityContextImpl(authenticationToken))));*/

							//命令式编程
							String data = jo.getString("data");
							JSONObject dataJo = JSONObject.parseObject(data, JSONObject.class);

							if("pass".equals(dataJo.getString("loginMsg"))){
								final String token = dataJo.getString("token");

								JSONObject userPageModelJO = (JSONObject)dataJo.get("userPageModel");
								String id = userPageModelJO.getString("id");

								//这里存的是userId 保证唯一需要根据userId拉取用户权限
								TokenAuthentication authenticationToken = new TokenAuthentication(token, id, SecurityConfig.PASSWD);//也可以直接授权(使用另一个构造器)
								//存入信息
								authenticationManager.authenticate(authenticationToken)
										.map(a -> a.getCredentials())
										.doOnSuccess(auth -> contextRepository.save(exchange, new SecurityContextImpl(authenticationToken)))
										.then(Mono.just(token))
										.subscribe(System.out::println);
//								contextRepository.save(exchange, new SecurityContextImpl(authenticationToken));//直接保存信息

							}
						}

						int length = jo.toJSONString().getBytes().length;
						headers.setContentLength(length);
						return bufferFactory().wrap(jo.toJSONString().getBytes());
					}));
				}
				return super.writeWith(body);
			};

			@Override
			public HttpHeaders getHeaders() {
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.putAll(super.getHeaders());
				//由于修改了请求体的body，导致content-length长度不确定，因此使用分块编码
				httpHeaders.remove(HttpHeaders.CONTENT_LENGTH);
				httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
				return httpHeaders;
			}

		};

		return chain.filter(exchange.mutate().response(responseDecorator).build());
//		return chain.filter(exchange);
	}

	/**
	 * 网关拒绝，返回401
	 *
	 * @param
	 */
	/*private Mono<Void> unauthorized(ServerWebExchange serverWebExchange) {
		serverWebExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
		DataBuffer buffer = serverWebExchange.getResponse()
				.bufferFactory().wrap(HttpStatus.UNAUTHORIZED.getReasonPhrase().getBytes());
		return serverWebExchange.getResponse().writeWith(Flux.just(buffer));
	}*/
}
