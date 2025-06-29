package com.example.demo3.controller;

import com.example.demo3.dto.UserLoginDTO;
import com.example.demo3.dto.UserRegistrationRequestDTO;
import com.example.demo3.dto.UserUpdateRequestDTO;
import com.example.demo3.service.UserService;
import jakarta.validation.Valid;
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
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequestDTO request) {
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginDTO request) {
        return ResponseEntity.ok(userService.loginUser(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getMyProfile(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userService.getMyProfile(token));
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable @Positive Long id) {
        return ResponseEntity.ok().body(userService.getUserProfile(id));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestHeader("Authorization") String token,
                                               @Valid @RequestBody UserUpdateRequestDTO request) {
        userService.updateUserProfile(token, request);
        return ResponseEntity.ok("Profile updated");
    }
}
