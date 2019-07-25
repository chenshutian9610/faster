package org.triski.faster.commons.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class InstantUtilsTest {

    @Test
    void parse() {
        Instant expected = LocalDateTime.of(1996, 11, 24, 8, 0, 0).atZone(ZoneId.systemDefault()).toInstant();
        Instant instant = InstantUtils.parse("1996-11-24 08:00:00");
        assertEquals(expected, instant);
    }

    @Test
    void toString1() {
        Instant instant = InstantUtils.parse("1996-11-24 08:00:20");
        assertEquals("1996-11-24 08:00:20", InstantUtils.toString(instant));
    }

    @Test
    void toString2() {
        Instant instant = InstantUtils.parse("1996-11-24 08:00:20");
        assertEquals("1996/11/24/08/00/20", InstantUtils.toString(instant, "yyyy/MM/dd/HH/mm/ss"));
    }
}