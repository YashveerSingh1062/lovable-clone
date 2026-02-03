package com.yashveer.lovable_clone.service.impl;

import com.yashveer.lovable_clone.dto.auth.AuthResponse;
import com.yashveer.lovable_clone.dto.auth.LoginRequest;
import com.yashveer.lovable_clone.dto.auth.SignupRequest;
import com.yashveer.lovable_clone.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    @Override
    public AuthResponse signup(SignupRequest signupRequest) {
        return null;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        return null;
    }
}
