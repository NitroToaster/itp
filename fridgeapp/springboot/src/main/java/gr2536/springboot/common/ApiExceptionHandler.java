package gr2536.springboot.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> onValidation(MethodArgumentNotValidException ex) {
    var fields = ex.getBindingResult().getFieldErrors().stream()
        .collect(java.util.stream.Collectors.toMap(f -> f.getField(), f -> f.getDefaultMessage(), (a,b)->a));
    return ResponseEntity.badRequest().body(Map.of("code","VALIDATION_ERROR","fields",fields));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> onIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("code","BAD_REQUEST","message",ex.getMessage()));
  }
}
