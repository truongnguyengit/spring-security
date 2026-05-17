package com.truong.service;

import java.sql.SQLException;
import java.util.List;

import com.truong.common.exception.CustomException;
import com.truong.entity.Employee;

public interface EmployeeService {

	Employee findOne(int id);
	
	Employee findByUsername(String username);
	
	List<Employee> findAll() throws SQLException;
	
	public Employee getProfileOfCurrentUser() throws CustomException;
}
