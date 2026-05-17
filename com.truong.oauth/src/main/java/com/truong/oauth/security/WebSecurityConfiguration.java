package com.truong.oauth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


/*
 * Nếu như các file trước tập trung vào cấu hình các cổng kết nối API và quản lý ứng dụng Client, thì file này thiết lập bộ não xác thực danh tính. 
 * Nó định nghĩa cách ứng dụng kiểm tra xem tài khoản (Username) và mật khẩu (Password) do người dùng nhập vào có chính xác hay không để từ đó quyết định cấp Token.
 */

@Configuration
@EnableMethodSecurity // kích hoạt tính năng phân quyền ở mức method (Method-level Security) trong toàn bộ ứng dụng Spring Boot của bạn.@PreAuthorize hoặc @PostAuthorize
public class WebSecurityConfiguration {

	
	/*
	 * Đây là nơi bạn tự định nghĩa (customize) quy trình xác thực người dùng. 
	 * Thay vì để Spring Security dùng tài khoản mặc định sinh ra trên log, bạn cấu hình cho nó kết nối trực tiếp tới Database của hệ thống thông qua hai tham số truyền vào:
	 */
    @Bean
    public AuthenticationManager authenticationManager(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    
    /*
     * Tổng kết bức tranh toàn cảnh:
		Đến bước này, hệ thống OAuth2 Authorization Server của bạn đã hoàn thiện 3 mảnh ghép cốt lõi:
		
		Cổng bảo vệ API (AuthorizationServerConfiguration & ResourceServerConfiguration): Biết cách chặn các request, đọc và giải mã token bằng RSA Key, trả về lỗi dạng JSON.
		
		Kho lưu trữ Ứng dụng Client (SecurityBeanConfiguration + MySQL): Biết check ứng dụng bên thứ ba nào (test_oauth2) được phép kết nối và thời gian sống của Token là bao lâu.
		
		Bộ xác thực Con người (WebSecurityConfiguration): Biết cách kiểm tra tài khoản, mật khẩu của người dùng cuối thông qua Database và kích hoạt tính năng phân quyền chi tiết cho từng hàm xử lý.
     */
    
    
	/*
	 * 1. Tại sao phiên bản mới KHÔNG cần lưu Access Token vào Redis nữa?
		Ở phiên bản cũ, mặc định hệ thống sinh ra Opaque Token (một chuỗi chuỗi ký tự ngẫu nhiên dạng UUID). Token này không chứa dữ liệu gì bên trong, nên mỗi khi Client gửi Token lên, Resource Server bắt buộc phải gọi ngược lại Auth Server (hoặc check vào Database/Redis) để xem "Token này là của ai, có quyền gì". Do đó, lưu trên Redis giúp quá trình kiểm tra này diễn ra cực kỳ nhanh.
		
		Trong kiến trúc mới bạn đang cấu hình:
		
		Token là JWT: Đoạn code ở lớp ResourceServerConfiguration của bạn đang cấu hình sử dụng .jwt(jwt -> {}).
		
		Tự xác thực (Self-contained): Bản thân chuỗi JWT đã chứa sẵn mọi thông tin bên trong (Username, Quyền hạn, Thời gian hết hạn) và được ký bảo mật bằng Private Key của Server.
		
		Không cần gọi vào DB/Redis: Khi Client gửi JWT lên Resource Server, Spring chỉ cần dùng Public Key (đã cấu hình ở lớp đầu tiên) để giải mã toán học và kiểm tra chữ ký trực tiếp trên RAM. Quá trình này hoàn toàn không gọi bất kỳ một câu lệnh nào vào Database hay Redis cả.
		
		Vì vậy, việc xác thực token hiện tại đã vô cùng "nhẹ nhàng" đúng như kỳ vọng của bạn mà không cần tốn thêm chi phí duy trì bộ nhớ Redis cho Access Token.
		
		2. Vậy cái gì mới cần lưu vào Redis/Database?
		Mặc dù Access Token không cần lưu trữ, nhưng có 2 thứ bạn vẫn phải quản lý trạng thái (State):
		
		Authorization Code: Mã code tạm thời (chỉ sống trong vài phút) ở luồng đăng nhập giao diện.
		
		Refresh Token: Mã dùng để đổi Access Token mới khi token cũ hết hạn (thường sống vài tuần hoặc vài tháng). Đặc biệt, bạn cần lưu trữ nó để có thể hủy (Revoke) quyền truy cập của người dùng khi cần thiết.
		
		Trong Spring Authorization Server mới, thành phần quản lý việc này không còn gọi là TokenStore nữa, mà nó được thay thế bằng một Interface tên là: OAuth2AuthorizationService.
	 */
}