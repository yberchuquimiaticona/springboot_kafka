package dev.ychuquimia.product_service.dto;

import java.time.Instant;
import org.springframework.http.HttpStatus;

public record ErrorResponse(
    Instant timestamp,
    int status,
    String code,
    String message,
    String path
) {

  public static ErrorResponse notFound(String message, String path) {
    return new ErrorResponse(Instant.now(), HttpStatus.NOT_FOUND.value(), "NOT_FOUND", message,
        path);
  }

  public static ErrorResponse validation(String message, String path) {
    return new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR",
        message, path);
  }

  public static ErrorResponse generic(String message, String path) {
    return new ErrorResponse(Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "INTERNAL_ERROR", message, path);
  }
}
