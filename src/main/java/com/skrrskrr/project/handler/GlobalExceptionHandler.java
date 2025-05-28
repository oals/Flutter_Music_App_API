package com.skrrskrr.project.handler;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.FileNotFoundException;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception e) {
        HttpStatus status;

        // 잘못된 입력값 (400)
        if (e instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
        }
        // null 값 참조 (400 )
        else if (e instanceof NullPointerException) {
            status = HttpStatus.BAD_REQUEST;
        }
        // 유효성 검사 실패 (400 )
        else if (e instanceof MethodArgumentNotValidException) {
            status = HttpStatus.BAD_REQUEST;
        }
        // 데이터 바인딩 실패 (400)
        else if (e instanceof BindException) {
            status = HttpStatus.BAD_REQUEST;
        }
        // 접근 권한 없음 (403 )
        else if (e instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
        }
        // 데이터베이스 제약 조건 위반 (409 )
        else if (e instanceof DataIntegrityViolationException) {
            status = HttpStatus.CONFLICT;
        }
        // 지원되지 않는 HTTP 메서드 요청 (405 )
        else if (e instanceof HttpRequestMethodNotSupportedException) {
            status = HttpStatus.METHOD_NOT_ALLOWED;
        }
        // 파일 누락 (404 )
        else if (e instanceof FileNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        }
        // 데이터 엔티티 없음 (404 )
        else if (e instanceof EntityNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        }
        // 논리적 충돌 발생 (409)
        else if (e instanceof IllegalStateException) {
            status = HttpStatus.CONFLICT;
        }
        // 기타 예상치 못한 오류 (500 )
        else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ResponseEntity.status(status).body(Map.of("message", e.getMessage()));
    }
}