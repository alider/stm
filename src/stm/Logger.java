package stm;

public class Logger {

    public static synchronized void log(String msg) {
        System.out.println(ThreadId.get() + " | " + msg);
    }
}
