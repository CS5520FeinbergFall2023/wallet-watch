package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO:
 * 2. Date Popup
 * 3. Get Values from inputs
 * 4. Clear button
 * 1. API Category Dropdown Options
 * 5. API Submit
 * 6. Clear values after submit
 */
public class ExpensePageActivity extends AppCompatActivity {

    private List<BudgetCategory> categories;

    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter<BudgetCategory> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_page);


        // Categories
        categories = getInitialOptions();
        autoCompleteTextView = findViewById(R.id.add_expense_category_text);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        autoCompleteTextView.setAdapter(adapter);

        // Nav Bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.expense);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                startActivity(new Intent(getApplicationContext(), HomePageActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.budget) {
                startActivity(new Intent(getApplicationContext(), BudgetPageActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.expense) {
                return true;
            } else if (item.getItemId() == R.id.data) {
                startActivity(new Intent(getApplicationContext(), DataVisualizationActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (item.getItemId() == R.id.account) {
                startActivity(new Intent(getApplicationContext(), AccountPageActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }

            return false;
        });
    }

    public void showDatePickerDialog(View view) {

    }

    private void updateCategoryOptions(List<BudgetCategory> options) {
        adapter.clear();
        adapter.addAll(options);
        adapter.notifyDataSetChanged();
    }

    // Will be an API call later
    private List<BudgetCategory> getInitialOptions() {
        List<BudgetCategory> options = new ArrayList<>();

        options.add(new BudgetCategory("Food"));
        options.add(new BudgetCategory("Travel"));
        options.add(new BudgetCategory("Other"));
        return options;
    }
}