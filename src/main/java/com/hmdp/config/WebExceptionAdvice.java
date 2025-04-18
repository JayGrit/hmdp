package com.hmdp.config;

import com.hmdp.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class WebExceptionAdvice {

    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        log.error(e.toString(), e);
        System.out.println("AAAAAAAAAAA");
        return Result.fail("服务器异常");
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("【全局异常】", e);
        System.out.println("BBBBBBBBBBB");
        return Result.fail("系统错误");
    }
}
