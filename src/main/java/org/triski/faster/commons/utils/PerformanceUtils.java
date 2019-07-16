package org.triski.faster.commons.utils;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author triski
 * @date 2019/3/18
 */
public class PerformanceUtils {

    public interface Doer {
        void run();
    }

    public static void test(Doer doer) {
        doer.run(); // 第一次执行会初始化很多东西，时间偏长，不能算进去
        test(doer, null, null, null);
    }

    public static <T> void test(Supplier<T> supplier) {
        System.out.printf("result : %s%n", supplier.get());
        test(null, supplier, null, null);
    }

    public static <T, R> void test(Function<T, R> function, T value) {
        System.out.printf("result : %s%n", function.apply(value));
        test(null, null, function, value);
    }

    private static <T, R> void test(Doer doer, Supplier<T> supplier, Function<T, R> function, T value) {
        int times = 3;
        long sum = 0;
        long start, duration;
        for (int i = 1; i <= times; i++) {
            start = System.nanoTime();

            if (doer != null) doer.run();
            else if (supplier != null) supplier.get();
            else if (function != null && value != null) function.apply(value);
            else break;

            duration = (System.nanoTime() - start) / 1_000_000;
            sum += duration;
            System.out.printf("%s : %s milliseconds%n", i, duration);
        }
        System.out.printf("mean consumption : %s milliseconds%n", sum / times);
    }
}

