package org.triski.faster.springweb.utils;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * @author triski
 * @date 2019/1/14
 * <p>
 * 对 spring mvc 的 @Valid 和 BindingResult 进行验证
 */
public class VerifyUtils {
    public static String getErrorString(BindingResult result) {
        Map map = result.getAllErrors().stream().collect(
                toMap(ObjectError::getDefaultMessage, error -> {
                    String code = error.getCodes()[0];
                    return code.substring(code.lastIndexOf('.') + 1);
                }, (x, y) -> x + ", " + y));
        StringBuilder sb = new StringBuilder();
        map.forEach((k, v) -> {
            sb.append(v).append(' ').append(k);
        });
        return sb.toString();
    }
}
