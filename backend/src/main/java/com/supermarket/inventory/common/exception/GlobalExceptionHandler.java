package com.supermarket.inventory.common.exception;

import com.supermarket.inventory.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        return ApiResponse.failure(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        return ApiResponse.failure(400, extractBindingMessage(ex.getBindingResult()));
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException ex) {
        return ApiResponse.failure(400, extractBindingMessage(ex.getBindingResult()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
            .findFirst()
            .map(violation -> violation.getMessage())
            .orElse("请求参数错误");
        return ApiResponse.failure(400, message);
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ApiResponse<Void> handleRequestFormatException(Exception ex) {
        return ApiResponse.failure(400, "请求参数格式错误");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception ex) {
        return ApiResponse.failure(500, "系统内部异常");
    }

    private String extractBindingMessage(BindingResult bindingResult) {
        Optional<FieldError> fieldError = Optional.ofNullable(bindingResult.getFieldError());
        if (fieldError.isPresent()) {
            return Optional.ofNullable(fieldError.get().getDefaultMessage()).orElse("请求参数错误");
        }

        Optional<ObjectError> objectError = Optional.ofNullable(bindingResult.getGlobalError());
        return objectError
            .map(ObjectError::getDefaultMessage)
            .orElse("请求参数错误");
    }
}
