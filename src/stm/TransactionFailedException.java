package stm;

/**
 * Thrown outside of the framework indicates unexpected failure in transaction processing.
 */
public class TransactionFailedException extends RuntimeException {

    private static final long serialVersionUID = -3379989566410528384L;

    public TransactionFailedException() {

    }

    public TransactionFailedException(Throwable cause) {
        super(cause);
    }

    public TransactionFailedException(String msg) {
        super(msg);
    }
}
