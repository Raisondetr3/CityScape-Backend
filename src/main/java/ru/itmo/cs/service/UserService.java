package ru.itmo.cs.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.adminStatus.AdminRequestStatusHandler;
import ru.itmo.cs.dto.auth.*;
import ru.itmo.cs.entity.City;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.entity.enums.AdminRequestStatus;
import ru.itmo.cs.entity.enums.UserRole;
import ru.itmo.cs.exception.UsernameAlreadyExistsException;
import ru.itmo.cs.repository.UserRepository;
import ru.itmo.cs.util.EntityMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private EntityMapper entityMapper;
    private Map<String, AdminRequestStatusHandler> statusHandlers;

    private JwtService jwtService;

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setEntityMapper(EntityMapper entityMapper) {
        this.entityMapper = entityMapper;
    }

    @Autowired
    public void setStatusHandlers(Map<String, AdminRequestStatusHandler> statusHandlers) {
        this.statusHandlers = statusHandlers;
    }

    @Autowired
    public void setJwtService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Transactional
    public User registerUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.findByUsername(registrationDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким username уже существует");
        }

        User newUser = new User();
        newUser.setUsername(registrationDTO.getUsername());
        newUser.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        newUser.setRole(UserRole.USER);
        newUser.setAdminRequestStatus(AdminRequestStatus.NONE); // Установка начального статуса

        return userRepository.save(newUser);
    }

    @Transactional
    public User login(UserLoginDTO input) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.getUsername(),
                            input.getPassword()));
            return userRepository.findByUsername(input.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + input.getUsername()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Неправильный пароль", e);
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException("Пользователь не найден: " + input.getUsername(), e);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
    }

    public boolean doesAdminExist() {
        return userRepository.existsByRole(UserRole.ADMIN);
    }

    @Transactional
    public String requestAdminApproval(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        if (user.getRole() != UserRole.USER) {
            throw new IllegalArgumentException("Только пользователи с ролью `USER` могут запросить права `ADMIN`");
        }

        if (!doesAdminExist()) {
            user.setRole(UserRole.ADMIN);
            user.setAdminRequestStatus(AdminRequestStatus.ACCEPTED);
            userRepository.save(user);
            return "В системе нет администраторов. Пользователю были немедленно предоставлены права ADMIN";
        } else {
            user.setAdminRequestStatus(AdminRequestStatus.PENDING);
            userRepository.save(user);
            return "Запрашивается одобрение администратора";
        }
    }

    @Transactional(readOnly = true)
    public String getAdminRequestStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        AdminRequestStatusHandler handler = statusHandlers.get(user.getAdminRequestStatus().name());
        if (handler == null) {
            throw new IllegalStateException("Не найден обработчик для статуса: " + user.getAdminRequestStatus());
        }
        return handler.getStatusMessage();
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAdminApprovalRequests() {
        return userRepository.findAllByAdminRequestStatus(AdminRequestStatus.PENDING).stream()
                .map(entityMapper::toUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveAdminRequest(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        if (user.getAdminRequestStatus() != AdminRequestStatus.PENDING) {
            throw new IllegalArgumentException("Нет ожидающих одобрения администратора для этого пользователя");
        }

        user.setRole(UserRole.ADMIN);
        user.setAdminRequestStatus(AdminRequestStatus.ACCEPTED);
        userRepository.save(user);
    }

    @Transactional
    public void rejectAdminRequest(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        if (user.getAdminRequestStatus() != AdminRequestStatus.PENDING) {
            throw new IllegalArgumentException("Нет ожидающих одобрения администратора для этого пользователя");
        }

        user.setAdminRequestStatus(AdminRequestStatus.REJECTED);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
        } else {
            throw new IllegalStateException("Authentication principal не имеет типа UserDetails");
        }
    }

    public boolean canModifyCity(City city) {
        User currentUser = getCurrentUser();
        return city.getCreatedBy().equals(currentUser) || currentUser.getRole() == UserRole.ADMIN;
    }

    @Transactional
    public UserUpdateResponseDTO updateCurrentUser(UserUpdateDTO userUpdateDTO) {
        User currentUser = getCurrentUser();

        if (userUpdateDTO.getUsername() != null && !userUpdateDTO.getUsername().isBlank()) {
            if (userRepository.findByUsername(userUpdateDTO.getUsername()).isPresent()) {
                throw new UsernameAlreadyExistsException("Имя пользователя уже занято.");
            }
            currentUser.setUsername(userUpdateDTO.getUsername());
        }

        if (userUpdateDTO.getPassword() != null && !userUpdateDTO.getPassword().isBlank()) {
            currentUser.setPassword(passwordEncoder.encode(userUpdateDTO.getPassword()));
        }

        User updatedUser = userRepository.save(currentUser);
        String newToken = jwtService.generateToken(updatedUser);
        return new UserUpdateResponseDTO(newToken, entityMapper.toUserDTO(updatedUser));
    }
}
