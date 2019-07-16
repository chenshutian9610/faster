package org.triski.faster.commons.utils;

import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapBuilderTest {

    @Test
    void put() {
        Map<String, String> map = MapBuilder
                .keyValue(String.class, String.class)
                .put("username", "root")
                .put("password", "root")
                .build();
        assertEquals("root", map.get("username"));
        assertEquals("root", map.get("password"));
    }
}