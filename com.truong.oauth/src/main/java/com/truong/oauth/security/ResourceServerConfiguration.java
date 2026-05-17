package com.truong.oauth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/*
 * Đây là lớp cấu hình Resource Server (Máy chủ tài nguyên). 
 * Nếu như lớp AuthorizationServerConfiguration ở bước trước đóng vai trò là "Nơi cấp chứng minh thư" (Token),
 *  thì lớp ResourceServerConfiguration này chính là "Chú bảo vệ đứng gác cửa" để kiểm tra chứng minh thư đó trước khi cho phép client truy cập vào các API nội bộ.
 */

@Configuration
public class ResourceServerConfiguration {

	
	/*
	 * Nếu request gọi vào các API thông thường (như /api/products, /api/user), chuỗi số 1 sẽ bỏ qua và request sẽ rơi xuống chuỗi số 2 (@Order(2)) này để kiểm tra Token.
	 */
	
    @Bean
    @Order(2)
    public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
        		.securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //Không tạo session — mỗi request phải tự mang JWT để chứng thực, server hoàn toàn stateless
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/login").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {}) // Cấu hình này bảo Spring hãy tự động tìm kiếm JWKSource (chính là Bean chứa Public Key mà bạn đã cấu hình ở lớp trước) để giải mã và kiểm tra chữ ký của các Token gửi lên. Bạn để trống {} nghĩa là áp dụng cấu hình mặc định của Spring, nó tự động map rất mượt mà.
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setStatus(401);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"Token không hợp lệ hoặc đã hết hạn\"}");
                        })
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(403);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"error\":\"forbidden\",\"message\":\"Không có quyền truy cập\"}");
                        })
                );

        return http.build();
    }
}