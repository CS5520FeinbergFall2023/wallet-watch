package com.example.myapplication;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.dao.Category;

import java.util.List;

public class CategoriesViewModel extends ViewModel {
    private MutableLiveData<List<Category>> categoryList = new MutableLiveData<>();

    public LiveData<List<Category>> getCategoryList() {
        return this.categoryList;
    }

    public void setCategoryList(List<Category> categories) {
        this.categoryList.setValue(categories);
    }
}