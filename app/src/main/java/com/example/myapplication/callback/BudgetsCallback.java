package com.example.myapplication.callback;

import com.example.myapplication.dao.Budget;

import java.util.List;

public interface BudgetsCallback {
    void onCallback(List<Budget> budgetList);
}
