package com.truong.oauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
	
	/*
	 * Luồng authorization_code là luồng bảo mật nhất trong OAuth 2.0/2.1. 
	 * Nó hoạt động theo nguyên tắc bảo mật 2 lớp: Thay vì cấp thẳng Token cho Postman ngay khi đăng nhập xong, Server sẽ cấp một mã code tạm thời trước, 
	 * rồi mới dùng code đó để đổi lấy Token chính thức.
	 */

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Sẽ tìm file login.html trong thư mục templates (Thymeleaf)
    }
}