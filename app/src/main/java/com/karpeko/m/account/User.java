package com.karpeko.m.account;

import com.google.firebase.Timestamp;

public class User {

    private String username;
    private String email;
    private Timestamp createdAt;

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.createdAt = Timestamp.now();
    }

    public User() {}

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
