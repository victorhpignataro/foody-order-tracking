package com.foody.order_tracking_server.exception;

public class InvalidCredentialsException extends RuntimeException {
  public InvalidCredentialsException() {
    super("Invalid credentials");
  }
}
