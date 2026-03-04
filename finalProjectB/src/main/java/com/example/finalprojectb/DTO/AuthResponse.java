package com.example.finalprojectb.DTO;

import com.example.finalprojectb.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String fullName;
    private User.Role role;
    private Long userId;
    private int loginStreak;
}