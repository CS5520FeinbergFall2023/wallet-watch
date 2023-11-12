package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ExpensesAdapter extends RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder> {

    private List<ExpenseItem> expenses;

    public ExpensesAdapter(List<ExpenseItem> expenses) {
        this.expenses = expenses;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_item, parent, false);
        return new ExpenseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseItem expense = expenses.get(position);
        holder.descriptionText.setText(expense.getName());
        holder.amountText.setText(String.valueOf(expense.getAmount()));
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void setExpenses(List<ExpenseItem> expenses) {
        this.expenses = expenses;
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private TextView descriptionText;
        private TextView amountText;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            this.descriptionText = itemView.findViewById(R.id.descriptionText);
            this.amountText = itemView.findViewById(R.id.amountText);
        }
    }
}
