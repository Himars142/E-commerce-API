package com.example.demo3.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRegistrationRequestDTO {
    @NotBlank
    @Size(max = 50)
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String passwordConfirmation;
    @NotBlank
    @Email
    private String email;
    @Size(max = 20)
    private String phone = null;

    public UserRegistrationRequestDTO() {
    }

    public UserRegistrationRequestDTO(String username,
                                      String password,
                                      String passwordConfirmation,
                                      String email,
                                      String phone) {
        this.username = username;
        this.password = password;
        this.passwordConfirmation = passwordConfirmation;
        this.email = email;
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public void setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
