package stm;

import java.util.concurrent.atomic.AtomicLong;

public class GlobalVersionClock {

    private static AtomicLong global = new AtomicLong();

    public static long getCurrent() {
        return global.get();
    }

    public static long increment() {
        return global.incrementAndGet();
    }
}
