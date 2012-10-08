package stm;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TransactionExecutor {

    private volatile BeforeOperation beforeOperation;
    private volatile AfterOperation afterOperation;
    private volatile OnCommit onCommit;
    private volatile OnAbort onAbort;

    private Validator transactionValidator;

    private Statistics statistics = new Statistics();

    public TransactionExecutor() {
    }

    public TransactionExecutor(Validator validator, BeforeOperation beforeOperation, AfterOperation afterOperation,
            OnCommit onCommit, OnAbort onAbort) {
        this.transactionValidator = validator;
        this.beforeOperation = beforeOperation;
        this.afterOperation = afterOperation;
        this.onCommit = onCommit;
        this.onAbort = onAbort;
    }

    public <T> T execute(Callable<T> action) throws Exception {
        while (!Thread.currentThread().isInterrupted()) {
            Transaction.startLocal();
            try {
                beforeOperation();
                T result = action.call();
                afterOperation();
                if (isTransactionValid()) {
                    if (Transaction.getLocal().commit()) {
                        onCommit();
                        return result;
                    }
                }
            } catch (AbortedException e) {
                Transaction.getLocal().abort();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                throw new TransactionFailedException(e);
            }
            onAbort();
        }
        throw new TransactionFailedException("Got interrupted!");
    }

    public abstract <E extends Copyable<E>> TransactionalObject<E> makeTransactional(E object);

    private boolean isTransactionValid() {
        if (transactionValidator != null) {
            return transactionValidator.validate();
        } else {
            return true;
        }
    }

    private void beforeOperation() {
        if (beforeOperation != null) {
            beforeOperation.execute();
        }
    }

    private void afterOperation() {
        if (afterOperation != null) {
            afterOperation.execute();
        }
    }

    private void onCommit() {
        statistics.incrementCommits();
        if (onCommit != null) {
            onCommit.execute();
        }
    }

    private void onAbort() {
        statistics.incrementAborts();
        if (onAbort != null) {
            onAbort.execute();
        }
    }

    interface Validator {
        boolean validate();
    }

    interface BeforeOperation {
        public void execute();
    }

    interface AfterOperation {
        public void execute();
    }

    interface OnCommit {
        public void execute();
    }

    interface OnAbort {
        public void execute();
    }

    public String dumpStatistics() {
        return statistics.toString();
    }

    static class Statistics {
        private final AtomicInteger commits = new AtomicInteger(0);
        private final AtomicInteger aborts = new AtomicInteger(0);

        public void incrementCommits() {
            this.commits.incrementAndGet();
        }

        public void incrementAborts() {
            this.aborts.incrementAndGet();
        }

        public String toString() {
            return "Executor statistics: commits=" + commits.get() + " aborts=" + aborts.get();
        }
    }
}
