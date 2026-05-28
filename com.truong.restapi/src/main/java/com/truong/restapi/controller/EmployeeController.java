package com.truong.restapi.controller;

import java.sql.SQLException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.truong.common.exception.CustomException;
import com.truong.common.utils.BaseResponse;
import com.truong.media.service.FileResponse;
import com.truong.media.service.MinioService;
import com.truong.restapi.response.EmployeeResponse;
import com.truong.service.PostService;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController extends BaseController{
	
	private final MinioService minioService;
	
	public EmployeeController(MinioService minioService) {
		this.minioService = minioService;
	}


	@GetMapping("")
	public ResponseEntity<BaseResponse<Object>> getAll() throws CustomException, SQLException {
		
		BaseResponse<Object> response = new BaseResponse<>();
		

		
		response.setData(new EmployeeResponse().mapToList(employeeService.findAll()));
		
		return new ResponseEntity<>(response, HttpStatus.OK);
		
		
	}
	
	@PostMapping("/upload")
    public ResponseEntity<FileResponse> upload(@RequestParam("file") MultipartFile file) {
        // Validation cơ bản
        if (file.isEmpty()) throw new RuntimeException("File không được trống");
        if (file.getSize() > 5 * 1024 * 1024) throw new RuntimeException("File quá lớn (max 5MB)");

        try {
            return ResponseEntity.ok(minioService.uploadFile(file));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
