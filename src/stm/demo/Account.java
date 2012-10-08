package stm.demo;

import stm.Copyable;

public class Account implements Copyable<Account> {

    private int id;

    private long balance;

    public Account() {
    }

    public Account(int accountId, int initBalance) {
        this.id = accountId;
        this.balance = initBalance;
    }

    public void transfer(Account to, int amount) {
        this.withdraw(amount);
        to.deposit(amount);
    }

    public void deposit(long amount) {
        this.balance += amount;
    }

    public void withdraw(long amount) {
        if (balance - amount > 0) {
            this.balance -= amount;
        } else {
            throw new IllegalStateException("Not enough money");
        }
    }

    public long getBalance() {
        return this.balance;
    }

    public int getId() {
        return id;
    }

    @Override
    public Account copy() {
        Account copy = new Account();
        this.copyTo(copy);
        return copy;
    }

    @Override
    public void copyTo(Account destination) {
        destination.balance = this.balance;
    }

    @Override
    public String toString() {
        return "Account [id=" + this.id + ", balance=" + this.balance + "]";
    }

}
