package stm.demo;

import static org.junit.Assert.assertEquals;

import java.util.Random;
import java.util.concurrent.Callable;

import org.junit.Test;

import stm.Executors;
import stm.TransactionExecutor;

public class Runner {

    public static final int NUM_OPS = 1000000;
    public static final int NUM_THREADS = 2;
    public static final int NUM_OPS_PER_THREAD = NUM_OPS / NUM_THREADS;
    public static final int WRITES_PERC = 20;

    private AbstractBankClient[] threads = new AbstractBankClient[NUM_THREADS];

    @Test
    public void transfer_ofree() throws Exception {
        runTestWith(Executors.newOFreeExecutor());
    }

    @Test
    public void transfer_locking() throws Exception {
        runTestWith(Executors.newLockingExecutor());
    }

    private void runTestWith(final TransactionExecutor te) throws Exception {
        Bank bank = new Bank(new AccountFactory() {
            @Override
            public Account create(int accountId, int initBalance) {
                return new TransactionalAccount(te.makeTransactional(new Account(accountId, initBalance)));
            }
        });

        run(bank, te);
    }

    private void run(Bank bank, TransactionExecutor te) throws Exception {
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new TransactionalBankClient(te, bank);
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        long totalDuration = 0;
        for (int i = 0; i < threads.length; i++) {
            totalDuration += threads[i].getDuration();
        }

        System.out.println("Finished in " + ((totalDuration) / 1000 / 1000 / NUM_THREADS) + " ms");
        System.out.println(te.dumpStatistics());

        long expectedTotalBalance = (long) Bank.NUM_ACCOUNTS * Bank.INIT_BALANCE;
        long totalBalance = bank.getTotalBalance();
        assertEquals(expectedTotalBalance, totalBalance);
    }

    static interface BankClient {

        long getBalance(int accountId);

        void transfer(Transfer transfer);
    }

    static abstract class AbstractBankClient extends Thread implements BankClient {

        private long duration;
        private long accumulatedBalance;
        private Random random = new Random();

        protected Bank bank;

        public AbstractBankClient(Bank bank) {
            this.bank = bank;
        }

        public void run() {
            long start = System.nanoTime();
            for (int i = 0; i < NUM_OPS_PER_THREAD; i++) {
                int rn = random.nextInt(100);
                if (rn < WRITES_PERC) {
                    transfer(new Transfer(
                            random.nextInt(Bank.NUM_ACCOUNTS), 
                            random.nextInt(Bank.NUM_ACCOUNTS),
                            random.nextInt(1000)));
                } else {
                    accumulatedBalance += getBalance(random.nextInt(Bank.NUM_ACCOUNTS));
                }
            }
            this.duration = System.nanoTime() - start;
        }

        public long getDuration() {
            return this.duration;
        }

        public long getAccumulatedBalance() {
            return this.accumulatedBalance;
        }
    }

    static class TransactionalBankClient extends AbstractBankClient {

        private TransactionExecutor te;

        public TransactionalBankClient(TransactionExecutor te, Bank bank) {
            super(bank);
            this.te = te;
        }

        @Override
        public long getBalance(final int accountId) {
            try {
                return te.execute(new Callable<Long>() {
                    public Long call() {
                        return bank.getBalanceOf(accountId);
                    }
                });
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        public void transfer(final Transfer transfer) {
            try {
                te.execute(new Callable<Void>() {
                    public Void call() {
                        bank.handle(transfer);
                        return null;
                    }
                });
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
