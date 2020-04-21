package com.feign.config;

import com.alibaba.fastjson.JSON;
import feign.Param;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2019-12-27
 */
public class JsonStrExpander implements Param.Expander {
	@Override
	public String expand(Object value) {
		return JSON.toJSONString(value);
	}
}
