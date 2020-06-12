package com.test.controller;

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


	@RequestMapping("/test")
	public JSONObject test(){

		JSONObject jo = new JSONObject();


		jo.put("error", "404");
		return jo;
	}

	@RequestMapping("/exception")
	public JSONObject exception(){

		throw new RuntimeException("exception");
	}

	@RequestMapping("/string")
	public String string(){

		JSONObject jo = new JSONObject();

		jo.put("data", "data");

		return new String(jo.toJSONString());
	}
}
