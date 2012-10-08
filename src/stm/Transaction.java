package stm;

import java.util.concurrent.atomic.AtomicReference;

public class Transaction {

    private final AtomicReference<Status> status;

    public enum Status {
        COMMITTED, ACTIVE, ABORTED
    };

    public static final Transaction COMMITTED = new Transaction(Status.COMMITTED);

    private static ThreadLocal<Transaction> local = new ThreadLocal<Transaction>() {
        @Override
        protected Transaction initialValue() {
            return new Transaction(Status.COMMITTED);
        }
    };

    public Transaction() {
        status = new AtomicReference<Status>(Status.ACTIVE);
    }

    private Transaction(Status myStatus) {
        status = new AtomicReference<Status>(myStatus);
    }

    public Status getStatus() {
        return status.get();
    }

    public boolean commit() {
        return status.compareAndSet(Status.ACTIVE, Status.COMMITTED);
    }

    public boolean abort() {
        return status.compareAndSet(Status.ACTIVE, Status.ABORTED);
    }

    public static Transaction getLocal() {
        return local.get();
    }

    public static void startLocal() {
        local.set(new Transaction());
    }
}
