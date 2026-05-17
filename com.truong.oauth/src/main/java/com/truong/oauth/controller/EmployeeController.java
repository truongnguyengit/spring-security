package com.truong.oauth.controller;

import java.sql.SQLException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.truong.common.exception.CustomException;
import com.truong.common.utils.BaseResponse;
import com.truong.oauth.response.EmployeeResponse;
import com.truong.service.EmployeeService;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController extends BaseController{
	
	private final EmployeeService employeeService;
	
	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	@GetMapping("")
	public ResponseEntity<BaseResponse<Object>> getAll() throws CustomException, SQLException {
		
		BaseResponse<Object> response = new BaseResponse<>();
		
		this.employeeService.getProfileOfCurrentUser();
		
		response.setData(new EmployeeResponse().mapToList(employeeService.findAll()));
		
		return new ResponseEntity<>(response, HttpStatus.OK);
		
		
	}
}
