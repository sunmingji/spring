package com.web.config;

import java.lang.annotation.*;

/**
 * 自定义解析参数
 *
 * 此注解只使用 多个requestBody入参的情况 需要严格按照如下格式传参
 * {"userList":[{ "userId" : "1", "userName" : "sunmj"}],"user":{ "userId" : "1", "userName" : "sunmj"}}
 *
 *
 * @author sunmingji
 * @date 2019-12-25
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomRequestBody {

	/**
	 * 解析时用到的JSON的key
	 */
	String value();

	/**
	 * 必传的属性
	 */
	String[] requiredParam() default {};

}
