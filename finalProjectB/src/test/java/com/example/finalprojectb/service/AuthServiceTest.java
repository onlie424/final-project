package com.example.finalprojectb.service;

import com.example.finalprojectb.DTO.AuthResponse;
import com.example.finalprojectb.DTO.LoginRequest;
import com.example.finalprojectb.DTO.RegisterRequest;
import com.example.finalprojectb.model.User;
import com.example.finalprojectb.repo.UserRepository;
import com.example.finalprojectb.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthService.
 *
 * Covers registration and login flows, including a dedicated test that uses a
 * real BCryptPasswordEncoder to empirically verify NFR3 (passwords are stored
 * as BCrypt hashes and never as plaintext).
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    // ---------- Shared fixtures ----------
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("student@test.com");
        registerRequest.setPassword("plaintext-password");
        registerRequest.setFullName("Test Student");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("student@test.com");
        loginRequest.setPassword("plaintext-password");
    }

    // ============================================================
    //  register()
    // ============================================================

    @Nested
    class Register {

        @Test
        void register_validRequest_persistsUserAndReturnsToken() {
            when(userRepository.existsByEmail("student@test.com")).thenReturn(false);
            when(passwordEncoder.encode("plaintext-password")).thenReturn("hashed-password");
            when(jwtUtil.generateToken("student@test.com", User.Role.USER)).thenReturn("jwt-token");

            AuthResponse response = authService.register(registerRequest);

            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getEmail()).isEqualTo("student@test.com");
            assertThat(response.getFullName()).isEqualTo("Test Student");
            assertThat(response.getRole()).isEqualTo(User.Role.USER);
        }

        @Test
        void register_alwaysAssignsUserRoleNotAdmin() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(jwtUtil.generateToken(anyString(), any(User.Role.class))).thenReturn("token");

            authService.register(registerRequest);

            ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(savedUser.capture());
            assertThat(savedUser.getValue().getRole()).isEqualTo(User.Role.USER);
        }

        @Test
        void register_callsPasswordEncoderBeforeSaving() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode("plaintext-password")).thenReturn("hashed-password");
            when(jwtUtil.generateToken(anyString(), any(User.Role.class))).thenReturn("token");

            authService.register(registerRequest);

            ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(savedUser.capture());
            assertThat(savedUser.getValue().getPassword())
                    .isEqualTo("hashed-password")
                    .isNotEqualTo("plaintext-password");
        }

        @Test
        void register_existingEmail_throwsAndDoesNotPersist() {
            when(userRepository.existsByEmail("student@test.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Email already exists");

            verify(userRepository, never()).save(any());
            verify(jwtUtil, never()).generateToken(anyString(), any(User.Role.class));
        }

        /**
         * NFR3 verification.
         *
         * Uses a REAL BCryptPasswordEncoder rather than a mock so we can assert
         * directly on the stored password format. This proves end-to-end that
         * passwords are hashed before persistence and never stored in plaintext.
         */
        @Test
        void register_NFR3_passwordPersistedAsBcryptHash() {
            // Replace the mocked encoder with a real BCrypt instance for this test only.
            ReflectionTestUtils.setField(authService, "passwordEncoder",
                    new BCryptPasswordEncoder());

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(jwtUtil.generateToken(anyString(), any(User.Role.class))).thenReturn("token");

            authService.register(registerRequest);

            ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(savedUser.capture());

            String stored = savedUser.getValue().getPassword();
            assertThat(stored)
                    .as("stored password must not be plaintext")
                    .isNotEqualTo("plaintext-password");
            assertThat(stored)
                    .as("stored password must use a BCrypt hash prefix")
                    .matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
            assertThat(new BCryptPasswordEncoder().matches("plaintext-password", stored))
                    .as("encoder must verify the original plaintext against the stored hash")
                    .isTrue();
        }
    }

    // ============================================================
    //  login()
    // ============================================================

    @Nested
    class Login {

        private User existingUser;

        @BeforeEach
        void seedExistingUser() {
            existingUser = new User();
            existingUser.setId(7L);
            existingUser.setEmail("student@test.com");
            existingUser.setFullName("Test Student");
            existingUser.setPassword("hashed-password");
            existingUser.setRole(User.Role.USER);
        }

        @Test
        void login_validCredentials_returnsTokenAndUserDetails() {
            when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches("plaintext-password", "hashed-password")).thenReturn(true);
            when(jwtUtil.generateToken("student@test.com", User.Role.USER)).thenReturn("jwt-token");

            AuthResponse response = authService.login(loginRequest);

            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getUserId()).isEqualTo(7L);
            assertThat(response.getRole()).isEqualTo(User.Role.USER);
        }

        @Test
        void login_unknownEmail_throwsInvalidCredentials() {
            when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid credentials");

            verify(jwtUtil, never()).generateToken(anyString(), any(User.Role.class));
        }

        @Test
        void login_wrongPassword_throwsInvalidCredentialsAndDoesNotIssueToken() {
            when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches("plaintext-password", "hashed-password")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid credentials");

            verify(jwtUtil, never()).generateToken(anyString(), any(User.Role.class));
        }

        @Test
        void login_adminUser_tokenContainsAdminRole() {
            existingUser.setRole(User.Role.ADMIN);
            when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(jwtUtil.generateToken("student@test.com", User.Role.ADMIN)).thenReturn("admin-jwt");

            AuthResponse response = authService.login(loginRequest);

            assertThat(response.getRole()).isEqualTo(User.Role.ADMIN);
            assertThat(response.getToken()).isEqualTo("admin-jwt");
            verify(jwtUtil).generateToken("student@test.com", User.Role.ADMIN);
        }
    }
}
