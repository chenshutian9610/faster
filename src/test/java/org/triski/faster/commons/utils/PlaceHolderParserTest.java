package org.triski.faster.commons.utils;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaceHolderParserTest {
    private static final String expectedStr = "hello world and china";

    @Test
    void newMessage() {
        assertEquals(expectedStr, PlaceHolderParser.process("hello {} and {}", "world", "china"));
    }

    @Test
    void process() {
        Properties properties = new Properties();
        properties.setProperty("where", "world");
        properties.setProperty("where2", "china");
        properties.setProperty("greet","hi");
        assertEquals(expectedStr, PlaceHolderParser.process("${greet:hello} ${where} and ${where2}", properties));
    }
}