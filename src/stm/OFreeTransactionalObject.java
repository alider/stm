package stm;

import java.util.concurrent.atomic.AtomicReference;

public class OFreeTransactionalObject<T extends Copyable<T>> implements TransactionalObject<T> {
    private AtomicReference<Locator> locator;

    public OFreeTransactionalObject(T object) {
        locator = new AtomicReference<Locator>(new Locator(object));
    }

    public T openForWrite() {
        Transaction me = Transaction.getLocal();
        switch (me.getStatus()) {
        case ACTIVE:
            Locator locator = this.locator.get();
            if (me == locator.owner) {
                return locator.current;
            }
            Locator newLocator = new Locator(me);
            while (!Thread.currentThread().isInterrupted()) {
                Locator oldLocator = this.locator.get();
                Transaction currentOwner = oldLocator.owner;
                switch (currentOwner.getStatus()) {
                case COMMITTED:
                    newLocator.backup = oldLocator.current;
                    break;
                case ABORTED:
                    newLocator.backup = oldLocator.backup;
                    break;
                case ACTIVE:
                    ContentionResolver.getLocal().resolve(me, currentOwner);
                    continue;
                }
                newLocator.current = newLocator.backup.copy();
                if (this.locator.compareAndSet(oldLocator, newLocator)) {
                    return newLocator.current;
                }
            }
            throw new AbortedException();
        case COMMITTED:
            return openOutsideTransaction();
        case ABORTED:
            throw new AbortedException();
        default:
            throw new TransactionFailedException();
        }
    }

    public T openForRead() {
        Transaction me = Transaction.getLocal();
        switch (me.getStatus()) {
        case ACTIVE:
            Locator locator = this.locator.get();
            if (me == locator.owner) {
                return locator.current;
            }
            Locator newLocator = new Locator(me);
            while (!Thread.currentThread().isInterrupted()) {
                Locator oldLocator = this.locator.get();
                Transaction currentOwner = oldLocator.owner;
                switch (currentOwner.getStatus()) {
                case COMMITTED:
                    newLocator.current = oldLocator.current;
                    break;
                case ABORTED:
                    newLocator.current = oldLocator.backup;
                    break;
                case ACTIVE:
                    ContentionResolver.getLocal().resolve(me, currentOwner);
                    continue;
                }
                if (this.locator.compareAndSet(oldLocator, newLocator)) {
                    return newLocator.current;
                }
            }
            throw new AbortedException();
        case COMMITTED:
            return openOutsideTransaction();
        case ABORTED:
            throw new AbortedException();
        default:
            throw new TransactionFailedException();
        }
    }

    public boolean isValid() {
        switch (Transaction.getLocal().getStatus()) {
        case COMMITTED:
            return true;
        case ABORTED:
            return false;
        case ACTIVE:
            return true;
        default:
            throw new TransactionFailedException();
        }
    }

    private T openOutsideTransaction() {
        Locator locator = this.locator.get();
        switch (locator.owner.getStatus()) {
        case COMMITTED:
            return locator.current;
        case ABORTED:
            return locator.backup;
        default:
            throw new TransactionFailedException("Transactions conflict");
        }
    }

    private class Locator {
        Transaction owner;
        T backup;
        T current;

        Locator(T version) {
            this.owner = Transaction.COMMITTED;
            this.current = version;
        }

        Locator(Transaction owner) {
            this.owner = owner;
        }
    }
}