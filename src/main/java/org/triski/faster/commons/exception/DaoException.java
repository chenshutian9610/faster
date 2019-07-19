package org.triski.faster.commons.exception;

import org.triski.faster.commons.utils.PlaceHolderParser;

/**
 * @author chenshutian
 * @date 2019/7/16
 */
public class DaoException extends RuntimeException {
    public DaoException() {
    }

    public DaoException(String message, Object... params) {
        super(PlaceHolderParser.process(message, params));
    }
}
