package org.triski.faster.springweb.utils;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.triski.faster.commons.utils.CollectionUtils;

import java.util.Map;

/**
 * @author triski
 * @date 2019/4/20
 */
public class HttpUtils {
    private static volatile RestTemplate restTemplate;

    private static RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            synchronized (RestTemplate.class) {
                if (restTemplate == null)
                    return new RestTemplate();
            }
        }
        return restTemplate;
    }

    public static void doPost(String url, Map<String, String> form) {
        doPost(url, form, Void.class);
    }

    public static <T> T doPost(String url, Map<String, String> form, Class<T> clazz) {
        if (form != null && form.size() != 0) {
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            form.forEach((k, v) -> map.put(k, CollectionUtils.listOf(v)));
            return getRestTemplate().postForObject(url, map, clazz);
        } else {
            return getRestTemplate().postForObject(url, null, clazz);
        }
    }

    public static void doGet(String url, Map<String, String> form) {
        doGet(url, form, Void.class);
    }

    public static <T> T doGet(String url, Map<String, String> form, Class<T> clazz) {
        if (form != null && form.size() != 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("?");
            form.forEach((k, v) -> sb.append(String.format("%s=%s", k, v)).append("&"));
            sb.delete(sb.length() - 1, sb.length());
            url += sb.toString();
        }
        return getRestTemplate().getForObject(url, clazz);
    }
}
