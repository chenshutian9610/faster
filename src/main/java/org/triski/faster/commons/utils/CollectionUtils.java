package org.triski.faster.commons.utils;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.triski.faster.commons.annotation.MainMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author triski
 * @date 2019/1/27
 * @export of, ofExactly, listOf, listOfExactly
 * @notice of 生成的集合 initialCapacity = size + 16
 * @notice ofExactly 生成的集合为 initialCapacity = size
 */
@UtilityClass
public class CollectionUtils {

    private final Logger logger = LoggerFactory.getLogger(CollectionUtils.class);

    /**
     * @param function        通过 initialCapacity 来初始化集合
     * @param initialCapacity 集合的初始容量
     * @param values          集合的值
     */
    @MainMethod
    private <E, T extends Collection<E>> T collect(Function<Integer, T> function, int initialCapacity, E[] values) {
        T collection = function.apply(initialCapacity);
        Collections.addAll(collection, values);

        if (logger.isDebugEnabled()) {
            logger.debug("generate {} {initialCapacity: {}, size: {}, elements: {}}",
                    collection.getClass().getSimpleName(), initialCapacity, collection.size(), collection);
        }

        return collection;
    }

    public <E> List<E> listOf(E... values) {
        return collect(ArrayList::new, values.length + 16, values);
    }

    public <E> List<E> listOfExactly(E... values) {
        return collect(ArrayList::new, values.length, values);
    }

    public <E, T extends Collection<E>> T of(Function<Integer, T> function, E... values) {
        return collect(function, values.length + 16, values);
    }

    public <E, T extends Collection<E>> T ofExactly(Function<Integer, T> function, E... values) {
        return collect(function, values.length, values);
    }
}
