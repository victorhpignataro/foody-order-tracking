package com.foody.order_tracking_server.service;

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
    if (userRepository.findByEmail(request.email()).isPresent()) {
      throw new EmailAlreadyExistsException(request.email());
    }

    User user = new User();
    user.setName(request.name());
    user.setEmail(request.email());
    user.setPassword(passwordEncoder.encode(request.password()));
    userRepository.save(user);
  }

  public LoginResult authenticate(LoginRequestDto request) {
    User user = userRepository.findByEmail(request.email())
        .orElseThrow(InvalidCredentialsException::new);

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new InvalidCredentialsException();
    }

    String token = jwtService.generateToken(user.getEmail());
    return new LoginResult(token, user.getName());
  }

  public record LoginResult(String token, String nome) {
  }
}