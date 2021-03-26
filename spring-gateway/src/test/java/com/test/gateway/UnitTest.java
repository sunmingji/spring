package com.test.gateway;

import java.util.Arrays;
import java.util.List;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2019-11-24
 */
public class UnitTest {

	public static void main(String[] args){

		final List<String> friends = Arrays.asList("Brian", "Nate", "Neal", "Raju", "Sara", "Scott");

		for(int i = 0; i < friends.size(); i++) {

			System.out.println(friends.get(i));

		}

		for(String name : friends) {

			System.out.println(name);

		}

		friends.forEach((final String name) -> System.out.println(name));

		friends.forEach((name) -> System.out.println(name));

		friends.forEach(name -> System.out.println(name));

		friends.forEach(System.out::println);
	}

}
