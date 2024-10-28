package com.example.dailyLog.exception.commonException;

import com.example.dailyLog.exception.commonException.error.BizException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 공통 에러 응답 생성 메소드
    private ResponseEntity<ErrorResponse> createErrorResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .message(message)
                        .httpStatus(status)
                        .localDateTime(LocalDateTime.now())
                        .build());
    }

    // CommonErrorCode를 사용하는 에러 응답 생성 메소드
    private ResponseEntity<ErrorResponse> createErrorResponse(CommonErrorCode commonErrorCode) {
        return ResponseEntity.status(commonErrorCode.getHttpStatus())
                .body(ErrorResponse.builder()
                        .message(commonErrorCode.getMessage())
                        .httpStatus(commonErrorCode.getHttpStatus())
                        .localDateTime(LocalDateTime.now())
                        .build());
    }


    // 시스템 예외
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e){
        return createErrorResponse(CommonErrorCode.INVALID_REQUEST_BODY);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleSqlIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        return createErrorResponse(CommonErrorCode.DATABASE_INTEGRITY_ERROR);
    }


    // 비즈니스 로직 예외
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ErrorResponse> handleBizException(BizException e){
        return createErrorResponse(e.getCommonErrorCode());
    }
}
