package com.webmvc.security.controlelr;

import com.alibaba.fastjson.JSONObject;
import com.webmvc.security.config.security.token.MobileCodeAuthenticationToken;
import com.webmvc.security.model.Login;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 登录 绕过AbstractAuthenticationProcessingFilter与AuthenticationManager.authenticate
 *
 * @author sunmingji
 * @date 2020-04-28
 */
@RestController
public class LoginController {

	public static ConcurrentHashMap<String, Authentication> loginMap = new ConcurrentHashMap<>();

	public static ConcurrentHashMap<String, String> codeMap = new ConcurrentHashMap<>();

	@Autowired
	private AuthenticationManagerBuilder authenticationManagerBuilder;

//
//	@Autowired
//	private AuthenticationManager authenticationManager;

	/**
	 * 表单登录
	 *
	 * @author sunmj
	 * @date 2020/6/12
	 */
	@RequestMapping("/login")
	public JSONObject login(@RequestParam String userName, @RequestParam String passwd){

		JSONObject data = new JSONObject();

		//TODO queryUserByUsernameAndPasswd
		//

		//REFER org.springframework.security.web.authentication.www.BasicAuthenticationFilter.doFilterInternal
		Authentication authentication = new UsernamePasswordAuthenticationToken(userName, passwd);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		String token = getToken();
		loginMap.put(token, authentication);
		data.put("token", token);
		return data;
	}

	/**
	 * 手机号登录
	 * 未走security认证流程(自己处理成功则直接放入SecurityContextHolder中)
	 *
	 * @author sunmj
	 * @date 2020/4/29
	 */
	@RequestMapping("/loginMobile")
	public JSONObject loginMobile(@RequestParam String mobile, @RequestParam String code){

		JSONObject data = new JSONObject();
		//TODO verify code by mobile
		String codeFromCache = codeMap.get(mobile);

		/*
			mobile 替换 使用mobile查出来user.id
			code可使用魔法值
		 */
		Authentication authentication = new UsernamePasswordAuthenticationToken(mobile, code);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		return data;
	}

	/**
	 * json格式登录
	 *
	 * @author sunmj
	 * @date 2020/6/12
	 */
	@RequestMapping("/loginJson")
	public JSONObject login(@RequestBody Login login){

		JSONObject data = new JSONObject();

		SecurityContext context = SecurityContextHolder.getContext();
		Function<String, String> passwordEncoder = password -> password;

		//
		Authentication authentication = new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPasswd());
		context.setAuthentication(authentication);

		String token = getToken();
		loginMap.put(token, authentication);
		data.put("token", token);
		return data;
	}

	/*
		下面登录使用security认证流程 并支持多种登录方式
	 */


	/**
	 * 与UsernamePasswordAuthenticationFilter逻辑一致authenticationManager.authenticate(authentication)
	 * 需要创建AuthenticationProvider 多种登录方式需要创建多个AuthenticationProvider 与token
	 * https://www.jianshu.com/p/779d3071e98d
	 * @author sunmj
	 * @date 2020/4/29
	 */
	@RequestMapping("/loginJsonBySecurity")
	public JSONObject loginJsonBySecurity(@RequestBody Login login){

		JSONObject data = new JSONObject();

		Authentication authentication = new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPasswd());

		//REFER org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.attemptAuthentication
//		Authentication authenticate = authenticationManager.authenticate(authentication);

		Authentication authenticate = authenticationManagerBuilder.getObject().authenticate(authentication);

		data.put("token", getToken());
//		data.put("authenticate", JSON.toJSONString(authenticate));
		return data;
	}

	/**
	 * 手机登录
	 *
	 * @author sunmj
	 * @date 2020/6/12
	 */
	@RequestMapping("/loginMobileBySecurity")
	public JSONObject loginMobileBySecurity(@RequestParam String mobile, String code){

		JSONObject data = new JSONObject();

		Authentication authentication = new MobileCodeAuthenticationToken(mobile, code);

		Authentication authenticate = authenticationManagerBuilder.getObject().authenticate(authentication);

		data.put("token", getToken());

		return data;
	}


	private String getToken(){
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}
