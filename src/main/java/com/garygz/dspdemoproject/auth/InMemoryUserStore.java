package com.garygz.dspdemoproject.auth;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InMemoryUserStore {

    private static final Map<String, String> USERS = Map.of(
            "test@test.com", "DspDemoTest!"
    );

    public boolean validate(String email, String password) {
        return password.equals(USERS.get(email));
    }
}
