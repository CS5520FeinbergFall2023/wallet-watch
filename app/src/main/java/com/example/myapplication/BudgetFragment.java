package com.example.myapplication;

import static android.content.Context.MODE_PRIVATE;
import static com.example.myapplication.MainActivity.PREFS_NAME;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BudgetFragment extends Fragment {

    private TextView monthYearTextView;
    private Calendar currentMonth;

    private RecyclerView categoryRecyclerView;
    private CategoryAdapter categoryAdapter;

    private FirebaseHelper firebaseHelper;
    private String username;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_budget_page, container, false);


        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // Get username from local storage
        username = prefs.getString("username","");

        firebaseHelper = new FirebaseHelper();


        // RecyclerView
        categoryAdapter = new CategoryAdapter();
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        categoryRecyclerView.setAdapter(categoryAdapter);

        return view;
    }


    // Custom adapter for the RecyclerView
    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

        private String[] categories = {"Food", "Entertainment", "Travel", "School", "Utilities"};

        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_category, parent, false);
            return new CategoryViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            String category = categories[position];
            holder.categoryNameTextView.setText(category);

            // Fetch and display the existing budget amounts for the category
            firebaseHelper.getBudgetAmount(username, new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int totalAmount = 0;

                    for (DataSnapshot budgetSnapshot : snapshot.getChildren()) {
                        String budgetCategory = budgetSnapshot.child("category").getValue(String.class);
                        if (category.equals(budgetCategory)) {
                            Integer amount = budgetSnapshot.child("amount").getValue(Integer.class);
                            if (amount != null) {
                                totalAmount += amount;
                            }
                        }
                    }

                    holder.totalBudgetTextView.setText("Total: $" + totalAmount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle the error
                }
            });

            // Handle item click
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChangeAmountDialog(username, category, holder);
                }
            });
        }

        @Override
        public int getItemCount() {
            return categories.length;
        }

        // Show a dialog for changing the amount
        private void showChangeAmountDialog(String username, String category, CategoryViewHolder holder) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Change Amount");

            // Set up the input field
            final EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            // Fetch existing amount for the selected category
            String existingAmount = holder.totalBudgetTextView.getText().toString().replace("Total: $", "");
            input.setText(existingAmount);

            // Set up the OK button
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String amountStr = input.getText().toString();
                    int amount = TextUtils.isEmpty(amountStr) ? 0 : Integer.parseInt(amountStr);

                    // Update the total budget TextView
                    holder.totalBudgetTextView.setText("Total: $" + amount);

                    // Update the Firebase database
                    firebaseHelper.updateBudgetAmount(username, category, amount);
                }
            });

            // Set up the Cancel button
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            // Show the dialog
            builder.show();
        }

        // ViewHolder class
        class CategoryViewHolder extends RecyclerView.ViewHolder {
            TextView categoryNameTextView;
            TextView totalBudgetTextView;

            CategoryViewHolder(@NonNull View itemView) {
                super(itemView);
                categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
                totalBudgetTextView = itemView.findViewById(R.id.totalBudgetTextView);
            }
        }
    }
}