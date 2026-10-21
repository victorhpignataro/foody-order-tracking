package com.foody.order_tracking_server.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.foody.order_tracking_server.dto.ErrorResponseDto;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<ErrorResponseDto> handleNotFound(OrderNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponseDto("ORDER_NOT_FOUND", ex.getMessage()));
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ErrorResponseDto> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponseDto("EMAIL_ALREADY_EXISTS", ex.getMessage()));
  }

  @ExceptionHandler(InvalidStatusTransitionException.class)
  public ResponseEntity<ErrorResponseDto> handleInvalidStatusTransition(
      InvalidStatusTransitionException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponseDto("INVALID_STATUS_TRANSITION", ex.getMessage()));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolation(
      DataIntegrityViolationException ex) {
    log.warn("Violação de integridade de dados: {}", ex.getMostSpecificCause().getClass().getSimpleName());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponseDto("DATA_CONFLICT",
            "Não foi possível concluir a operação por conflito de dados."));
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ErrorResponseDto> handleInvalidCredentials(InvalidCredentialsException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponseDto("INVALID_CREDENTIALS", ex.getMessage()));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponseDto> handleAuthentication(AuthenticationException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponseDto("UNAUTHORIZED", "Autenticação necessária."));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(new ErrorResponseDto("FORBIDDEN", "Acesso negado."));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(e -> e.getField() + ": " + e.getDefaultMessage())
        .findFirst()
        .orElse("Dados inválidos.");
    return ResponseEntity.badRequest()
        .body(new ErrorResponseDto("VALIDATION_ERROR", message));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponseDto> handleUnreadableBody(HttpMessageNotReadableException ex) {
    return ResponseEntity.badRequest()
        .body(new ErrorResponseDto("INVALID_REQUEST_BODY", "Corpo da requisição inválido."));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponseDto> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    return ResponseEntity.badRequest()
        .body(new ErrorResponseDto("INVALID_PARAMETER",
            "Parâmetro inválido: " + ex.getName()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleUnexpected(Exception ex) {
    if (ex instanceof ErrorResponse errorResponse) {
      HttpStatusCode status = errorResponse.getStatusCode();
      return ResponseEntity.status(status)
          .body(new ErrorResponseDto(codeFor(status), messageFor(status)));
    }

    log.error("Erro inesperado ao processar a requisição", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponseDto("INTERNAL_ERROR", "Erro interno inesperado."));
  }

  private static String codeFor(HttpStatusCode status) {
    return switch (status.value()) {
      case 400 -> "BAD_REQUEST";
      case 401 -> "UNAUTHORIZED";
      case 403 -> "FORBIDDEN";
      case 404 -> "NOT_FOUND";
      case 405 -> "METHOD_NOT_ALLOWED";
      case 406 -> "NOT_ACCEPTABLE";
      case 409 -> "CONFLICT";
      case 415 -> "UNSUPPORTED_MEDIA_TYPE";
      default -> "HTTP_" + status.value();
    };
  }

  private static String messageFor(HttpStatusCode status) {
    return switch (status.value()) {
      case 404 -> "Recurso não encontrado.";
      case 405 -> "Método HTTP não suportado para este recurso.";
      case 415 -> "Tipo de conteúdo não suportado.";
      default -> "A requisição não pôde ser processada.";
    };
  }
}
