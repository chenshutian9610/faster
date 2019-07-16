package org.triski.faster.commons.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsExtTest {

    @Test
    void newMessage() {
        assertEquals("hello world and china", StringUtilsExt.newMessage("hello {} and {}", "world", "china"));
    }
}