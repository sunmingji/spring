package com.web.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2020-06-08
 */
@RestController
public class TestController {

	@RequestMapping("/string")
	public String string(){

		JSONObject jo = new JSONObject();

		jo.put("data", "data");

		return new String(jo.toJSONString());
	}
}
