package com.wxadt.wxcep.config.shiro;

import com.wxadt.wxcep.model.User;
import com.wxadt.wxcep.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2021-03-05
 */
@Slf4j
@Component
public class CustomAuthorizingRealm  extends AuthorizingRealm {

	public CustomAuthorizingRealm() {
		super(new AllowAllCredentialsMatcher());
	}

	@Override
	public boolean supports(AuthenticationToken authenticationToken) {
		return authenticationToken instanceof CustomAuthenticationToken;
	}

	/**
	 * 认证
	 *
	 * @author sunmj
	 * @date 2021/3/24
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		CustomAuthenticationToken authenticationToken = (CustomAuthenticationToken) token;
		User user = UserUtil.usermap.get(authenticationToken.getToken());
//		log.info("doGetAuthenticationInfo user {}", JSON.toJSONString(user));
		SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, user.getPasswd(), this.getName());
		return info;
	}

	/**
	 * 授权
	 *
	 * @author sunmj
	 * @date 2021/3/24
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		User user = (User)principals.getPrimaryPrincipal();

//		log.info("doGetAuthorizationInfo user {}", JSON.toJSONString(user));
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

		String account = user.getAccount();
		//授权
		info.addStringPermissions(UserUtil.permMap.get(account));
		info.addStringPermissions(UserUtil.menupermMap.get(account));
		return info;
	}

	/**
	 * 获取用户授权信息
	 *
	 * @author sunmj
	 * @date 2021/3/25
	 */
	public AuthorizationInfo queryAuthorizationInfo(PrincipalCollection principals){
		return this.getAuthorizationInfo(principals);
	}
}
