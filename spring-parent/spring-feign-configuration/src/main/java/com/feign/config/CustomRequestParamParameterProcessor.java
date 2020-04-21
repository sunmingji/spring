package com.feign.config;

import feign.MethodMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import static feign.Util.checkState;
import static feign.Util.emptyToNull;


/**
 * 类注释
 *
 * @author sunmingji
 * @date 2019-12-27
 */
@Slf4j
public class CustomRequestParamParameterProcessor implements AnnotatedParameterProcessor {

	private static final Class<CustomRequestParam> ANNOTATION = CustomRequestParam.class;

	@Override
	public Class<? extends Annotation> getAnnotationType() {
		return ANNOTATION;
	}



	@Override
	public boolean processArgument(AnnotatedParameterContext context, Annotation annotation, Method method) {

		int parameterIndex = context.getParameterIndex();
		Class<?> parameterType = method.getParameterTypes()[parameterIndex];
		MethodMetadata data = context.getMethodMetadata();

		if (Map.class.isAssignableFrom(parameterType)) {
			checkState(data.queryMapIndex() == null, "Query map can only be present once.");
			data.queryMapIndex(parameterIndex);

			return true;
		}

		CustomRequestParam requestParam = ANNOTATION.cast(annotation);
		String name = requestParam.value();
		checkState(emptyToNull(name) != null,
				"CustomRequestParam.value() was empty on parameter %s",
				parameterIndex);
		context.setParameterName("@" + name);

		data.formParams().add("@" + name);

		data.indexToExpander().put(context.getParameterIndex(), new JsonStrExpander());
		return true;
	}
}
