package com.feign.config;


import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.ValueConstants;

import java.lang.annotation.*;

/**
 * copy @RequestParam
 *
 * @author sunmingji
 * @date 2019-12-27
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomRequestParam {

	@AliasFor("name")
	String value() default "";

	@AliasFor("value")
	String name() default "";

	boolean required() default true;

	String defaultValue() default ValueConstants.DEFAULT_NONE;

}