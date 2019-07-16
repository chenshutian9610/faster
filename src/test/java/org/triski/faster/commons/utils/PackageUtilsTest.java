package org.triski.faster.commons.utils;

import org.junit.jupiter.api.Test;

class PackageUtilsTest {

    @Test
    void scan() {
        PackageUtils.scan(PackageUtilsTest.class.getPackage().getName());
    }
}