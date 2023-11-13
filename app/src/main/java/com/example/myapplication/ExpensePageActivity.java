package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO:
 * 1. API Category Dropdown Options
 * 2. API Submit & (Clear values, toast)
 */
public class ExpensePageActivity extends AppCompatActivity {

    private AutoCompleteTextView categoriesInput;

    private ArrayAdapter<BudgetCategory> adapter;

    private EditText budgetAmountText;

    private EditText datePickerText;
    private long datePickerValue;

    private EditText notesText;

    private SwitchMaterial recurringExpenseToggle;

    private MaterialButton uploadExpenseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_page);

        // Budget Field
        budgetAmountText = findViewById(R.id.add_expense_budget);

        // Categories Field, Values
        categoriesInput = findViewById(R.id.add_expense_category_text);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        categoriesInput.setAdapter(adapter);

        updateCategoryOptions(getInitialOptions()); // will be API call later

        // Upload Button
        uploadExpenseButton = findViewById(R.id.add_expense_upload_button);

        // Date Field
        datePickerText = findViewById(R.id.add_expense_date_picker);
        datePickerValue = 0;

        // Notes Field
        notesText = findViewById(R.id.add_expense_notes);

        // Recurring Toggle Field
        recurringExpenseToggle = findViewById(R.id.add_expense_recurring_toggle);

        // Clear Button
        MaterialButton clearExpenseButton = findViewById(R.id.add_expense_clear_button);
        clearExpenseButton.setOnClickListener(v -> onClear());

        // Submit Button
        MaterialButton addExpenseButton = findViewById(R.id.add_expense_submit_button);
        addExpenseButton.setOnClickListener(v -> checkValues());

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

    /**
     * Check appropriate fields for validity. Submit if all valid.
     */
    private void checkValues() {
        if (checkValidField(budgetAmountText) && checkValidField(categoriesInput) &&
                checkValidField(datePickerText) && checkValidField(notesText)) {
            onSubmit();
        }
    }

    /**
     * Check if a text field is valid (non-empty). if it is invalid, set the error as "required"
     * @param textViewField text field
     * @return whether the field is valid or not (non-empty)
     */
    private boolean checkValidField(EditText textViewField) {
        if (textViewField.getText().toString().trim().isEmpty()) {
            textViewField.setError("required");
            return false;
        } else {
            textViewField.setError(null);
            return true;
        }
    }

    private void onSubmit() {
        Map<String, Object> expenses = new HashMap<>();
        expenses.put("budget", budgetAmountText.getText());
        expenses.put("categories", categoriesInput.getText());
        expenses.put("photo", "none");
        expenses.put("date", datePickerValue);
        expenses.put("notes", notesText.getText());
        expenses.put("recurring", recurringExpenseToggle.isChecked());

        Log.d("EXP-PAGE", expenses.toString());
    }

    /**
     * Clear/reset values. Clears errors as well.
     */
    private void onClear() {
        budgetAmountText.setText("");
        categoriesInput.setText("");
        datePickerText.setText("");
        datePickerValue = 0;
        notesText.setText("");
        recurringExpenseToggle.setChecked(false);


        // clear errors
        budgetAmountText.setError(null);
        categoriesInput.setError(null);
        datePickerText.setError(null);
        notesText.setError(null);
    }

    public void showDatePickerDialog(View view) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            datePickerValue = selection;

            // update date picker text on modal positive button click
            datePickerText.setText(datePicker.getHeaderText());
        });

        // show date picker
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
        options.add(new BudgetCategory("School"));
        options.add(new BudgetCategory("Other"));
        return options;
    }
}