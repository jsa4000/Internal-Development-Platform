package ${{ values.packageName }}.${{ values.artifactId }}.controller;

import ${{ values.packageName }}.${{ values.artifactId }}.controller.dto.ErrorDto;
import ${{ values.packageName }}.${{ values.artifactId }}.exceptions.HttpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class ControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { Exception.class })
    protected ResponseEntity<ErrorDto> handleConflict(Exception ex, ServletWebRequest request) {
        log.error(ex.getMessage(), ex);
        ErrorDto error = new ErrorDto()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(ex.getMessage());
        return ResponseEntity.internalServerError().body(error);
    }

    @ExceptionHandler(value = { HttpException.class })
    protected ResponseEntity<ErrorDto> handleConflict(HttpException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        ErrorDto error = new ErrorDto()
                .code(ex.getStatus().value())
                .message(ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

}
