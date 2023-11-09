package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Accordion extends RecyclerView.Adapter<Accordion.ViewHolder> {

    private Context context;
    private List<BudgetCategory> categories;

    public Accordion(Context context, List<BudgetCategory> categories) {
        this.context = context;
        this.categories = categories;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.accordion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BudgetCategory category = this.categories.get(position);
        holder.categoryHeader.setText(
                this.context.getString(
                        R.string.accordian_header,
                        category.getName(),
                        category.getBudget()));
        holder.expensesLayout.removeAllViews();

        for (Expense expense : category.getExpenses()) {
            TextView expenseView = new TextView(holder.expensesLayout.getContext());
            expenseView.setText(
                    this.context.getString(
                            R.string.expense_item,
                            expense.getName(),
                            expense.getAmount()));
            holder.expensesLayout.addView(expenseView);
        }

        holder.categoryHeader.setOnClickListener(view -> {
            boolean visible = holder.expensesLayout.getVisibility() == View.VISIBLE;
            holder.expensesLayout.setVisibility(visible ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public int getItemCount() {
        return this.categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView categoryHeader;
        public LinearLayout expensesLayout;

        public ViewHolder(View view) {
            super(view);
            this.categoryHeader = view.findViewById(R.id.categoryHeader);
            this.expensesLayout = view.findViewById(R.id.expensesPerCategory);
        }
    }
}
