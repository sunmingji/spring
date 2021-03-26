package com.wxadt.wxcep.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试目录权限
 *
 * @author sunmingji
 * @date 2021-03-26
 */
@RestController
@RequestMapping("/role")
public class RoleController {

	@RequestMapping("/queryRoleList")
	public ResponseEntity queryRoleList(){

		JSONArray ja = new JSONArray();
		ja.add("/admin");
		ja.add("/guest");

		return ResponseEntity.ok(ja);
	}

	@RequestMapping("/queryRoleDetail")
	public ResponseEntity queryRoleDetail(String name){

		JSONObject jo = new JSONObject();
		jo.put("id", (int)(Math.random() * 1000));
		jo.put("name", name);

		return ResponseEntity.ok(jo);
	}

}
