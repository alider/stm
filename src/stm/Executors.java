package stm;

public class Executors {

    public static TransactionExecutor newOFreeExecutor() {
        TransactionExecutor te = new TransactionExecutor() {
            @Override
            public <E extends Copyable<E>> TransactionalObject<E> makeTransactional(E object) {
                return new OFreeTransactionalObject<E>(object);
            }
        };
        return te;
    }

    public static TransactionExecutor newLockingExecutor() {
        TransactionExecutor.Validator validator = new TransactionExecutor.Validator() {
            @Override
            public boolean validate() {
                if (!WriteSet.getLocal().tryLock()) {
                    return false;
                }
                for (LockingTransactionalObject<?> object : ReadSet.getLocal().openedObjects()) {
                    if (object.isLockedByOtherThread() || !object.isValid()) {
                        return false;
                    }
                }
                return true;
            }
        };

        TransactionExecutor.BeforeOperation beforeOperation = new TransactionExecutor.BeforeOperation() {
            @Override
            public void execute() {
                LocalVersionClock.setCurrent();
            }
        };

        TransactionExecutor.AfterOperation afterOperation = new TransactionExecutor.AfterOperation() {
            @Override
            public void execute() {
            }
        };

        TransactionExecutor.OnCommit onCommit = new TransactionExecutor.OnCommit() {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public void execute() {
                long newVersion = GlobalVersionClock.increment();
                LocalVersionClock.setCurrent();
                WriteSet<?> writeSet = WriteSet.getLocal();
                for (LockingTransactionalObject openedObject : writeSet.openedObjects()) {
                    Object current = openedObject.openForRead();
                    Copyable localCopy = writeSet.getLocalCopyOf(openedObject);
                    localCopy.copyTo(current);
                    openedObject.setVersion(newVersion);
                }
                writeSet.unlock();
                writeSet.clear();
                ReadSet.getLocal().clear();
            }
        };

        TransactionExecutor.OnAbort onAbort = new TransactionExecutor.OnAbort() {
            @Override
            public void execute() {
                WriteSet.getLocal().clear();
                ReadSet.getLocal().clear();
            }
        };

        TransactionExecutor te = new TransactionExecutor(validator, beforeOperation, afterOperation, onCommit, onAbort) {
            @Override
            public <E extends Copyable<E>> TransactionalObject<E> makeTransactional(E object) {
                return new LockingTransactionalObject<E>(object);
            }
        };

        return te;
    }
}
