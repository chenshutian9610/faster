package org.triski.faster.commons.utils;

import lombok.experimental.UtilityClass;
import java.util.HashMap;
import java.util.Map;

/**
 * @author triski
 * @date 2019/7/14
 */
@UtilityClass
public class MapBuilder {

    public <K, V> InnerMap<K, V> keyValue(Class<K> keyClass, Class<V> valueClass) {
        return new InnerMap<>();
    }

    public <K, V> InnerMap<K, V> put(K key, V value) {
        return new InnerMap<>(key, value);
    }

    public static class InnerMap<K, V> {
        private Map<K, V> map = new HashMap<>();

        public InnerMap() {
        }

        private InnerMap(K key, V value) {
            map.put(key, value);
        }

        public InnerMap<K, V> put(K key, V value) {
            map.put(key, value);
            return this;
        }

        public Map<K, V> build() {
            return map;
        }
    }
}
