package stm;

import java.util.Random;

public class ContentionResolver {

    private static final int MIN_DELAY = 32;
    private static final int MAX_DELAY = 1024;

    private Random random = new Random();
    private Transaction rival = null;
    private int delay = MIN_DELAY;

    private static ThreadLocal<ContentionResolver> local = new ThreadLocal<ContentionResolver>() {
        @Override
        protected ContentionResolver initialValue() {
            return new ContentionResolver();
        }
    };

    public static ContentionResolver getLocal() {
        return local.get();
    }

    public void resolve(Transaction me, Transaction other) {
        if (other != rival) {
            rival = other;
            delay = MIN_DELAY;
        }
        if (delay < MAX_DELAY) {
            sleep(random.nextInt(delay));
            delay = 2 * delay;
        } else {
            other.abort();
            delay = MIN_DELAY;
        }
    }

    private void sleep(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}