package stm;

public interface Copyable<T> {

    T copy();

    void copyTo(T destination);
}
