package com.example.backend.common.exception;

import com.example.backend.common.response.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException ex) {
        return Result.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class, HttpMessageNotReadableException.class})
    public Result<Void> handleBadRequest(Exception ex) {
        return Result.fail(400, "请求参数不正确");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        return Result.fail(500, "服务器内部错误: " + ex.getMessage());
    }
}
