package com.foody.order_tracking_server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.foody.order_tracking_server.dto.auth.LoginRequestDto;
import com.foody.order_tracking_server.dto.auth.RegisterRequestDto;
import com.foody.order_tracking_server.exception.EmailAlreadyExistsException;
import com.foody.order_tracking_server.exception.InvalidCredentialsException;
import com.foody.order_tracking_server.model.User;
import com.foody.order_tracking_server.repository.UserRepository;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private RegisterRequestDto registerRequest;
    private LoginRequestDto loginRequest;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setName("João Silva");
        sampleUser.setEmail("joao@email.com");
        sampleUser.setPassword("encodedPassword");

        registerRequest = new RegisterRequestDto("João Silva", "joao@email.com", "password123");
        loginRequest = new LoginRequestDto("joao@email.com", "password123");
    }

    @Test
    void register_WhenEmailNotExists_ShouldCreateUser() {
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        authService.register(registerRequest);

        verify(userRepository).findByEmail("joao@email.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WhenEmailAlreadyExists_ShouldThrowEmailAlreadyExistsException() {
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(sampleUser));

        assertThatThrownBy(() -> authService.register(registerRequest))
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessageContaining("The requested email already exists: joao@email.com");

        verify(userRepository).findByEmail("joao@email.com");
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldNormalizeEmailBeforePersisting() {
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(new RegisterRequestDto("  João Silva  ", "  JOAO@Email.COM ", "password123"));

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("joao@email.com");
        assertThat(captor.getValue().getName()).isEqualTo("João Silva");
    }

    @Test
    void register_WhenDatabaseRejectsDuplicateEmail_ShouldThrowEmailAlreadyExistsException() {
        // Simula a condição de corrida: a verificação prévia não encontrou o
        // e-mail, mas a restrição única do banco rejeitou o insert.
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenThrow(
            new org.springframework.dao.DataIntegrityViolationException(
                "UNIQUE constraint failed: users.email"));

        assertThatThrownBy(() -> authService.register(registerRequest))
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessageContaining("joao@email.com");
    }

    @Test
    void authenticate_WhenValidCredentials_ShouldReturnLoginResult() {
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken("joao@email.com")).thenReturn("jwt-token");

        AuthService.LoginResult result = authService.authenticate(loginRequest);

        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.nome()).isEqualTo("João Silva");
        verify(userRepository).findByEmail("joao@email.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtService).generateToken("joao@email.com");
    }

    @Test
    void authenticate_ShouldNormalizeEmail() {
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken("joao@email.com")).thenReturn("jwt-token");

        authService.authenticate(new LoginRequestDto("  JOAO@Email.COM ", "password123"));

        verify(userRepository).findByEmail("joao@email.com");
    }

    @Test
    void authenticate_WhenUserNotFound_ShouldThrowInvalidCredentialsException() {
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate(loginRequest))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessage("Invalid credentials");

        verify(userRepository).findByEmail("joao@email.com");
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
    }

    @Test
    void authenticate_WhenWrongPassword_ShouldThrowInvalidCredentialsException() {
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.authenticate(new LoginRequestDto("joao@email.com", "wrongPassword")))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessage("Invalid credentials");

        verify(userRepository).findByEmail("joao@email.com");
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verifyNoInteractions(jwtService);
    }
}