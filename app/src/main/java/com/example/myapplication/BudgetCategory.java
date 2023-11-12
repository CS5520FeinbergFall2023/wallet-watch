package com.example.myapplication;

public class BudgetCategory {
    private String categoryName;

    public BudgetCategory(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return this.categoryName;
    }
}