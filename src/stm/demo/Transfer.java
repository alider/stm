package stm.demo;

public class Transfer {

    private int fromAccountId;
    private int toAccountId;
    private int amount;

    public Transfer(int fromId, int toId, int amount) {
        this.fromAccountId = fromId;
        this.toAccountId = toId;
        this.amount = amount;
    }

    public int getFromAccountId() {
        return fromAccountId;
    }

    public int getToAccountId() {
        return toAccountId;
    }

    public int getAmount() {
        return amount;
    }

    public String toString() {
        return "Transfer [from=" + this.fromAccountId + ", to=" + this.toAccountId + ", amount=" + this.amount + "]";
    }
}
