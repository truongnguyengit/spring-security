package com.truong.media.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;

@Service
public class MinioService {
    private final MinioClient minioClient;
    
    @Value("${minio.bucket-name}")
    private String bucketName;
    
    @Value("${minio.public-url}")
    private String minioPublicUrl;

    public MinioService(MinioClient minioClient) { this.minioClient = minioClient; }

    public FileResponse uploadFile(MultipartFile file) throws Exception {
        // 1. Tạo tên file duy nhất
        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + extension;

        // 2. Upload file
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());

       String url = this.getPublicUrl(fileName);

        return new FileResponse(fileName, url, file.getSize());
    }
    
    public String getPublicUrl(String fileName) {
        return String.format("%s/%s", minioPublicUrl, fileName);
    }
}