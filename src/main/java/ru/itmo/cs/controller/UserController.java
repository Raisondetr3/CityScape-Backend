package ru.itmo.cs.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.cs.dto.AdminApprovalDTO;
import ru.itmo.cs.dto.UserLoginDTO;
import ru.itmo.cs.dto.UserRegistrationDTO;
import ru.itmo.cs.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRegistrationDTO registrationDTO) {
        userService.registerUser(registrationDTO);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody UserLoginDTO loginDTO) {
        String token = userService.login(loginDTO);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/request-admin")
    public ResponseEntity<String> requestAdminApproval(@RequestBody AdminApprovalDTO approvalDTO) {
        userService.requestAdminApproval(approvalDTO.getUserId());
        return ResponseEntity.ok("Admin approval requested");
    }

    @PostMapping("/approve-admin")
    public ResponseEntity<String> approveAdmin(@RequestBody AdminApprovalDTO approvalDTO) {
        userService.approveAdmin(approvalDTO);
        return ResponseEntity.ok("Admin rights granted");
    }
}

