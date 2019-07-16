package org.triski.faster.commons.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CamelCaseUtilsTest {

    @Test
    void toCapitalizeCamel() {
        assertEquals("HelloWorldAndHiChina",
                CamelCaseUtils.toCapitalizeCamel("hello_world_and_hi_china"));
    }

    @Test
    void toUnCapitalizeCamel() {
        assertEquals("helloWorldAndHiChina",
                CamelCaseUtils.toUnCapitalizeCamel("hello_world_and_hi_china"));
    }

    @Test
    public void toUnderline() {
        assertEquals("hello_world_and_hi_china",
                CamelCaseUtils.toUnderline("helloWorldAndHiChina"));
    }
}