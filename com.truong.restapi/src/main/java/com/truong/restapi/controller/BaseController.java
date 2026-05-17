package com.truong.restapi.controller;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.truong.common.utils.StringValue;
import com.truong.service.EmployeeService;

public class BaseController {
	
	
	@Autowired
	EmployeeService employeeService;
	
	protected List<String> getAuthorities() {
	    var authentication = SecurityContextHolder.getContext().getAuthentication();
	    
	    if (authentication == null) {
	        return List.of();
	    }

	    // Lấy tập hợp các GrantedAuthority có sẵn trong SecurityContext
	    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

	    // Chuyển đổi thành List<String> để dễ xử lý logic hoặc phân quyền bằng mã lệnh
	    return authorities.stream()
	            .map(GrantedAuthority::getAuthority)
	            .collect(Collectors.toList());
	}
	
	public boolean isOwner() throws Exception {
		return this.getAuthorities().stream().anyMatch(x -> x.equals(StringValue.OWNER));
	}

}
