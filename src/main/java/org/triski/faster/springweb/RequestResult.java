package org.triski.faster.springweb;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>前端处理逻辑</p>
 * <pre>
 *  if (message) {
 *      if (success) {
 *          alertSuccess(message)
 *          render(data)
 *      } else {
 *          alertFail(message)
 *      }
 *  }
 * </pre>
 *
 * @author triski
 * @date 2019/1/14
 */
@Data
@Accessors(chain = true)
public class RequestResult<T> {
    public static final String PARAMETER_MISSING = "参数缺失";
    public static final String PASSWORD_ERROR = "密码错误";
    public static final String AUTH_CODE_ERROR = "验证码错误";
    public static final String OPERATION_ERROR = "操作失败";

    private boolean success;
    private String message;
    private T data;

    public RequestResult() {
        init();
    }

    public RequestResult(T data) {
        init();
        this.data = data;
    }

    private void init() {
        success = true;
        message = OPERATION_ERROR;
    }
}
