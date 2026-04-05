package com.example.demostracion.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.example.demostracion.dto.EmailResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("Errores de validación: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }
    
    @ExceptionHandler(EmailException.class)
    public ResponseEntity<EmailResponse> handleEmailException(EmailException ex) {
        log.error("Error de correo: {}", ex.getMessage(), ex);
        EmailResponse response = EmailResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<EmailResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.error("Archivo demasiado grande: {}", ex.getMessage());
        EmailResponse response = EmailResponse.error("El archivo adjunto es demasiado grande");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }
    
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<EmailResponse> handleNoSuchElementException(NoSuchElementException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        EmailResponse response = EmailResponse.error("Recurso no encontrado");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<EmailResponse> handleGenericException(Exception ex) {
        log.error("Error inesperado", ex);
        EmailResponse response = EmailResponse.error("Error interno del servidor");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}