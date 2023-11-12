package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO:
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

    private EditText budgetAmountText;

    private TextInputEditText datePickerText;

    private EditText notesText;

    private SwitchMaterial recurringExpenseToggle;

    private MaterialButton clearExpenseButton;

    private MaterialButton addExpenseButton;

    private MaterialButton uploadExpenseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_page);

        // Budget Field
        budgetAmountText = findViewById(R.id.add_expense_budget);

        // Categories Field
        categories = getInitialOptions();
        autoCompleteTextView = findViewById(R.id.add_expense_category_text);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        autoCompleteTextView.setAdapter(adapter);

        // Upload Button
        uploadExpenseButton = findViewById(R.id.add_expense_upload_button);

        // Date Field
        datePickerText = findViewById(R.id.add_expense_date_picker);

        // Notes Field
        notesText = findViewById(R.id.add_expense_notes);

        // Recurring Toggle Field
        recurringExpenseToggle = findViewById(R.id.add_expense_recurring_toggle);

        // Clear Button
        clearExpenseButton = findViewById(R.id.add_expense_clear_button);

        // Submit Button
        addExpenseButton = findViewById(R.id.add_expense_submit_button);

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
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // update date picker text on modal positive button click
            datePickerText.setText(datePicker.getHeaderText());
        });

        // show
        datePicker.show(getSupportFragmentManager(), "EXPENSE_PAGE_DATE_PICKER");
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