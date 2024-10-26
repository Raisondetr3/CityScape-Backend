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
import ru.itmo.cs.dto.UserDTO;
import ru.itmo.cs.dto.UserLoginDTO;
import ru.itmo.cs.dto.UserRegistrationDTO;
import ru.itmo.cs.dto.AdminApprovalDTO;
import ru.itmo.cs.entity.City;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.entity.enums.UserRole;
import ru.itmo.cs.repository.UserRepository;
import ru.itmo.cs.util.EntityMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private EntityMapper entityMapper;

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

    @Transactional
    public User registerUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.findByUsername(registrationDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("User with this username already exists");
        }

        User newUser = new User();
        newUser.setUsername(registrationDTO.getUsername());
        newUser.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        newUser.setRole(UserRole.USER);

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
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + input.getUsername()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Incorrect password", e);
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException("User not found: " + input.getUsername(), e);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public boolean doesAdminExist() {
        return userRepository.existsByRole(UserRole.ADMIN);
    }

    @Transactional
    public String requestAdminApproval(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() != UserRole.USER) {
            throw new IllegalArgumentException("Only users with USER role can request admin rights");
        }

        if (!doesAdminExist()) {
            user.setRole(UserRole.ADMIN);
            user.setPendingAdminApproval(false);
            userRepository.save(user);
            return "No admins in the system. User has been granted ADMIN rights immediately.";
        } else {
            user.setPendingAdminApproval(true);
            userRepository.save(user);
            return "Admin approval requested";
        }
    }


    @Transactional(readOnly = true)
    public List<UserDTO> getAdminApprovalRequests() {
        return userRepository.findAllByPendingAdminApprovalTrue().stream()
                .map(entityMapper::toUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveAdminRequest(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isPendingAdminApproval()) {
            throw new IllegalArgumentException("No pending admin approval for this user");
        }

        user.setRole(UserRole.ADMIN);
        user.setPendingAdminApproval(false);
        userRepository.save(user);
    }

    @Transactional
    public void rejectAdminRequest(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isPendingAdminApproval()) {
            throw new IllegalArgumentException("No pending admin approval for this user");
        }

        user.setPendingAdminApproval(false);
        userRepository.save(user);
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        } else {
            throw new IllegalStateException("Authentication principal is not of type UserDetails");
        }
    }


    public boolean canModifyCity(City city) {
        User currentUser = getCurrentUser();
        return city.getCreatedBy().equals(currentUser) || currentUser.getRole() == UserRole.ADMIN;
    }
}