package com.truong.common.exception;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import com.truong.common.utils.BaseResponse;
import com.truong.common.utils.Utils;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<BaseResponse<Object>> handleCustomException(CustomException ex) {

		BaseResponse<Object> response = new BaseResponse<>();

		response.setStatus(ex.getHttpStatus());
		response.setMessageError(ex.getMessage());

		return new ResponseEntity<>(response, HttpStatus.OK);
	}



	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
	        HttpHeaders headers, HttpStatusCode status, WebRequest request) { // Đã đổi sang HttpStatusCode
	    
	    BaseResponse<Object> response = new BaseResponse<>();
	    List<String> errors = new ArrayList<>();

	    // Thu thập toàn bộ thông báo lỗi từ các trường dữ liệu (Field Errors)
	    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
	        errors.add(error.getDefaultMessage());
	    }
	    
	    // Thu thập toàn bộ thông báo lỗi tổng quan (Global Errors)
	    for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
	        errors.add(error.getDefaultMessage());
	    }

	    // Set status trong cục BaseResponse của bạn (vẫn dùng HttpStatus cũ hoặc ép kiểu nếu cần)
	    response.setStatus(HttpStatus.BAD_REQUEST); 
	    
	    // Gom các lỗi lại cách nhau bằng dấu phẩy
	    response.setMessageError(errors.stream().collect(Collectors.joining(",")));

	    // Trả về ResponseEntity với mã HTTP là 200 OK (theo đúng logic cũ của bạn)
	    return new ResponseEntity<>(response, HttpStatus.OK);
	}

	

	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
	        HttpHeaders headers, HttpStatusCode status, WebRequest request) { // Đã đổi thành HttpStatusCode
	    
	    BaseResponse<Object> response = new BaseResponse<>();
	    response.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
	    
	    // ex.getSupportedHttpMethods() ở bản mới trả về Set<HttpMethod> nên .toString() vẫn hoạt động rất tốt
	    response.setMessageError(String.format("Method not support :)), %s", 
	            ex.getSupportedHttpMethods() != null ? ex.getSupportedHttpMethods().toString() : "UNKNOWN"));

	    return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
	        HttpHeaders headers, HttpStatusCode status, WebRequest request) { // Đã đổi thành HttpStatusCode
	    
	    BaseResponse<Object> response = new BaseResponse<>();
	    String message = ex.getParameterName() + " parameter is missing";

	    response.setStatus(HttpStatus.BAD_REQUEST);
	    response.setMessageError(message);

	    // Dọn dẹp Diamond Operator thừa <Object> cho code chuẩn Java 17 sạch
	    return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ExceptionHandler({ Exception.class })
	public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
	    BaseResponse<Object> response = new BaseResponse<>();
	    
	    if (ex instanceof CustomException) {
	        CustomException customException = (CustomException) ex;
	        response.setStatus(customException.getHttpStatus());
	        response.setMessageError(customException.getErrorMessage());
	        response.setData(customException.getData());
	        
	    } else if (ex instanceof OAuth2AuthenticationException) { // Đã đổi từ InvalidTokenException sang chuẩn mới
	        response.setStatus(HttpStatus.UNAUTHORIZED);
	        response.setMessageError("Không có quyền truy cập hệ thống (Token không hợp lệ hoặc hết hạn)");
	        
	    } else if (ex instanceof AccessDeniedException) { // Bắt chính xác lớp từ gói jakarta/authorization mới
	        response.setStatus(HttpStatus.FORBIDDEN);
	        response.setMessageError("Tài khoản bạn không có quyền truy cập tính năng này");
	        
	    } else {
	        // Các lỗi hệ thống không xác định khác (NullPointerException, SQLException...)
	        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
	        response.setMessageError(ex.getLocalizedMessage());
	        
	        // Log lỗi ra màn hình Console chuẩn chỉnh
	        System.out.println(Utils.getDatetimeString(new Date()) + ": ERROR====>");
	        ex.printStackTrace();
	    }
	    
	    return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
}
