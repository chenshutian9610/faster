package org.triski.faster.commons.utils;

import com.alibaba.fastjson.JSON;
import lombok.experimental.UtilityClass;
import java.util.Map;

/**
 * @author triski
 * @date 2019/1/27
 */
@UtilityClass
public class MapUtils {

    public <T> T toObject(Map<?, ?> map, Class<T> clazz) {
        return JSON.parseObject(JSON.toJSONString(map), clazz);
    }

    public Map toMap(Object object) {
        return JSON.parseObject(JSON.toJSONString(object), Map.class);
    }

}
