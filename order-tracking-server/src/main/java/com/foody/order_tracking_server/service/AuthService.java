package com.foody.order_tracking_server.service;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.foody.order_tracking_server.dto.auth.LoginRequestDto;
import com.foody.order_tracking_server.dto.auth.RegisterRequestDto;
import com.foody.order_tracking_server.exception.EmailAlreadyExistsException;
import com.foody.order_tracking_server.exception.InvalidCredentialsException;
import com.foody.order_tracking_server.model.User;
import com.foody.order_tracking_server.repository.UserRepository;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  public void register(RegisterRequestDto request) {
    String email = normalizeEmail(request.email());

    if (userRepository.findByEmail(email).isPresent()) {
      throw new EmailAlreadyExistsException(email);
    }

    User user = new User();
    user.setName(request.name().strip());
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(request.password()));

    try {
      userRepository.save(user);
    } catch (DataIntegrityViolationException e) {
      throw new EmailAlreadyExistsException(email);
    }
  }

  public LoginResult authenticate(LoginRequestDto request) {
    User user = userRepository.findByEmail(normalizeEmail(request.email()))
        .orElseThrow(InvalidCredentialsException::new);

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new InvalidCredentialsException();
    }

    String token = jwtService.generateToken(user.getEmail());
    return new LoginResult(token, user.getName());
  }

  private static String normalizeEmail(String email) {
    return email.strip().toLowerCase(Locale.ROOT);
  }

  public record LoginResult(String token, String nome) {
  }
}