package com.webmvc.security.controlelr;

import com.alibaba.fastjson.JSONObject;
import com.webmvc.security.model.Login;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
//
//	@Autowired
//	private AuthenticationManager authenticationManager;

	@RequestMapping("/login")
	public JSONObject login(@RequestParam String userName, String passwd){

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
	 *
	 *
	 * @author sunmj
	 * @date 2020/4/29
	 */
	@RequestMapping("/loginMobile")
	public JSONObject loginMobile(@RequestParam String mobile, String code){

		JSONObject data = new JSONObject();
		//TODO verify code by mobile
		String codeFromCache = codeMap.get(mobile);

		return data;
	}

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

	/**
	 * 与UsernamePasswordAuthenticationFilter逻辑一致authenticationManager.authenticate(authentication)
	 *
	 * @author sunmj
	 * @date 2020/4/29
	 */
	@RequestMapping("/loginJsonByAuth")
	public JSONObject loginJsonByAuth(@RequestBody Login login){

		JSONObject data = new JSONObject();

		Authentication authentication = new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPasswd());

		//REFER org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.attemptAuthentication
//		Authentication authenticate = authenticationManager.authenticate(authentication);

		data.put("token", getToken());
//		data.put("authenticate", JSON.toJSONString(authenticate));
		return data;
	}


	private String getToken(){
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}
