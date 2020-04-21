package com.web.config;


import lombok.extern.slf4j.Slf4j;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * HttpServletRequestWrapper包装类
 *
 * @author sunmj
 * @date 2019-05-07
 */
@Slf4j
public class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {

	/**
	 * 请求体的内容
	 * Content-Type: application-json
	 */
	private byte[] body;

	private String requestBody;

	/**
	 * 所有参数的Map集合
	 * 1、GET请求或POST请求，直接在url后面跟上的参数，如：?schoolId=569
	 * 2、POST请求，但是Content-Type: x-www-form-urlencoded的
	 */
	private Map<String, String[]> parameterMap;

	/**
	 * 重新包装request流
	 *
	 * @author sunmj
	 * @date 2020/4/20
	 */
	public CustomHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
		parameterMap = new HashMap(request.getParameterMap());
		String sessionStream = getBodyString(request);
		requestBody = sessionStream;
		body = sessionStream.getBytes(Charset.forName("UTF-8"));
	}

	/**
	 * 自定义参数
	 *
	 * @author sunmj
	 * @date 2020/1/2
	 */
	public CustomHttpServletRequestWrapper(HttpServletRequest request, String body) {
		super(request);

		this.body = body.getBytes(Charset.forName("UTF-8"));
	}


	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(getInputStream()));
	}

	/**
	 * 在使用@RequestBody注解的时候，其实框架是调用了getInputStream()方法，所以我们要重写这个方法
	 *
	 * @author sunmj
	 * @date 2019/7/26
	 */
	@Override
	public ServletInputStream getInputStream() {

		final ByteArrayInputStream bais = new ByteArrayInputStream(body);
		return new ServletInputStream() {

			@Override
			public int read() {

				return bais.read();
			}

			@Override
			public boolean isFinished() {

				return false;
			}

			@Override
			public boolean isReady() {

				return false;
			}

			@Override
			public void setReadListener(ReadListener readListener) {

			}
		};
	}

	@Override
	public void setAttribute(String name, Object o) {
		super.setAttribute(name, o);
	}


	@Override
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(parameterMap.keySet());
	}

	@Override
	public String getParameter(String name) {
		String[] values = parameterMap.get(name);
		if (values != null) {
			if (values.length == 0) {
				return "";
			}
			return values[0];
		} else {
			return null;
		}
	}

	@Override
	public String[] getParameterValues(String name) {
		return parameterMap.get(name);
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return parameterMap;
	}

	/**
	 * 获取请求Body
	 *
	 * @author sunmj
	 * @date 2019/7/24
	 */
	private String getBodyString(final ServletRequest request) {

		StringBuilder sb = new StringBuilder();
		try (
				InputStream inputStream = cloneInputStream(request.getInputStream());
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")))
		) {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


		return sb.toString();
	}

	/**
	 * 复制输入流
	 *
	 * @author sunmj
	 * @date 2019/7/24
	 */
	private InputStream cloneInputStream(ServletInputStream inputStream) {

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		try {
			while ((len = inputStream.read(buffer)) > -1) {
				byteArrayOutputStream.write(buffer, 0, len);
			}
			byteArrayOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
	}
}
