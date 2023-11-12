package com.example.myapplication;

import androidx.annotation.NonNull;

public class BudgetCategory {
    private String categoryName;

    public BudgetCategory(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    @NonNull
    @Override
    public String toString() {
        return this.categoryName;
    }
}