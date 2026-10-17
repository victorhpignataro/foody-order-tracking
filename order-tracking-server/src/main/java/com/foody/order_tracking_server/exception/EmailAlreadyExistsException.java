package com.foody.order_tracking_server.exception;

public class EmailAlreadyExistsException extends RuntimeException {

  public EmailAlreadyExistsException(String email) {
    super("The requested email already exists: " + email);
  }
}