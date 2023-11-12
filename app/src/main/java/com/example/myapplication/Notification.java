package com.example.myapplication;

import java.util.Date;

public class Notification {
    private String type;
    private String message;
    private Date date;

    public Notification(String type, String message, Date date) {
        this.type = type;
        this.message = message;
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }

    // Add getters and setters
    public void setType(String type) {
        this.type = type;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
