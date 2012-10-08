package stm.demo;

public interface AccountFactory {

    public Account create(int accountId, int initBalance);
}
