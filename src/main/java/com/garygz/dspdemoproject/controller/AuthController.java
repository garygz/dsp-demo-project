package com.garygz.dspdemoproject.controller;

import com.garygz.dspdemoproject.auth.InMemoryUserStore;
import com.garygz.dspdemoproject.auth.JwtUtil;
import com.garygz.dspdemoproject.auth.LoginRequest;
import com.garygz.dspdemoproject.auth.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final InMemoryUserStore userStore;
    private final JwtUtil jwtUtil;

    @Value("${auth.dev.bypass.enabled:false}")
    private boolean devBypassEnabled;

    @Value("${auth.dev.bypass.user:test@test.com}")
    private String devBypassUser;

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

    @GetMapping("/dev-login")
    public ResponseEntity<LoginResponse> devLogin() {
        if (!devBypassEnabled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(new LoginResponse(jwtUtil.generate(devBypassUser)));
    }
}
