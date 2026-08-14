package com.lumiora.auth.service;

import com.lumiora.auth.dto.LoginRequest;
import com.lumiora.auth.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}