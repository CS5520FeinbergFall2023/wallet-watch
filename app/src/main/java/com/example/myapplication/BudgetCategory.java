package com.example.myapplication;

import java.util.List;

public class BudgetCategory {
    private String name;
    private int budget;
    private List<Expense> expenses;

    public BudgetCategory(String name, int budget, List<Expense> expenses) {
        this.name = name;
        this.budget = budget;
        this.expenses = expenses;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBudget() {
        return this.budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public List<Expense> getExpenses() {
        return this.expenses;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }
}
