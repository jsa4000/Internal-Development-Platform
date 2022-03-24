package ${{ values.packageName }}.${{ values.artifactId }}.exceptions;

public class PaymentConflictException extends PaymentException{
    public PaymentConflictException() {super();}

    public PaymentConflictException(String message) {super(message);}

    public PaymentConflictException(String message, Throwable cause) {super(message, cause);}

    public PaymentConflictException(Throwable cause) {super(cause);}

    protected PaymentConflictException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
