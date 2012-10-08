package stm.demo;

import stm.TransactionalObject;

public class TransactionalAccount extends Account {

    private TransactionalObject<Account> transactionalObject;

    public TransactionalAccount(TransactionalObject<Account> transactionalObject) {
        this.transactionalObject = transactionalObject;
    }

    @Override
    public void deposit(long amount) {
        Account account = transactionalObject.openForWrite();
        account.deposit(amount);
        // if (!transactionalObject.isValid())
        // throw new AbortedException();
    }

    @Override
    public void withdraw(long amount) {
        Account account = transactionalObject.openForWrite();
        account.withdraw(amount);
        // if (!transactionalObject.isValid())
        // throw new AbortedException();
    }

    @Override
    public long getBalance() {
        Account account = transactionalObject.openForRead();
        long value = account.getBalance();
        // if (!transactionalObject.isValid())
        // throw new AbortedException();
        return value;
    }
}
