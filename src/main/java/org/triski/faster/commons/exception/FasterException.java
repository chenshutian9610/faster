package org.triski.faster.commons.exception;

import org.triski.faster.commons.utils.PlaceHolderUtils;

/**
 * @author triski
 * @date 2019/5/30
 */
public class FasterException extends RuntimeException {
    public FasterException() {
    }

    public FasterException(Throwable cause) {
        super(cause);
    }

    public FasterException(String message, Object... params) {
        super(PlaceHolderUtils.process(message, params));
    }
}
