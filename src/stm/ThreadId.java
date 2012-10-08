package stm;

public class ThreadId {

    private static volatile int GLOBAL_ID = 0;

    private static ThreadLocal<Integer> threadId = new ThreadLocal<Integer>() {
        @Override
        protected synchronized Integer initialValue() {
            return GLOBAL_ID++;
        }
    };

    public static int get() {
        return threadId.get();
    }

    public static void set(int index) {
        threadId.set(index);
    }

    public static void reset() {
        GLOBAL_ID = 0;
    }
}
