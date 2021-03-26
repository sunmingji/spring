package com.wxadt.wxcep.util;

import com.wxadt.wxcep.model.User;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2021-03-23
 */
public class UserUtil {

	public static ConcurrentHashMap<String, User> usermap = new ConcurrentHashMap<>(16 * 4);

	/**
	 * 具体权限
	 *
	 * @author sunmj
	 * @date 2021/3/26
	 */
	public static ConcurrentHashMap<String, List<String>> permMap = new ConcurrentHashMap<String, List<String>>(16 * 4){

		{
			put("user", Arrays.asList("/user/queryUser", "/depart/queryDepart"));
		}
	};


	/**
	 * 菜单权限
	 *
	 * @author sunmj
	 * @date 2021/3/26
	 */
	public static ConcurrentHashMap<String, List<String>> menupermMap = new ConcurrentHashMap<String, List<String>>(16 * 4){

		{
			put("user", Arrays.asList("/role/**"));
		}
	};





}
