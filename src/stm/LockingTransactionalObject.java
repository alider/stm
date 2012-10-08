package stm;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class LockingTransactionalObject<T extends Copyable<T>> implements TransactionalObject<T> {
    private static final long LOCK_TIMEOUT = 2; // ms

    private T current;
    private volatile long version;
    private ReentrantLock lock = new ReentrantLock();

    public LockingTransactionalObject(T object) {
        current = object;
    }

    public T openForWrite() {
        switch (Transaction.getLocal().getStatus()) {
        case ACTIVE:
            WriteSet<T> writeSet = WriteSet.getLocal();
            T localCopy = writeSet.get(this);
            if (localCopy == null) {
                if (lock.isLocked()) {
                    throw new AbortedException();
                }
                localCopy = current.copy();
                writeSet.add(this, localCopy);
            }
            return localCopy;
        case COMMITTED:
            return current;
        case ABORTED:
            throw new AbortedException();
        default:
            throw new TransactionFailedException();
        }
    }

    public T openForRead() {
        switch (Transaction.getLocal().getStatus()) {
        case ACTIVE:
            WriteSet<T> writeSet = WriteSet.getLocal();
            if (writeSet.get(this) == null) {
                if (lock.isLocked()) {
                    throw new AbortedException();
                }
                ReadSet<T> readSet = ReadSet.getLocal();
                readSet.add(this);
                return current;
            } else {
                return writeSet.get(this);
            }
        case COMMITTED:
            return current;
        case ABORTED:
            throw new AbortedException();
        default:
            throw new TransactionFailedException();
        }
    }

    public boolean tryLock() {
        try {
            if (!lock.isHeldByCurrentThread()) {
                return lock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
            }
            return true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void unlock() {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    public boolean isValid() {
        switch (Transaction.getLocal().getStatus()) {
        case ACTIVE:
            boolean unlocked = !lock.isLocked() || lock.isHeldByCurrentThread();
            boolean unchanged = version <= LocalVersionClock.getCurrent();
            return unlocked && unchanged;
        case COMMITTED:
            return true;
        case ABORTED:
            return false;
        default:
            throw new TransactionFailedException();
        }
    }

    public boolean isLockedByCurrentThread() {
        return this.lock.isLocked() && this.lock.isHeldByCurrentThread();
    }

    public boolean isLockedByOtherThread() {
        return this.lock.isLocked() && !this.lock.isHeldByCurrentThread();
    }

    public void setVersion(long newVersion) {
        this.version = newVersion;
    }

    public String toString() {
        return String.valueOf(current);
    }
}