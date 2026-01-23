package com.erikallas.ndl.api.emailverification;

public class ResendCodeRequest {
    private String email;

    public ResendCodeRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
