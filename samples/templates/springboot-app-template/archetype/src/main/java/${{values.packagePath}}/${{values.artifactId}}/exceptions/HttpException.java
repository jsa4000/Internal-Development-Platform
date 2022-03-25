package ${{ values.packageName }}.${{ values.artifactId }}.exceptions;

import org.springframework.http.HttpStatus;

public class HttpException extends Exception{

    private final HttpStatus status;

    public HttpException(HttpStatus status) {
        super();
        this.status = status;
    }

    public HttpException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpException(HttpStatus status, Throwable cause) {
        super(cause);
        this.status = status;
    }

    protected HttpException(HttpStatus status, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
