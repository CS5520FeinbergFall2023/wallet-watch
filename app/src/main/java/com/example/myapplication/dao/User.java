package com.example.myapplication.dao;

public class User {

    private String username;
    private String password;
    private Long registration_date;
    public User() {}

    public User(String username, String password, Long registrationDate) {
        this.username = username;
        this.password = password;
        this.registration_date = registrationDate;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public Long getRegistration_date() {
        return registration_date;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRegistration_date(Long registration_date) {
        this.registration_date = registration_date;
    }
}
