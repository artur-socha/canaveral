package pl.codewise.samples.spring.webapp.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pl.codewise.samples.spring.webapp.rest.model.ErrorResponse;

@ControllerAdvice
public class ErrorHandlingController extends ResponseEntityExceptionHandler {

    private static final String description = "There were errors.";

    @ExceptionHandler
    public ResponseEntity<Object> handleInputValidationException(IllegalArgumentException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return this.handleExceptionInternal(
                ex,
                new ErrorResponse(status, description, ex.getMessage()),
                new HttpHeaders(),
                status, request);
    }
}
