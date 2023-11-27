package com.example.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.dao.Expense;

import java.util.List;
import java.util.Locale;

public class ExpensesAdapter extends RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder> {

    private List<Expense> expenses;
    private Context context;
    private FirebaseHelper firebaseHelper = new FirebaseHelper();

    public ExpensesAdapter(List<Expense> expenses, Context context) {
        this.expenses = expenses;
        this.context = context;
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

        if (expense.getImageUrl() != null) {
            holder.photoIcon.setVisibility(View.VISIBLE);
            holder.photoIcon.setOnClickListener(v -> {
                showImageDialog("expense-403cedf7-2bbd-4a43-b562-d8aefe93c0d6.jpg", this.context);
//                showImageDialog(expense.getImageUrl(), this.context);
            });
        } else {
            holder.photoIcon.setVisibility(View.GONE);
        }
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
        private TextView descriptionText, amountText;
        private ImageView photoIcon;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            this.descriptionText = itemView.findViewById(R.id.descriptionText);
            this.photoIcon = itemView.findViewById(R.id.photoIcon);
            this.amountText = itemView.findViewById(R.id.amountText);
        }
    }
}
