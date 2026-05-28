package com.truong.oauth.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.truong.oauth.config.OAuth2Properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.http.HttpStatus;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/*
 * Mục đích chính của đoạn code này là biến ứng dụng của bạn thành một nơi cấp phát và quản lý các mã thông báo (Access Token/ID Token) dạng JWT (JSON Web Token) bằng thuật toán mã hóa bất đối xứng RSA (Public/Private Key).
 */

@Configuration
@EnableConfigurationProperties(OAuth2Properties.class)
public class AuthorizationServerConfiguration {

    private final OAuth2Properties properties;

    public AuthorizationServerConfiguration(OAuth2Properties properties) {
        this.properties = properties;
    }

    /*
     * Nếu request gọi vào các endpoint của Auth Server (như /oauth2/token), chuỗi số 1 sẽ xử lý.
     */
    
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http); // áp dụng các cấu hình mặc định của Spring Authorization Server. Nó sẽ tự động tạo ra các endpoints tiêu chuẩn như: /oauth2/authorize, /oauth2/token, /oauth2/revoke
        http.securityMatcher("/oauth2/**", "/login", "/css/**", "/js/**", "/images/**");
        http
                .csrf(AbstractHttpConfigurer::disable)
                /*
                 * Tại Auth Server (Cổng 9001): Sử dụng SessionCreationPolicy.IF_REQUIRED. Cần bộ nhớ Session để phục vụ khoảnh khắc con người tương tác với giao diện Web để lấy Token. Sau khi Mobile lấy được Token xong, Session này coi như hết nhiệm vụ.
                 * Tại các API Resource Server khác Sử dụng SessionCreationPolicy.STATELESS. Không tạo bất kỳ Session nào, chỉ tin vào chiếc Access Token (JWT) mà Mobile gửi lên.
                 */
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)) 
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                            new LoginUrlAuthenticationEntryPoint("/login"),
                            new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                    )
                .exceptionHandling(exceptions -> exceptions
                	    .defaultAuthenticationEntryPointFor(
                	        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                	        new MediaTypeRequestMatcher(MediaType.APPLICATION_JSON)
                	    )
                	)
                .formLogin(form -> form
                        .loginPage("/login") // Đường dẫn URL đến trang login đẹp của bạn
                        .permitAll()
                    )
                .getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults())
                ;

        return http.build();
    }

    /*
     * Nó lấy Khóa bí mật (Private Key) để ký vào phần chữ ký (Signature) của JWT. Signature = Encrypt(Hash(Header + Payload), PrivateKey)
     * 
     * Để các ứng dụng khác (Resource Server - ví dụ như API Gateway, Microservices backend) có thể xác thực được token do Authorization Server cấp ra, chúng cần có Khóa công khai (Public Key).
     * 
     * 
     * Khi Resource Server nhận Token và kiểm tra:

		Vế 1: Nó tự băm lại phần Header và Payload (đã bị sửa) đang có trong tay để ra một chuỗi mã MD5/SHA mới (gọi là Hash A).
		
		Vế 2: Nó dùng Khóa công khai để giải mã chuỗi Signature ở đuôi token nhằm lấy lại chuỗi băm gốc lúc chưa bị sửa (gọi là Hash B). Khi  dùng Public Key để "giải mã" Signature: Decrypt(Signature, PublicKey) -> Kết quả thu được KHÔNG PHẢI LÀ PAYLOAD, mà nó trả về chính là Hash(Header + Payload) gốc.
		
		So sánh: Nó so sánh Hash A == Hash B.
		
		Nếu Hacker đã sửa Payload, Hash A (tính trên dữ liệu sửa) chắc chắn sẽ khác hoàn toàn Hash B (giải mã từ chữ ký gốc).
     */
    
    @Bean
    public JWKSource<SecurityContext> jwkSource() throws Exception {
        RSAPublicKey publicKey = loadPublicKey(properties.authorizationServer().rsaPublicKey());
        RSAPrivateKey privateKey = loadPrivateKey(properties.authorizationServer().rsaPrivateKey());

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(properties.authorizationServer().keyId())
                .build();

        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(properties.authorizationServer().issuer())
                .build();
    }

    private RSAPublicKey loadPublicKey(String pem) throws Exception {
        byte[] decoded = Base64.getMimeDecoder().decode(stripHeaders(pem));
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
    }

    private RSAPrivateKey loadPrivateKey(String pem) throws Exception {
        byte[] decoded = Base64.getMimeDecoder().decode(stripHeaders(pem));
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    private String stripHeaders(String pem) {
        return pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
    }
}