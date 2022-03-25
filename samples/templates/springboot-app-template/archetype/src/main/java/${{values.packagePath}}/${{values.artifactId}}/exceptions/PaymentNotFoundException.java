package ${{ values.packageName }}.${{ values.artifactId }}.exceptions;

public class PaymentNotFoundException extends PaymentException{
    public PaymentNotFoundException() {super();}

    public PaymentNotFoundException(String message) {super(message);}

    public PaymentNotFoundException(String message, Throwable cause) {super(message, cause);}

    public PaymentNotFoundException(Throwable cause) {super(cause);}

    protected PaymentNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
