package com.wxadt.wxcep.controller;

import com.alibaba.fastjson.JSONObject;
import com.wxadt.wxcep.model.User;
import com.wxadt.wxcep.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2021-03-05
 */
@Slf4j
@Controller
@RequestMapping("/login")
public class LoginController {

	@RequestMapping("/loginByAccount")
	@ResponseBody
	public ResponseEntity loginByAccount(String account){

		User user = new User();

		user.setAccount(account);
		user.setPasswd("passwd");
		user.setId((int)(Math.random() * 1000));

		String uuid = UUID.randomUUID().toString().replace("-", "");
		UserUtil.usermap.put(uuid, user);

		JSONObject jo = new JSONObject();
		jo.put("token", uuid);
		return ResponseEntity.ok(jo);
	}

	@Test
	public void uuid(){
		log.info("uuid {}", UUID.randomUUID().toString().replace("-", ""));
	}

 }
