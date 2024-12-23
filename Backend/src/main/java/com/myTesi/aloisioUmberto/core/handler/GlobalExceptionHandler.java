package com.myTesi.aloisioUmberto.core.handler;

import com.myTesi.aloisioUmberto.dto.Error.ErrorService;
import jdk.jshell.spi.ExecutionControl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;

import javax.persistence.EntityNotFoundException;
import java.util.Date;


@RestControllerAdvice
@Slf4j
@AllArgsConstructor
public class GlobalExceptionHandler {

    //400
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorService onBadRequest(WebRequest req, BadRequestException ex){
        return errorResponse(req, ex.getMessage());
    }

    //404
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorService onResourceNotFoundException(WebRequest req, EntityNotFoundException ex){
        return errorResponse(req, ex.getMessage());
    }

    //400
    @ExceptionHandler(ExecutionControl.UserException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorService onResourceNotFoundException(WebRequest req, ExecutionControl.UserException ex){
        return errorResponse(req, ex.getMessage() );
    }


    private ErrorService errorResponse (WebRequest req, String message) {
        HttpServletRequest httpreq = (HttpServletRequest) req.resolveReference("request");
        assert httpreq != null;
        final ErrorService output = new ErrorService(new Date(), httpreq.getRequestURI(), message);
        log.error("Exception handler :::: {}", output.toString());
        return output;

    }
}
