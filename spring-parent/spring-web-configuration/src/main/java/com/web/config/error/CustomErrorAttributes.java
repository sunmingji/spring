package com.web.config.error;

import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2020-06-05
 */
public class CustomErrorAttributes extends DefaultErrorAttributes {


	public CustomErrorAttributes() {
		super();
	}

	@Override
	public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {

		Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, includeStackTrace);


		/*ServletWebRequest servletWebRequest = (ServletWebRequest)webRequest;
		HttpServletRequest nativeRequest = (HttpServletRequest)servletWebRequest.getNativeRequest();

		RequestAttributes requestAttributes = new ServletRequestAttributes(nativeRequest);*/

		Throwable error = getError(webRequest);

		errorAttributes.put("exception", error.getClass().getName());
		return errorAttributes;
	}
}
