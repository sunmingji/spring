package com.wxadt.wxcep.controller;

import com.wxadt.wxcep.config.shiro.CustomAuthorizingRealm;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2021-03-24
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private CustomAuthorizingRealm customAuthorizingRealm;

	@RequestMapping("/queryUser")
	public ResponseEntity queryUser(){
		Subject subject = SecurityUtils.getSubject();
		Object principal = subject.getPrincipals().getPrimaryPrincipal();
		return ResponseEntity.ok(principal);
	}

	@RequestMapping("/queryAuthorizationInfo")
	public ResponseEntity queryAuthInfo(){
		Subject subject = SecurityUtils.getSubject();
		AuthorizationInfo authInfo = customAuthorizingRealm.queryAuthorizationInfo(subject.getPrincipals());
		return ResponseEntity.ok(authInfo);
	}
}
