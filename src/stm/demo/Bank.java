package stm.demo;

public class Bank {

    public static final int NUM_ACCOUNTS = 10000;
    public static final int INIT_BALANCE = 1000000;

    private final Account[] accounts = new Account[NUM_ACCOUNTS];

    public Bank(AccountFactory accountFactory) {
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = accountFactory.create(i, INIT_BALANCE);
        }
    }

    public void handle(Transfer transfer) {
        Account from = accounts[transfer.getFromAccountId()];
        Account to = accounts[transfer.getToAccountId()];
        int amount = transfer.getAmount();
        from.transfer(to, amount);
    }

    public long getBalanceOf(int accountId) {
        return accounts[accountId].getBalance();
    }

    public long getTotalBalance() {
        long totalBalance = 0;
        for (int i = 0; i < accounts.length; i++) {
            long balance = accounts[i].getBalance();
            totalBalance += balance;
        }
        return totalBalance;
    }
}
