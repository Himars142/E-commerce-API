package com.example.demo3.controller;

import com.example.demo3.dto.UserLoginDTO;
import com.example.demo3.dto.UserRegistrationRequestDTO;
import com.example.demo3.dto.UserUpdateRequestDTO;
import com.example.demo3.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestHeader(name = "User-Agent", required = false) String userAgent,
                                          @Valid @RequestBody UserRegistrationRequestDTO request) {
        return ResponseEntity.ok(userService.registerUser(request, userAgent));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestHeader(name = "User-Agent", required = false) String userAgent,
                                       @Valid @RequestBody UserLoginDTO request) {
        return ResponseEntity.ok(userService.loginUser(request, userAgent));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getMyProfile(@RequestHeader(name = "User-Agent", required = false) String userAgent,
                                          @RequestHeader("Authorization") @NotEmpty String token) {
        return ResponseEntity.ok(userService.getMyProfile(token, userAgent));
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getUserProfile(@RequestHeader(name = "User-Agent", required = false) String userAgent,
                                            @PathVariable @Positive Long id) {
        return ResponseEntity.ok().body(userService.getUserProfile(id, userAgent));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestHeader(name = "User-Agent", required = false) String userAgent,
                                               @RequestHeader("Authorization") @NotEmpty String token,
                                               @Valid @RequestBody UserUpdateRequestDTO request) {
        userService.updateUserProfile(token, request, userAgent);
        return ResponseEntity.ok("Profile updated");
    }
}
