package com.truong.media.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;


//MINIO_ACCESS_KEY	uZ70SbxhVDshIDUY3JD6
//MINIO_BUCKET	cto-networks
//MINIO_ENDPOINT	172.16.10.218
//MINIO_PORT	30900
//MINIO_PUBLIC_URL	https://beta.api.gateway.overate-vntech.com/s3/cto-networks
//MINIO_SECRET_KEY	OGHLInKB62XYboY50ayrROc9DYik2VUK0EvdbNxv

@Configuration
public class MinioConfig {
    @Value("${minio.url}")
    private String url;
    @Value("${minio.access-key}")
    private String accessKey;
    @Value("${minio.secret-key}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }
}