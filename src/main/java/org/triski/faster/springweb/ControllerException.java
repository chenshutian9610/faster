package org.triski.faster.springweb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author triski
 * @date 2018/12/18
 */
@ControllerAdvice
public class ControllerException {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public RequestResult deal(Exception e) {
        e.printStackTrace();
        return new RequestResult().setSuccess(false).setMessage(e.getMessage());
    }

}