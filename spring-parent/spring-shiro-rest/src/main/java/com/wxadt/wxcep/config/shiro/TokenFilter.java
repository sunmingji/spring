package com.wxadt.wxcep.config.shiro;

import com.alibaba.fastjson.JSONObject;
import com.wxadt.wxcep.model.User;
import com.wxadt.wxcep.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2021-03-16
 */
@Slf4j
public class TokenFilter extends BasicHttpAuthenticationFilter {

	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {

		String token = request.getParameter("token");

		if(StringUtils.isEmpty(token)){
			this.response401(response, "token不存在");
			return false;
		}

		User user = UserUtil.usermap.get(token);

		if(user == null){
			this.response401(response, "登录超时");
			return false;
		}

		Subject subject = SecurityUtils.getSubject();
		CustomAuthenticationToken authenticationToken =  new CustomAuthenticationToken(token);
		subject.login(authenticationToken);

		return true;
	}

	/**
	 * 无需转发，直接返回Response信息
	 */
	private void response401(ServletResponse response, String msg) {
		HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
		httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
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
