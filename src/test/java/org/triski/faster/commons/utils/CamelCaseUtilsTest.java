package org.triski.faster.commons.utils;

import org.apache.commons.lang3.CharUtils;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CamelCaseUtilsTest {

    @Test
    void toCapitalizeCamel() {
        String expectedStr = "HelloWorldAndHiChina";
        assertEquals(expectedStr, CamelCaseUtils.toCapitalizeCamel("hello_world_and-hi_china"));
        assertEquals(expectedStr, CamelCaseUtils.toCapitalizeCamel("hello-world-and-hi-china"));
        assertEquals(expectedStr, CamelCaseUtils.toCapitalizeCamel("hello world and hi china"));
    }

    @Test
    void toUnCapitalizeCamel() {
        String expectedStr = "helloWorldAndHiChina";
        assertEquals(expectedStr, CamelCaseUtils.toCapitalizeCamel("hello_world_and-hi_china"));
        assertEquals(expectedStr, CamelCaseUtils.toCapitalizeCamel("hello-world-and-hi-china"));
        assertEquals(expectedStr, CamelCaseUtils.toCapitalizeCamel("hello world and hi china"));
    }

    @Test
    void toUnderline() {
        assertEquals("hello_world_and_hi_china",
                CamelCaseUtils.toUnderline("helloWorldAndHiChina"));
    }

    @Test
    void toMiddleLine() {
        assertEquals("hello-world-and-hi-china",
                CamelCaseUtils.toMiddleLine("helloWorldAndHiChina"));
    }
}