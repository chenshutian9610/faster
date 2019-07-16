package org.triski.faster.commons.utils;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionUtilsTest {

    @Test
    void listOf() {
        // list 的容量为 5 + 16 =21
        List<Integer> list = CollectionUtils.listOf(1, 2, 3, 4, 5);
        assertEquals(5, list.size());
        assertEquals("[1, 2, 3, 4, 5]", list.toString());
    }

    @Test
    void listOfExactly() {
        // list 的容量为 5
        List<Integer> list = CollectionUtils.listOfExactly(1, 2, 3, 4, 5);
        assertEquals(5, list.size());
        assertEquals("[1, 2, 3, 4, 5]", list.toString());
    }

    @Test
    void of() {
        // set 的容量为 3 + 16 = 19
        Set<Integer> set = CollectionUtils.of(HashSet::new, 1, 2, 3);
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        assertTrue(set.contains(3));
    }

    @Test
    void ofExactly() {
        // set 的容量由具体实现类决定，比 3 大一点
        Set<Integer> set = CollectionUtils.ofExactly(HashSet::new, 1, 2, 3);
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        assertTrue(set.contains(3));
    }
}