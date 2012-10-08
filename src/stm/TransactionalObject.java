package stm;

public interface TransactionalObject<T extends Copyable<T>> {

    T openForRead();

    T openForWrite();

    boolean isValid();
}
