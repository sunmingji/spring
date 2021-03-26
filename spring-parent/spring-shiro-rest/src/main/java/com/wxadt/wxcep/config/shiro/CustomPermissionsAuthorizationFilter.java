package com.wxadt.wxcep.config.shiro;

import com.alibaba.fastjson.JSONObject;
import com.wxadt.wxcep.model.User;
import com.wxadt.wxcep.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2021-03-25
 */
@Slf4j
public class CustomPermissionsAuthorizationFilter extends AuthorizationFilter {

	/**
	 * 开始鉴权
	 *
	 * @author sunmj
	 * @date 2021/3/25
	 */
	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {

		Subject subject = SecurityUtils.getSubject();
		User user = (User)subject.getPrincipals().getPrimaryPrincipal();
		List<String> menulist = UserUtil.menupermMap.get(user.getAccount());
		HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
		String requestURI = httpServletRequest.getRequestURI();

		//目录权限
		if(!CollectionUtils.isEmpty(menulist)){
			boolean permitted = false;
			for (String m : menulist) {
				permitted = this.pathsMatch(m, requestURI);
			}
			if(permitted){
				return permitted;
			}
		}

		//接口权限
		return subject.isPermitted(requestURI);
	}

	/**
	 * 鉴权失败
	 *
	 * @author sunmj
	 * @date 2021/3/25
	 */
	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
		this.response403(response, "权限不足");
		return false;
	}

	/**
	 * 无需转发，直接返回Response信息
	 */
	private void response403(ServletResponse response, String msg) {
		HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
		httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
		httpServletResponse.setCharacterEncoding("UTF-8");
		httpServletResponse.setContentType("application/json; charset=utf-8");
		try (PrintWriter out = httpServletResponse.getWriter()) {
			JSONObject jo = new JSONObject();
			jo.put("msg", msg);
			out.append(jo.toJSONString());
		} catch (IOException e) {
			log.error("直接返回Response信息出现IOException异常:{}", e.getMessage());
			throw new RuntimeException("直接返回Response信息出现IOException异常:" + e.getMessage());
		}
	}
}
