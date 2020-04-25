package com.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.web.config.CustomRequestBody;
import com.web.model.Dept;
import com.web.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2020-04-25
 */
@Slf4j
@RestController
public class Controller {

	@PostMapping("/quryUserDeptByModels")
	public JSONObject quryUserDeptByModels(@CustomRequestBody("user")User user, @CustomRequestBody("dept") Dept dept,
										   @CustomRequestBody("userArray") User[] userArray,
										   @CustomRequestBody("deptList") List<Dept> deptList){

		JSONObject data = new JSONObject();

		data.put("user", user);
		data.put("dept", dept);
		data.put("userArray", userArray);
		data.put("deptList", deptList);

		return data;
	}
}
