package com.example.demo3.dto;

import jakarta.validation.constraints.Size;

public class UserUpdateRequestDTO {
    @Size(max = 50)
    private String username;
    @Size(max = 100)
    private String firstName;
    @Size(max = 100)
    private String lastName;
    @Size(max = 20)
    private String phone;

    public UserUpdateRequestDTO() {
    }

    public UserUpdateRequestDTO(String username,
                                String firstName,
                                String lastName,
                                String phone) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
