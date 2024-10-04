package ru.itmo.cs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.cs.dto.UserLoginDTO;
import ru.itmo.cs.dto.UserRegistrationDTO;
import ru.itmo.cs.dto.AdminApprovalDTO;
import ru.itmo.cs.entity.City;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.entity.UserRole;
import ru.itmo.cs.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

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

    public String login(UserLoginDTO loginDTO) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(),
                        loginDTO.getPassword())
        );

        UserDetails userDetails = loadUserByUsername(loginDTO.getUsername());

        return jwtService.generateToken(userDetails);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional
    public User requestAdminApproval(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() != UserRole.USER) {
            throw new IllegalArgumentException("Only users with USER role can request admin rights");
        }

        user.setPendingAdminApproval(true);
        return userRepository.save(user);
    }

    @Transactional
    public User approveAdmin(AdminApprovalDTO approvalDTO) {
        User user = userRepository.findById(approvalDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isPendingAdminApproval()) {
            throw new IllegalArgumentException("No pending admin approval for this user");
        }

        user.setRole(UserRole.ADMIN);
        user.setPendingAdminApproval(false);

        return userRepository.save(user);
    }

    public User getCurrentUser() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public boolean canModifyCity(City city) {
        User currentUser = getCurrentUser();
        return city.getCreatedBy().equals(currentUser) || currentUser.getRole() == UserRole.ADMIN;
    }
}