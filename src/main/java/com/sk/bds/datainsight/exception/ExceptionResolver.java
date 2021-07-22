package com.sk.bds.datainsight.exception;

import com.sk.bds.datainsight.response.ResponseBase;
import com.sk.bds.datainsight.response.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ExceptionResolver  {

    private static final Logger log = LoggerFactory.getLogger(ExceptionResolver.class);

    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public @ResponseBody
    Object authExceptionHandling(HttpServletRequest request, AuthException e) {
        log.error("authExceptionHandling", e);
        return e.getRes();
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public @ResponseBody
    Object forbiddenExceptionHandling(HttpServletRequest request, ForbiddenException e) {
        log.error("forbiddenExceptionHandling", e);
        return e.getRes();
    }

    @ExceptionHandler(BadException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody
    Object BadExceptionHandling(HttpServletRequest request, BadException e) {
        log.error("BadExceptionHandling", e);
        return e.getRes();
    }

    @ExceptionHandler(InternalException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody
    Object internalExceptionHanding(HttpServletRequest request, InternalException e) {
        log.error("internalExceptionHanding", e);
        return e.getRes();
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody
    Object notFoundExceptionHandling(HttpServletRequest request, NoHandlerFoundException e) {
        log.error("notFoundExceptionHandling", e);
        return new ResponseBase(ResponseCode.NOT_FOUND).getRes();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody
    Object exceptionHanding(HttpServletRequest request, Exception e) {
        log.error("exceptionHanding", e);
        InternalException ie = new InternalException(e);
        ie.setMessage("요청 작업에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
        return ie.getRes();
    }
}
