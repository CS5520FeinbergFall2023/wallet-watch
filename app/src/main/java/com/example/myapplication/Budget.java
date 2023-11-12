package com.example.myapplication;

import java.time.Instant;
import java.util.List;

import java.util.Date;

public class Budget {
    private String category;
    private double amount;
    private Long startDate;
    private Long endDate;


    public Budget() {
        // Default constructor required for Firebase
        this.category = "";
        this.amount = 0.0;
        this.startDate = Instant.now().toEpochMilli();
        this.endDate = Instant.now().toEpochMilli();
    }

    public Budget(String category, double amount, Long startDate, Long endDate) {
        this.category = category;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "Budget{" +
                "category='" + category + '\'' +
                ", amount=" + amount +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}

