package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.dao.ExpenseItem;

import java.util.List;
import java.util.Locale;

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
        holder.amountText.setText(formatCurrency(expense.getAmount()));
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void setExpenses(List<ExpenseItem> expenses) {
        this.expenses = expenses;
    }

    private String formatCurrency(double value) {
        return String.format(Locale.getDefault(), "$%.2f", value);
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
