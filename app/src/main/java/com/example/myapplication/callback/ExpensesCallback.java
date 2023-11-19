package com.example.myapplication.callback;

import com.example.myapplication.dao.Expense;

import java.util.List;

public interface ExpensesCallback {
    void onCallback(List<Expense> expensesList);
}
