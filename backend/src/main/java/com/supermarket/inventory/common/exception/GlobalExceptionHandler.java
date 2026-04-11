package com.supermarket.inventory.common.exception;

import com.supermarket.inventory.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        return ApiResponse.failure(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        BindException.class,
        ConstraintViolationException.class,
        HttpMessageNotReadableException.class
    })
    public ApiResponse<Void> handleBadRequest(Exception ex) {
        return ApiResponse.failure(400, "请求参数错误");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception ex) {
        return ApiResponse.failure(500, "系统内部异常");
    }
}
