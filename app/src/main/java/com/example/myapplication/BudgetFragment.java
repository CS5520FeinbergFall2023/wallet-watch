package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.example.myapplication.MainActivity.PREFS_NAME;

public class BudgetFragment extends Fragment {

    private RecyclerView categoryRecyclerView;
    private CategoryAdapter categoryAdapter;

    private FirebaseHelper firebaseHelper;
    private String username;

    private List<BarEntry> barEntries = new ArrayList<>();
    private BarChart barChart;
    private final String[] categories = {"Food", "Entertainment", "Travel", "School", "Utilities"};
    private List<Integer> userAmounts = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0)); // Default values

    private final int[] BAR_COLORS = new int[]{
            Color.rgb(88, 146, 78),
            Color.rgb(123, 164, 103),
            Color.rgb(158, 186, 129),
            Color.rgb(194, 209, 148),
            Color.rgb(229, 235, 173)
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_budget_page, container, false);

        TextView headerTitle = view.findViewById(R.id.headerTitle);
        headerTitle.setText(getText(R.string.budget_header));

        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // Get username from local storage
        username = prefs.getString("username", "");

        firebaseHelper = new FirebaseHelper();

        // Bar Chart
        barChart = view.findViewById(R.id.barChart);

        // Initialize BarChart
        setupBarChart();

        // Initialize categoryRecyclerView
        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView);

        // RecyclerView
        categoryAdapter = new CategoryAdapter();
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Fetch and display the existing budget amounts for the category when the fragment loads
        firebaseHelper.getBudgetAmount(username, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAmount = 0;
                userAmounts.clear(); // Clear the list to avoid duplication

                for (String category : categories) {
                    Integer amount = 0;

                    for (DataSnapshot budgetSnapshot : snapshot.getChildren()) {
                        String budgetCategory = budgetSnapshot.child("category").getValue(String.class);
                        if (category.equals(budgetCategory)) {
                            amount = budgetSnapshot.child("amount").getValue(Integer.class);
                            if (amount != null) {
                                totalAmount += amount;
                            }
                            break; // Exit loop once the category is found
                        }
                    }

                    userAmounts.add(amount); // Store the user's initial amount
                }

                // Update the BarChart entries
                updateBarChartEntries(categories, userAmounts);

                // Update BarChart with the new entries
                setupBarChart(); // Call setupBarChart to update the BarChart

                // Notify the adapter that data has changed
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
            }
        });

        return view;
    }


    // Custom adapter for the RecyclerView
    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_category_card, parent, false);
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

                    holder.totalBudgetTextView.setText("$" + totalAmount);

                    // Update the BarChart entries
                    updateBarChartEntries(categories, userAmounts);

                    // Update BarChart with the new entries
                    updateBarChart();
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
            String existingAmount = holder.totalBudgetTextView.getText().toString().replace("$", "");
            input.setText(existingAmount);

            // Set up the OK button
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String amountStr = input.getText().toString();
                    int amount = TextUtils.isEmpty(amountStr) ? 0 : Integer.parseInt(amountStr);

                    // Update the RecyclerView immediately
                    holder.totalBudgetTextView.setText("$" + amount);

                    // Update the temporary variable with proper bounds checking
                    if (holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < userAmounts.size()) {
                        userAmounts.set(holder.getAdapterPosition(), amount);
                    }

                    // Update the BarChart entries using the temporary variable
                    updateBarChartEntries(categories, userAmounts);

                    // Update BarChart with the new entries
                    updateBarChart();

                    // Update the Firebase database
                    firebaseHelper.updateBudgetAmount(username, category, amount);

                    Toast.makeText(requireContext(), "Amount for " + category + " changed to $" + amount, Toast.LENGTH_SHORT).show();
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

    private void updateBarChartEntries(String[] categories, List<Integer> amounts) {
        barEntries.clear();

        if (!amounts.isEmpty()) {
            for (int i = 0; i < categories.length; i++) {
                barEntries.add(new BarEntry((float) i, amounts.get(i)));
            }
        }
    }

    private void configureBarChart() {
        // Disable the legend
        Legend legend = barChart.getLegend();
        legend.setEnabled(false);

        barChart.setExtraBottomOffset(35f);

        // X-axis Customization
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getCategoryLabels()));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setCenterAxisLabels(false);
        xAxis.setLabelCount(categories.length);
        xAxis.setTextSize(15f);

        // Customize Y-axis
        barChart.getAxisLeft().setTextSize(15f);
        barChart.getAxisRight().setEnabled(false);

        // Remove description label text on the side
        barChart.getDescription().setEnabled(false);
    }

    private void setupBarChart() {

        BarDataSet dataSet = new BarDataSet(barEntries, "");
        dataSet.setColors(BAR_COLORS);
        dataSet.setDrawValues(false);

        BarData data = new BarData(dataSet);
        barChart.setData(data);

        configureBarChart();
        barChart.invalidate();
    }

    private void updateBarChart() {
        BarDataSet dataSet = new BarDataSet(barEntries, "");
        dataSet.setColors(BAR_COLORS);
        dataSet.setDrawValues(false);

        BarData data = new BarData(dataSet);
        barChart.setData(data);

        configureBarChart();
        barChart.invalidate();
    }

    // Helper method to get category labels
    private List<String> getCategoryLabels() {
        List<String> labels = new ArrayList<>();
        for (String category : categories) {
            labels.add(category);
        }
        return labels;
    }

}
