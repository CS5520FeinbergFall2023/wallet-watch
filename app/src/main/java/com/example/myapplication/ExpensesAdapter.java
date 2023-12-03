package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.dao.Expense;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ExpensesAdapter extends RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder> {

    private List<Expense> expenses;
    private Context context;
    private FirebaseHelper firebaseHelper = new FirebaseHelper();
    private String username;
    private ExpenseDeleteCallback expenseDeleteCallback;

    public ExpensesAdapter(List<Expense> expenses, Context context, String username, ExpenseDeleteCallback callback) {
        this.expenses = expenses;
        this.context = context;
        this.username = username;
        this.expenseDeleteCallback = callback;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_item, parent, false);
        return new ExpenseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.descriptionText.setText(expense.getDescription());
        holder.amountText.setText(formatCurrency(expense.getAmount()));

        if (!Objects.equals(expense.getImageUrl(), "")) {
            holder.photoIcon.setVisibility(View.VISIBLE);
            holder.photoIcon.setOnClickListener(v -> showImageDialog(expense.getImageUrl(), this.context));
        } else {
            holder.photoIcon.setVisibility(View.INVISIBLE);
        }

        holder.deleteIcon.setOnClickListener(v -> deleteExpense(expense));
    }

    private void deleteExpense(Expense expense) {
        // Alert for when navigating away from the Expense Page
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to Delete?\n\nThis action cannot be undone.");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            firebaseHelper.deleteExpense(username, expense, x -> {
                expenses.remove(expense);
                notifyDataSetChanged();
                Toast.makeText(context, "Expense Deleted", Toast.LENGTH_SHORT).show();
                this.expenseDeleteCallback.onExpenseDeleted();
            });
        });
        builder.setNegativeButton("Dismiss", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }

    private void showImageDialog(String imageUrl, Context context) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.data_viz_photo);

        ImageView imageView = dialog.findViewById(R.id.photoReceipt);

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(screenWidth / 2, screenHeight / 2);
        imageView.setLayoutParams(params);

        this.firebaseHelper.getImageFirebase(imageUrl, imageView, context);

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }

    private String formatCurrency(double value) {
        return String.format(Locale.getDefault(), "$%.2f", value);
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final TextView descriptionText;
        private final TextView amountText;
        private final ImageView photoIcon;
        private final ImageView deleteIcon;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            this.descriptionText = itemView.findViewById(R.id.descriptionText);
            this.photoIcon = itemView.findViewById(R.id.photoIcon);
            this.deleteIcon = itemView.findViewById(R.id.deleteIcon);
            this.amountText = itemView.findViewById(R.id.amountText);
        }
    }
}
