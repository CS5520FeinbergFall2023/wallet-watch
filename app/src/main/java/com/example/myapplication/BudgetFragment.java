package com.example.myapplication;

import static android.content.Context.MODE_PRIVATE;
import static com.example.myapplication.MainActivity.PREFS_NAME;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BudgetFragment extends Fragment {

    private RecyclerView categoryRecyclerView;
    private CategoryAdapter categoryAdapter;

    private FirebaseHelper firebaseHelper;
    private String username;

    private List<BarEntry> barEntries = new ArrayList<>();
    private BarChart barChart;
    private String[] categories = {"Food", "Entertainment", "Travel", "School", "Utilities"};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_budget_page, container, false);

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
                    updateBarChartEntries(categories, snapshot);

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

    private void updateBarChartEntries(String[] categories, DataSnapshot budgetSnapshot) {
        barEntries.clear();

        for (String category : categories) {
            int totalAmount = 0;

            for (DataSnapshot snapshot : budgetSnapshot.getChildren()) {
                String budgetCategory = snapshot.child("category").getValue(String.class);
                if (category.equals(budgetCategory)) {
                    Integer amount = snapshot.child("amount").getValue(Integer.class);
                    if (amount != null) {
                        totalAmount += amount;
                    }
                }
            }

            barEntries.add(new BarEntry((float) barEntries.size(), totalAmount));
        }
    }

    private void setupBarChart() {
        // BarChart Config

        // Create a BarDataSet
        BarDataSet dataSet = new BarDataSet(barEntries, "");

        // Set custom colors for each entry
        dataSet.setColors(new int[]{Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW, Color.BLACK});

        dataSet.setValueTextColor(Color.TRANSPARENT);

        // Create a BarData object
        BarData data = new BarData(dataSet);

        barChart.setData(data);

        // Customize X-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getCategoryLabels()));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setCenterAxisLabels(false);
        xAxis.setLabelCount(categories.length); // Set the number of labels
        //xAxis.setLabelRotationAngle(-45); // Rotate labels for better visibility
        xAxis.setXOffset(-10f); // Set a custom offset to adjust the position of labels


        // Remove description label text on the side
        barChart.getDescription().setEnabled(false);

        // Set up legends
        Legend legend = barChart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setTextSize(12f);

        barChart.invalidate();
    }

    private void updateBarChart() {
        BarDataSet dataSet = new BarDataSet(barEntries, "");
        dataSet.setColors(new int[]{Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW, Color.GRAY});

        BarData data = new BarData(dataSet);

        barChart.setData(data);

        // Remove description label text on the side
        barChart.getDescription().setEnabled(false);

        // Set up legends
        Legend legend = barChart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setTextSize(12f);

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
