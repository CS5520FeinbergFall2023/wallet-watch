package com.example.myapplication;

public class Category {
    private String category;

    public Category() {
        this.category = "";
    }

    public Category(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return this.category;
    }
}
