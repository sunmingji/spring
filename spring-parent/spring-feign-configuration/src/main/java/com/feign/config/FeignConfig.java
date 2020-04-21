package com.feign.config;

import feign.*;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.form.spring.SpringFormEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;
import org.springframework.cloud.openfeign.annotation.PathVariableParameterProcessor;
import org.springframework.cloud.openfeign.annotation.RequestHeaderParameterProcessor;
import org.springframework.cloud.openfeign.annotation.RequestParamParameterProcessor;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义feign
 *
 * @author zpy
 * @date 2019-12-21
 */
@Slf4j
@Configuration
public class FeignConfig {

	@Autowired
	private ObjectFactory<HttpMessageConverters> messageConverters;

	/**
	 * 日志级别
	 *
	 * @author sunmj
	 * @date 2020/3/19
	 */
	@Bean
	Logger.Level feignLoggerLevel() {
		return Logger.Level.FULL;
	}
	/**
	 * feign请求拦截器
	 *
	 * @return
	 */
	@Bean
	public RequestInterceptor requestInterceptor(){
		return new FeignBasicAuthRequestInterceptor();
	}

	@Bean
	public Contract feignContract() {

		return springMvcContract();

//		return new feign.Contract.Default();
	}


	/**
	 * 重写异常信息
	 *
	 * feign.codec.ErrorDecoder
	 *
	 * @author sunmj
	 * @date 2020/3/19
	 */
	@Bean
	@Primary
	@Scope("prototype")
	public ErrorDecoder errorDecoder(){

		return (methodKey, response) ->{

			try {
				String reponseBodyStr = Util.toString(response.body().asReader());

				Request request = response.request();

				String requestBodyStr = "";
				if(null != request.body()){

					requestBodyStr = new String(request.body());
				}


				log.warn(" request method {}, url {}, body {}", request.method(), request.url(),requestBodyStr);
				log.warn(" response status {}, reason{}, body {}, ", response.status(), response.reason(), reponseBodyStr);

			} catch (IOException e) {
				e.printStackTrace();
			}
			return new RuntimeException();
		};
	}

	/**
	 * feign取消重试
	 * NOTE!!! 重试并不是报错以后的重试，而是负载均衡客户端发现远程请求实例不可到达后，去重试其他实例
	 *
	 * source file : feign.Retryer
	 *
	 * @author sunmj
	 * @date 2020/3/23
	 */
	@Bean
	Retryer feignRetryer() {

		//取消
//		return Retryer.NEVER_RETRY;

		//默认
		return new Retryer.Default();
	}

	/**
	 * feign请求超时设置
	 *
	 * @author sunmj
	 * @date 2020/3/23
	 */
	@Bean
	Request.Options requestOptions(ConfigurableEnvironment env){
		int ribbonReadTimeout = env.getProperty("ribbon.ReadTimeout", int.class, 6000);
		int ribbonConnectionTimeout = env.getProperty("ribbon.ConnectTimeout", int.class, 3000);

		return new Request.Options(ribbonConnectionTimeout, ribbonReadTimeout);
	}


	/**
	 * 支持form 文件传输
	 *
	 * @author sunmj
	 * @date 2020/3/19
	 */
	@Bean
	@Primary
	@Scope("prototype")
	public Encoder multipartFormEncoder() {
		return new SpringFormEncoder(new SpringEncoder(messageConverters));
	}

	/**
	 * springMvcContract
	 * 定义多model模板
	 *
	 * @author sunmj
	 * @date 2020/3/18
	 */
	private SpringMvcContract springMvcContract(){

		List<AnnotatedParameterProcessor> annotatedArgumentResolvers = new ArrayList<>();

		annotatedArgumentResolvers.add(new PathVariableParameterProcessor());
		annotatedArgumentResolvers.add(new RequestParamParameterProcessor());
		annotatedArgumentResolvers.add(new RequestHeaderParameterProcessor());
		annotatedArgumentResolvers.add(new CustomRequestParamParameterProcessor());

		return new SpringMvcContract(annotatedArgumentResolvers){

			@Override
			public MethodMetadata parseAndValidateMetadata(Class<?> targetType, Method method) {

				return super.parseAndValidateMetadata(targetType, method);
			}

			@Override
			protected void processAnnotationOnMethod(MethodMetadata data, Annotation methodAnnotation, Method method) {
				super.processAnnotationOnMethod(data, methodAnnotation, method);

				//动态生成 RequestTemplate.bodyTemplate
				Annotation[][] parameterAnnotations = method.getParameterAnnotations();
				int count = parameterAnnotations.length;

				List<String> valueList = new ArrayList<>();
				for (int i = 0; i < count; i++) {

					if (parameterAnnotations[i] != null) {
						Annotation[] annotationArray = parameterAnnotations[i];

						for(Annotation annotation : annotationArray){

							if(annotation instanceof CustomRequestParam){
								CustomRequestParam customRequestParam = (CustomRequestParam) annotation;

								String value = customRequestParam.value();
								valueList.add(value);
							}
						}
					}
				}

				StringBuffer stringBuffer = new StringBuffer("%7B");
				for( int i = 0; i < valueList.size(); i ++){
					String param = valueList.get(i);
					stringBuffer.append("\"" + param + "\"" ).append(":").append("{").append("@" + param).append("}");
					if(i < valueList.size() -1){
						stringBuffer.append(",");
					}
				}

				stringBuffer.append("%7D");

				data.template().bodyTemplate(stringBuffer.toString());
			}
		};
	}


}

