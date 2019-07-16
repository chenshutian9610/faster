package org.triski.faster.commons.exception;

import org.triski.faster.commons.utils.StringUtilsExt;

/**
 * @author triski
 * @date 2019/5/30
 */
public class FasterException extends RuntimeException {
    public FasterException() {
    }

    public FasterException(String message, Object... params) {
        super(StringUtilsExt.newMessage(message, params));
    }
}
