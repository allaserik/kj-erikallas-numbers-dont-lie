package com.erikallas.ndl.api.auth;

public class AuthLoginRequest {
    private String email;
    private String password;

    public AuthLoginRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
