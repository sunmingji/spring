package com.wxadt.wxcep.config;

import com.wxadt.wxcep.config.shiro.CustomAuthorizingRealm;
import com.wxadt.wxcep.config.shiro.CustomPermissionsAuthorizationFilter;
import com.wxadt.wxcep.config.shiro.TokenFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.filter.mgt.DefaultFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.DefaultWebSubjectFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * shiro无状态配置
 *
 * @author sunmingji
 * @date 2021-03-05
 */
@Slf4j
@Configuration
public class ShiroConfig {


	/**
	 * 安全管理器
	 * 禁用session
	 *
	 * @author sunmj
	 * @date 2021/3/24
	 */
	@Bean
	public DefaultWebSecurityManager securityManager(CustomAuthorizingRealm shiroRealm) {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		// 禁用sessionStorage
		((DefaultSessionStorageEvaluator) ((DefaultSubjectDAO) securityManager.getSubjectDAO()).getSessionStorageEvaluator()).setSessionStorageEnabled(false);
		// 禁用session会话调度器
		DefaultSessionManager sessionManager = new DefaultSessionManager();
		sessionManager.setSessionValidationSchedulerEnabled(false);
		securityManager.setSessionManager(sessionManager);
		// 禁止创建session
		securityManager.setSubjectFactory(defaultWebSubjectFactory());
		// 设置认证和授权服务
		securityManager.setRealm(shiroRealm);
		// 设置SecurityUtils的静态方法 获取用户等使用 SecurityManager
		SecurityUtils.setSecurityManager(securityManager);
		return securityManager;
	}


	@Bean
	public ShiroFilterFactoryBean shiroFilter(DefaultWebSecurityManager securityManager) {
		ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
		shiroFilterFactoryBean.setSecurityManager(securityManager);
		// 定义拦截器
		Map<String, Filter> filterMap = new LinkedHashMap<>();
		filterMap.put("token", new TokenFilter());
		filterMap.put(DefaultFilter.perms.name(), new CustomPermissionsAuthorizationFilter());
		shiroFilterFactoryBean.setFilters(filterMap);
		// 定义url过滤
		Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
		// 所有url都必须认证通过才可以访问
		// authc 必须认证通过才可以访问
		filterChainDefinitionMap.put("/login/**", DefaultFilter.anon.name());

		// 其他所有请求全部走token验证
		filterChainDefinitionMap.put("/**", "token".concat(",").concat(DefaultFilter.perms.name()));
		shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
		return shiroFilterFactoryBean;
	}

	/**
	 * 启用shrio授权注解拦截方式，AOP式方法级权限检查
	 */
	@Bean
	public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
		AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
		authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
		return authorizationAttributeSourceAdvisor;
	}

	/**
	 * 禁用seesion
	 *
	 * @author sunmj
	 * @date 2021/3/5
	 */
	public DefaultWebSubjectFactory defaultWebSubjectFactory(){
		DefaultWebSubjectFactory defaultWebSubjectFactory = new DefaultWebSubjectFactory(){
			@Override
			public Subject createSubject(SubjectContext context) {
				//不创建session
				context.setSessionCreationEnabled(false);
				return super.createSubject(context);
			}
		};
		return defaultWebSubjectFactory;
	}
}
