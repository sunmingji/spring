package com.webmvc.security.controlelr;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2020-04-28
 */
@RestController
@RequestMapping("/dept")
public class DeptController {

	@RequestMapping("/queryDept")
	public JSONObject queryDept(){

		JSONObject data = new JSONObject();

		data.put("routine", "/queryDept");
		return data;
	}
}
