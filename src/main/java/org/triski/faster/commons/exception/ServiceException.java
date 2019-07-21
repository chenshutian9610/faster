package org.triski.faster.commons.exception;

import org.triski.faster.commons.utils.PlaceHolderUtils;

/**
 * @author chenshutian
 * @date 2019/7/16
 */
public class ServiceException extends RuntimeException {
    public ServiceException() {
    }

    public ServiceException(String message, Object... params) {
        super(PlaceHolderUtils.process(message, params));
    }
}
