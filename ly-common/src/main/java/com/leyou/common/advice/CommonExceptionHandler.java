package com.leyou.common.advice;


import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.ExceptionResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class CommonExceptionHandler {


    //@ExceptionHandler(RuntimeException.class)
    @ExceptionHandler(LyException.class)
    public ResponseEntity<ExceptionResult> handleException(LyException e)
    {
        //通过异常实例得到其中的异常枚举，再从这个枚举中获得相应信息
        return ResponseEntity.status(e.getExceptionEnum().getCode()).
                body(new ExceptionResult(e.getExceptionEnum()));
    }

}
