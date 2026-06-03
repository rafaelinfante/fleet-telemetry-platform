package net.rafaelinfante.fleet.web.error;

import java.net.URI;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Maps application exceptions to RFC 9457 problem responses. Bean-validation failures are already
 * rendered as {@code application/problem+json} by the base class (spring.mvc.problemdetails.enabled).
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String ERROR_BASE = "https://fleet.rafaelinfante.net/errors/";

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage(), "not-found");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", ex.getMessage(), "invalid-request");
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail, String slug) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(ERROR_BASE + slug));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
