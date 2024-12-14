package ru.itmo.cs.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.cs.dto.*;
import ru.itmo.cs.dto.auth.*;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.service.JwtService;
import ru.itmo.cs.service.UserService;
import ru.itmo.cs.util.EntityMapper;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    private final EntityMapper entityMapper;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRegistrationDTO registrationDTO) {
        userService.registerUser(registrationDTO);
        return ResponseEntity.ok("User registered successfully.");
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

    @GetMapping("/admin-requests/status")
    public ResponseEntity<String> getAdminRequestStatus(@RequestParam Long userId) {
        String statusMessage = userService.getAdminRequestStatus(userId);
        return ResponseEntity.ok(statusMessage);
    }

    @PostMapping("/request-admin")
    public ResponseEntity<String> requestAdminApproval(@RequestBody AdminApprovalDTO approvalDTO) {
        String responseMessage = userService.requestAdminApproval(approvalDTO.getUserId());
        return ResponseEntity.ok(responseMessage);
    }

    @PostMapping("/approve-admin")
    public ResponseEntity<String> approveAdmin(@RequestBody AdminApprovalDTO approvalDTO) {
        userService.approveAdminRequest(approvalDTO.getUserId());
        return ResponseEntity.ok("Admin rights granted");
    }

    @PostMapping("/reject-admin")
    public ResponseEntity<String> rejectAdmin(@RequestBody AdminApprovalDTO approvalDTO) {
        userService.rejectAdminRequest(approvalDTO.getUserId());
        return ResponseEntity.ok("Admin request rejected");
    }

    @GetMapping("/current")
    public ResponseEntity<UserDTO> getCurrentUser() {
        User currentUser = userService.getCurrentUser();
        UserDTO currentUserDTO = entityMapper.toUserDTO(currentUser);
        return ResponseEntity.ok(currentUserDTO);
    }

    @PutMapping("/current")
    public ResponseEntity<UserUpdateResponseDTO> updateCurrentUser(@RequestBody UserUpdateDTO userUpdateDTO) {
        UserUpdateResponseDTO response = userService.updateCurrentUser(userUpdateDTO);
        return ResponseEntity.ok(response);
    }
}