package ${{ values.packageName }}.${{ values.artifactId }}.exceptions;

public class PaymentException extends Exception{
    public PaymentException() {super();}

    public PaymentException(String message) {super(message);}

    public PaymentException(String message, Throwable cause) {super(message, cause);}

    public PaymentException(Throwable cause) {super(cause);}

    protected PaymentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
