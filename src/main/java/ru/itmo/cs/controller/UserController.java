package ru.itmo.cs.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.cs.dto.*;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.entity.enums.UserRole;
import ru.itmo.cs.service.JwtService;
import ru.itmo.cs.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRegistrationDTO registrationDTO) {
        User registeredUser = userService.registerUser(registrationDTO);

        if (registeredUser.getRole() == UserRole.ADMIN) {
            return ResponseEntity.ok("You have become an administrator!");
        } else {
            return ResponseEntity.ok("User registered successfully.");
        }
    }

    @GetMapping("/admin-exists")
    public ResponseEntity<Boolean> checkAdminExists() {
        return ResponseEntity.ok(userService.doesAdminExist());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponseDTO> loginUser(@RequestBody UserLoginDTO input) {
        User authenticatedUser = userService.login(input);
        String token = jwtService.generateToken(authenticatedUser);

        AuthLoginResponseDTO authLoginResponseDTO = AuthLoginResponseDTO.builder()
                .token(token)
                .expiresIn(jwtService.getExpirationTime())
                .build();
        return ResponseEntity.ok(authLoginResponseDTO);
    }

    @GetMapping("/admin-requests")
    public ResponseEntity<List<UserDTO>> getAdminRequests() {
        return ResponseEntity.ok(userService.getAdminApprovalRequests());
    }

    @PostMapping("/request-admin")
    public ResponseEntity<String> requestAdminApproval(@RequestBody AdminApprovalDTO approvalDTO) {
        String responseMessage = userService.requestAdminApproval(approvalDTO.getUserId());
        return ResponseEntity.ok(responseMessage);
    }

    // Одобрение запроса на права администратора
    @PostMapping("/approve-admin")
    public ResponseEntity<String> approveAdmin(@RequestBody AdminApprovalDTO approvalDTO) {
        userService.approveAdminRequest(approvalDTO.getUserId());
        return ResponseEntity.ok("Admin rights granted");
    }

    // Отклонение запроса на права администратора
    @PostMapping("/reject-admin")
    public ResponseEntity<String> rejectAdmin(@RequestBody AdminApprovalDTO approvalDTO) {
        userService.rejectAdminRequest(approvalDTO.getUserId());
        return ResponseEntity.ok("Admin request rejected");
    }
}



