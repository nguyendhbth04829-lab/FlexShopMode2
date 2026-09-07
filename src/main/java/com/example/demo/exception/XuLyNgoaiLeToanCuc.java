package com.example.demo.exception;

import com.example.demo.dto.response.PhanHoiApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class XuLyNgoaiLeToanCuc {

    @ExceptionHandler(NgoaiLeUngDung.class)
    public ResponseEntity<PhanHoiApi<Object>> handleNgoaiLeUngDung(NgoaiLeUngDung ex) {
        return ResponseEntity.status(ex.getMaTrangThai())
                .body(PhanHoiApi.thatBai(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PhanHoiApi<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        PhanHoiApi<Map<String, String>> response = PhanHoiApi.<Map<String, String>>builder()
                .thanhCong(false)
                .thongBao("Dữ liệu đầu vào không hợp lệ")
                .duLieu(errors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<PhanHoiApi<Object>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(PhanHoiApi.thatBai("Tài khoản hoặc mật khẩu không chính xác"));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<PhanHoiApi<Object>> handleUsernameNotFound(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(PhanHoiApi.thatBai(ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<PhanHoiApi<Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(PhanHoiApi.thatBai("Bạn không có quyền truy cập chức năng này"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<PhanHoiApi<Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PhanHoiApi.thatBai("Đã xảy ra lỗi hệ thống: " + ex.getMessage()));
    }
}
