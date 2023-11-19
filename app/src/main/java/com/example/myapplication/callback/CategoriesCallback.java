package com.example.myapplication.callback;

import com.example.myapplication.dao.Category;

import java.util.List;

public interface CategoriesCallback {
    void onCallback(List<Category> categoriesList);
}
