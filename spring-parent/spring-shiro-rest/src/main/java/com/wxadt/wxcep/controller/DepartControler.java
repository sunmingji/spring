package com.wxadt.wxcep.controller;

import com.wxadt.wxcep.model.Depart;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 类注释
 *
 * @author sunmingji
 * @date 2021-03-25
 */
@RestController
@RequestMapping("/depart")
public class DepartControler {

	@RequestMapping("/queryDepart")
	public ResponseEntity queryDepart(){

		Depart depart = new Depart();
		depart.setId((int)(Math.random() * 1000));
		depart.setName("depart");

		return ResponseEntity.ok(depart);
	}
}
