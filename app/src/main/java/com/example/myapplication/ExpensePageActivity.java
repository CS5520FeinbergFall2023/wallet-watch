package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO:
 * 2. API Submit & (Clear values, toast)
 */
public class ExpensePageActivity extends AppCompatActivity {

    private AutoCompleteTextView categoriesInput;

    private ArrayAdapter<Category> adapter;

    private EditText budgetAmountText;

    private EditText datePickerText;
    private long datePickerValue;

    private EditText notesText;

    private SwitchMaterial recurringExpenseToggle;

    private MaterialButton uploadExpenseButton;

    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_page);

        // Firebase helper
        firebaseHelper = new FirebaseHelper();

        // Budget Field
        budgetAmountText = findViewById(R.id.add_expense_budget);

        // Categories Field, Values
        categoriesInput = findViewById(R.id.add_expense_category_text);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        categoriesInput.setAdapter(adapter);

        firebaseHelper.getCategories(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot budgetsSnapshot) {
                Set<String> categories = new HashSet<>();

                for (DataSnapshot budgetSnapshot : budgetsSnapshot.getChildren()) {
                    String category = budgetSnapshot.child("category").getValue(String.class);
                    categories.add(category);
                }

                // update categories dropdown
                updateCategoryOptions(new ArrayList<>(categories));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ExpensePageActivity.this, "Ok", Toast.LENGTH_SHORT).show();
            }
        });

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
     *
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
        String category = categoriesInput.getText().toString();
        double amount = Double.parseDouble(budgetAmountText.getText().toString());
        String description = notesText.getText().toString();
        Long date = datePickerValue;
        String imageUrl = "";

        // create valid expense
        Expense expense = new Expense(category, amount, description, date, imageUrl);

        firebaseHelper.createExpense(expense, v -> {
            Toast.makeText(this, "Expense Created!", Toast.LENGTH_SHORT).show();
            onClear();
        });
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

    private void updateCategoryOptions(List<String> options) {
        List<Category> budgetCategories = new ArrayList<>();

        // convert List<String> to List<BudgetCategory>
        options.forEach(category -> budgetCategories.add(new Category(category)));

        adapter.clear();
        adapter.addAll(budgetCategories);
        adapter.notifyDataSetChanged();
    }
}