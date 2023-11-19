package com.example.myapplication;

import androidx.annotation.NonNull;

import java.time.Instant;

public class Expense {
    private String category;
    private double amount;
    private String description;
    private Long date;
    private String imageUrl;
    private boolean recurring;


    public Expense() {
        // Default constructor required for Firebase
        this.category = "";
        this.amount = 0.0;
        this.description = "";
        this.date = Instant.now().toEpochMilli();
        this.imageUrl = "";
        this.recurring = false;
    }

    public Expense(String category, double amount, String description, Long date, String imageUrl) {
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.imageUrl = imageUrl;
        this.recurring = false;
    }

    public Expense(String category, double amount, String description, Long date, String imageUrl, boolean recurring) {
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.imageUrl = imageUrl;
        this.recurring = recurring;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean getRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    @NonNull
    @Override
    public String toString() {
        return "Expense{" +
                "category='" + category + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", recurring='" + recurring + '\'' +
                '}';
    }
}
