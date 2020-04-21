package com.web.config;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 解决@MultiRequestBody list参数显示传参的问题
 *
 * @author sunmingji
 * @date 2020-01-02
 */
@Slf4j
public class CustomMessageConverterMethodArgumentResolver extends AbstractMessageConverterMethodArgumentResolver {


	public CustomMessageConverterMethodArgumentResolver(List<HttpMessageConverter<?>> converters) {
		super(converters);
	}

	public CustomMessageConverterMethodArgumentResolver(List<HttpMessageConverter<?>> converters, List<Object> requestResponseBodyAdvice) {
		super(converters, requestResponseBodyAdvice);
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(CustomRequestBody.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {


		Object arg = readWithMessageConverters(webRequest, parameter, parameter.getNestedGenericParameterType());

		return arg;
	}

	@Override
	protected ServletServerHttpRequest createInputMessage(NativeWebRequest webRequest) {

		HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

		Assert.state(servletRequest != null, "No HttpServletRequest");
		return new ServletServerHttpRequest(servletRequest);

	}

	/**
	 * 需要重新包装inputMessage
	 *
	 * @author sunmj
	 * @date 2020/1/2
	 */
	@Override
	protected <T> Object readWithMessageConverters(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType) throws IOException, HttpMediaTypeNotSupportedException, HttpMessageNotReadableException {

		InputStream inputStream = inputMessage.getBody();

		CustomRequestBody parameterAnnotation = parameter.getParameterAnnotation(CustomRequestBody.class);

		String value = parameterAnnotation.value();

		if(StringUtils.isEmpty(value)){
			throw new IllegalArgumentException("Request part name for argument type [" +
					parameter.getNestedParameterType().getName() +
					"] not specified, and parameter name information not found in class file either.");
		}
		//取出json
		String bodyStr = IOUtils.toString(inputStream, Charset.forName("UTF-8"));

		if(StringUtils.isEmpty(bodyStr)){
			return super.readWithMessageConverters(inputMessage, parameter, targetType);
		}

		Annotation[][] parameterAnnotations = parameter.getMethod().getParameterAnnotations();

		//获取此方法中所有CustomRequestBody的参数
		int length = parameterAnnotations.length;

		List<String> valueList = new ArrayList<>();

		for (int i = 0; i < length; i++) {

			if (parameterAnnotations[i] != null) {
				Annotation[] annotationArray = parameterAnnotations[i];

				for(Annotation annotation : annotationArray){

					if(annotation instanceof CustomRequestBody){
						CustomRequestBody customRequestBody = CustomRequestBody.class.cast(annotation);

						valueList.add(customRequestBody.value());
					}
				}
			}
		}

		//替换未解析的模板
		for(String valueItem : valueList){

			valueItem = "{@" + valueItem + "}";

			bodyStr = bodyStr.replace(valueItem, "\"\"");
		}

		JSONObject jo = JSONObject.parseObject(bodyStr);

		String string = jo.getString(value);


		log.info(" string {}", string);
		ServletServerHttpRequest httpRequest = (ServletServerHttpRequest) inputMessage;

		//重新包装
		CustomHttpServletRequestWrapper customHttpServletRequestWrapper = new CustomHttpServletRequestWrapper(httpRequest.getServletRequest(), string);

		ServletServerHttpRequest serverHttpRequest = new ServletServerHttpRequest(customHttpServletRequestWrapper);

		return super.readWithMessageConverters(serverHttpRequest, parameter, targetType);
	}

}
