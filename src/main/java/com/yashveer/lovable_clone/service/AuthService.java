package com.yashveer.lovable_clone.service;

import com.yashveer.lovable_clone.dto.auth.AuthResponse;
import com.yashveer.lovable_clone.dto.auth.LoginRequest;
import com.yashveer.lovable_clone.dto.auth.SignupRequest;
import org.jspecify.annotations.Nullable;

public interface AuthService {
    AuthResponse signup(SignupRequest signupRequest);

    AuthResponse login(LoginRequest request);
}
