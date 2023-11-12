package com.example.myapplication;

import java.util.List;

public interface ExpensesCallback {
    void onCallback(List<Expense> expensesList);
}
