package stm;

import java.util.HashSet;
import java.util.Set;

public class ReadSet<T extends Copyable<T>> {

    private Set<LockingTransactionalObject<T>> opened;

    private static ThreadLocal<ReadSet<?>> local = new ThreadLocal<ReadSet<?>>() {
        @SuppressWarnings("rawtypes")
        @Override
        protected ReadSet<?> initialValue() {
            return new ReadSet();
        }
    };

    public ReadSet() {
        this.opened = new HashSet<LockingTransactionalObject<T>>();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Copyable<T>> ReadSet<T> getLocal() {
        return (ReadSet<T>) local.get();
    }

    public void add(LockingTransactionalObject<T> object) {
        opened.add(object);
    }

    public void clear() {
        opened.clear();
    }

    public Set<LockingTransactionalObject<T>> openedObjects() {
        return opened;
    }
}
