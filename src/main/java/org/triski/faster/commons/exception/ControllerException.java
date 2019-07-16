package org.triski.faster.commons.exception;

import org.triski.faster.commons.utils.StringUtilsExt;

/**
 * @author chenshutian
 * @date 2019/7/16
 */
public class ControllerException extends RuntimeException {
    public ControllerException() {
    }

    public ControllerException(String message, Object... params) {
        super(StringUtilsExt.newMessage(message, params));
    }
}
