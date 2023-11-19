package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.dao.Category;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BudgetFragment extends Fragment {

    private TextView monthYearTextView;
    private Calendar currentMonth;

    private RecyclerView categoryRecyclerView;
    private CategoryAdapter categoryAdapter;

    private FirebaseHelper firebaseHelper;
    private ArrayAdapter<Category> adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_budget_page, container, false);

        firebaseHelper = new FirebaseHelper();

        // Find views
        monthYearTextView = view.findViewById(R.id.monthYearTextView);
        Button prevMonthButton = view.findViewById(R.id.prevMonthButton);
        Button nextMonthButton = view.findViewById(R.id.nextMonthButton);

        // Set up the initial month and year
        currentMonth = Calendar.getInstance();
        updateMonthYear(currentMonth);

        // Set up button click listeners
        prevMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth.add(Calendar.MONTH, -1);
                updateMonthYear(currentMonth);
            }
        });

        nextMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth.add(Calendar.MONTH, 1);
                updateMonthYear(currentMonth);
            }
        });

        // Set up the RecyclerView
        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView);
        categoryAdapter = new CategoryAdapter();
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        categoryRecyclerView.setAdapter(categoryAdapter);




        return view;
    }

    private void updateMonthYear(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String formattedMonthYear = sdf.format(calendar.getTime());
        monthYearTextView.setText(formattedMonthYear);
    }

    // Create a custom adapter for the RecyclerView
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

            // Set the initial total budget
            holder.totalBudgetTextView.setText("Total: $0");

            // Handle item click
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChangeAmountDialog(category, holder);
                }
            });
        }

        @Override
        public int getItemCount() {
            return categories.length;
        }

        // Show a dialog for changing the amount
        private void showChangeAmountDialog(String category, CategoryViewHolder holder) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Change Amount");

            // Set up the input field
            final EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            // Set up the OK button
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String amountStr = input.getText().toString();
                    int amount = TextUtils.isEmpty(amountStr) ? 0 : Integer.parseInt(amountStr);

                    // Update the total budget TextView
                    holder.totalBudgetTextView.setText("Total: $" + amount);

                    // TODO: Save the amount to your data structure or perform other actions
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



    private void updateCategoryOptions(List<String> options) {
        List<Category> budgetCategories = new ArrayList<>();

        // convert List<String> to List<BudgetCategory>
        options.forEach(category -> budgetCategories.add(new Category(category)));

        adapter.clear();
        adapter.addAll(budgetCategories);
        adapter.notifyDataSetChanged();
    }
}