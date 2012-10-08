package stm;

public class LocalVersionClock {

    private static ThreadLocal<Long> local = new ThreadLocal<Long>() {
        @Override
        protected Long initialValue() {
            return 0L;
        }
    };

    public static long getCurrent() {
        return local.get();
    }

    public static void setCurrent() {
        local.set(GlobalVersionClock.getCurrent());
    }
}
