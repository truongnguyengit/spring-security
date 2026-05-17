package com.truong.oauth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

/**
 * Bean cấu hình bổ sung cho Authorization Server.
 *
 * Lớp này thay thế phần clients.jdbc(dataSource) trong cấu hình cũ.
 *
 * Nếu muốn load client từ database (tương đương clients.jdbc cũ),
 * hãy implement JdbcRegisteredClientRepository thay vì InMemoryRegisteredClientRepository:
 *
 *   @Bean
 *   public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
 *       return new JdbcRegisteredClientRepository(jdbcTemplate);
 *   }
 *
 * Lưu ý: Schema database mới khác hoàn toàn schema cũ của spring-security-oauth2.
 * Script schema: https://github.com/spring-projects/spring-authorization-server/blob/main/oauth2-authorization-server/src/main/resources/org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql
 */
@Configuration
public class SecurityBeanConfiguration {
	
	
	@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 1. Quản lý thông tin Client trong DB
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

//    // 2. BỔ SUNG: Quản lý trạng thái Token/Authorization trong DB (Tránh mất phiên đăng nhập khi restart server)
//    @Bean
//    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
//        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
//    }
//
//    // 3. BỔ SUNG: Quản lý sự chấp thuận quyền của User trong DB
//    @Bean
//    public OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
//        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
//    }

//    /**
//     * Đăng ký OAuth2 client (thay thế clients.jdbc trong cấu hình cũ).
//     *
//     * Ví dụ dưới dùng InMemory. Trong production nên dùng JdbcRegisteredClientRepository.
//     */
//    @Bean
//    public RegisteredClientRepository registeredClientRepository() {
//        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
//                .clientId("test_oauth2")                                        // client_id
//                .clientSecret(new BCryptPasswordEncoder().encode("secret"))     // client_secret (BCrypt)
//                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
//                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
//                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
//                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
//                .redirectUri("http://localhost:8080/login/oauth2/code/client")  // Thay đổi theo ứng dụng
//                .scope("read")
//                .scope("write")
//                .tokenSettings(TokenSettings.builder()
//                        .accessTokenTimeToLive(Duration.ofHours(1))
//                        .refreshTokenTimeToLive(Duration.ofDays(30))
//                        .reuseRefreshTokens(true)                               // Tương đương setReuseRefreshToken(true) cũ
//                        .build())
//                .clientSettings(ClientSettings.builder()
//                        .requireAuthorizationConsent(false)
//                        .build())
//                .build();
//
//        return new InMemoryRegisteredClientRepository(registeredClient);
//    }
}
