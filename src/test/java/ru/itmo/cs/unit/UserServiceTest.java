package ru.itmo.cs.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.itmo.cs.adminStatus.AdminRequestStatusHandler;
import ru.itmo.cs.dto.auth.UserLoginDTO;
import ru.itmo.cs.dto.auth.UserRegistrationDTO;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.entity.enums.AdminRequestStatus;
import ru.itmo.cs.entity.enums.UserRole;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.repository.UserRepository;
import ru.itmo.cs.service.JwtService;
import ru.itmo.cs.service.UserService;
import ru.itmo.cs.util.EntityMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private JwtService jwtService;

    @Mock
    private Map<String, AdminRequestStatusHandler> statusHandlers;

    private User testUser;
    private UserRegistrationDTO registrationDTO;
    private UserLoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setPassword("encodedPassword");
        testUser.setRole(UserRole.USER);
        testUser.setAdminRequestStatus(AdminRequestStatus.NONE);

        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("testUser");
        registrationDTO.setPassword("password");

        loginDTO = new UserLoginDTO();
        loginDTO.setUsername("testUser");
        loginDTO.setPassword("password");
    }

    @Test
    @DisplayName("Успешная регистрация пользователя")
    void shouldRegisterUserSuccessfully() {
        // Arrange
        when(userRepository.findByUsername(registrationDTO.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registrationDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.registerUser(registrationDTO);

        // Assert
        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        assertEquals(UserRole.USER, result.getRole());
        verify(userRepository).findByUsername(registrationDTO.getUsername());
        verify(passwordEncoder).encode(registrationDTO.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Ошибка регистрации пользователя с существующим username")
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Arrange
        when(userRepository.findByUsername(registrationDTO.getUsername())).thenReturn(Optional.of(testUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(registrationDTO),
                "Expected exception for duplicate username"
        );

        assertEquals("Пользователь с таким username уже существует", exception.getMessage());
        verify(userRepository).findByUsername(registrationDTO.getUsername());
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("Успешный логин пользователя")
    void shouldLoginUserSuccessfully() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.login(loginDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername(loginDTO.getUsername());
    }

    @Test
    @DisplayName("Ошибка логина при неверных учетных данных")
    void shouldThrowExceptionOnBadCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> userService.login(loginDTO),
                "Expected exception for invalid credentials"
        );

        assertEquals("Неправильный пароль", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Получение текущего пользователя")
    void shouldGetCurrentUserSuccessfully() {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, List.of())
        );
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getCurrentUser();

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result);
        verify(userRepository).findByUsername(testUser.getUsername());
    }

    @Test
    @DisplayName("Ошибка получения текущего пользователя, если principal не является UserDetails")
    void shouldThrowExceptionWhenPrincipalIsNotUserDetails() {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("nonExistentUser", null, List.of())
        );

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> userService.getCurrentUser(),
                "Expected exception for invalid principal type"
        );

        assertEquals("Authentication principal не имеет типа UserDetails", exception.getMessage());
    }

    @Test
    @DisplayName("Успешный запрос на получение прав администратора")
    void shouldRequestAdminApprovalSuccessfully() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        String result = userService.requestAdminApproval(testUser.getId());

        // Assert
        assertEquals("В системе нет администраторов. Пользователю были немедленно предоставлены права ADMIN", result);
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).existsByRole(UserRole.ADMIN);
        verify(userRepository).save(testUser);
    }
}
