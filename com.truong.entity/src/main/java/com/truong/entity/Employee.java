package com.truong.entity;

import java.util.List;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.truong.common.utils.Utils;

import jakarta.persistence.Entity;



@Entity
@Table(name = "employees")
public class Employee {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private String username;
	
	private String password;
	
	private int enabled;
	
	private String authorities;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAuthorities() {
		return authorities;
	}
	
	public List<AuthoritiesJson> getListAuthorities() throws Exception {
		return Utils.convertJsonStringToListObject(this.authorities, AuthoritiesJson[].class);
	}

	public void setAuthorities(String authorities) {
		this.authorities = authorities;
	}

	public int getEnabled() {
		return enabled;
	}

	public void setEnabled(int enabled) {
		this.enabled = enabled;
	}
	
	
}
