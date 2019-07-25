package org.triski.faster.commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FasterPropertiesTest {

    @Test
    void test() {
        FasterProperties.load("mybatis/faster.yml");
    }
}