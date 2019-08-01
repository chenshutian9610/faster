package org.triski.faster.commons.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultValueUtilsTest {

    @Test
    void getDefaultValueIfNull() {
        assertEquals(1, DefaultValueUtils.getDefaultValueIfNull(null, 1));
        assertEquals("1", DefaultValueUtils.getDefaultValueIfNull(null, "1"));
        assertEquals("1", DefaultValueUtils.getDefaultValueIfNull("1", "..."));
    }

    @Test
    void getDefaultValueIfBlank() {
        assertEquals("1", DefaultValueUtils.getDefaultValueIfBlank("  ","1"));
        assertEquals("1",DefaultValueUtils.getDefaultValueIfBlank("1","..."));
    }
}