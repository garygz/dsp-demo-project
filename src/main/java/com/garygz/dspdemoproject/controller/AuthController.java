package com.garygz.dspdemoproject.controller;

import com.garygz.dspdemoproject.auth.InMemoryUserStore;
import com.garygz.dspdemoproject.auth.JwtUtil;
import com.garygz.dspdemoproject.auth.LoginRequest;
import com.garygz.dspdemoproject.auth.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final InMemoryUserStore userStore;
    private final JwtUtil jwtUtil;

    public AuthController(InMemoryUserStore userStore, JwtUtil jwtUtil) {
        this.userStore = userStore;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if (!userStore.validate(request.email(), request.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new LoginResponse(jwtUtil.generate(request.email())));
    }
}
