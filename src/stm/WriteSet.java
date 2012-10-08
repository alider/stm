package stm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class WriteSet<T extends Copyable<T>> {

    private Map<LockingTransactionalObject<T>, T> opened;

    private static ThreadLocal<WriteSet<?>> local = new ThreadLocal<WriteSet<?>>() {
        @SuppressWarnings("rawtypes")
        @Override
        protected WriteSet<?> initialValue() {
            return new WriteSet();
        }
    };

    public WriteSet() {
        opened = new HashMap<LockingTransactionalObject<T>, T>();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Copyable<T>> WriteSet<T> getLocal() {
        return ((WriteSet<T>) local.get());
    }

    public boolean tryLock() {
        Stack<LockingTransactionalObject<T>> lockedObjects = new Stack<LockingTransactionalObject<T>>();
        for (LockingTransactionalObject<T> object : opened.keySet()) {
            if (object.tryLock()) {
                lockedObjects.push(object);
                if (object.isValid()) {
                    continue;
                } else {
                    object.unlock();
                }
            }
            for (LockingTransactionalObject<T> lockedObject : lockedObjects) {
                lockedObject.unlock();
            }
            return false;
        }
        return true;
    }

    public void unlock() {
        for (LockingTransactionalObject<T> object : opened.keySet()) {
            object.unlock();
        }
    }

    public T get(LockingTransactionalObject<T> key) {
        return opened.get(key);
    }

    public void add(LockingTransactionalObject<T> key, T value) {
        opened.put(key, value);
    }

    public void clear() {
        opened.clear();
    }

    public Set<LockingTransactionalObject<T>> openedObjects() {
        return opened.keySet();
    }

    public Copyable<T> getLocalCopyOf(LockingTransactionalObject<T> lockedObject) {
        return opened.get(lockedObject);
    }
}
