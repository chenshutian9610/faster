package org.triski.faster.commons;

import org.junit.jupiter.api.Test;

class FasterPropertiesTest {

    @Test
    void test() {
        FasterProperties.load("cfg/faster.yml");
    }
}