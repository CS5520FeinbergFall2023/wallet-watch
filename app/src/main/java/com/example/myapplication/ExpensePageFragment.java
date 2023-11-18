package com.example.myapplication;

import static android.content.Context.MODE_PRIVATE;
import static com.example.myapplication.MainActivity.PREFS_NAME;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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

public class ExpensePageFragment extends Fragment {

    private AutoCompleteTextView categoriesInput;
    private ArrayAdapter<Category> adapter;
    private EditText expenseAmountText, datePickerText, descriptionText;
    private long datePickerValue;
    private SwitchMaterial recurringExpenseToggle;
    private MaterialButton uploadExpenseButton;
    private FirebaseHelper firebaseHelper;
    private String username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense_page, container, false);

        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // Get username from local storage
        username = prefs.getString("username","NONE");

        // Firebase helper
        firebaseHelper = new FirebaseHelper();

        // Expense Field
        expenseAmountText = view.findViewById(R.id.add_expense_amount);

        // Categories Field, Values
        categoriesInput = view.findViewById(R.id.add_expense_category_text);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        categoriesInput.setAdapter(adapter);

        firebaseHelper.getCategories(username, new ValueEventListener() {
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
                Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        // Upload Button
        uploadExpenseButton = view.findViewById(R.id.add_expense_upload_button);

        // Date Field
        datePickerText = view.findViewById(R.id.add_expense_date_picker);
        datePickerText.setOnClickListener(v -> showDatePickerDialog());
        datePickerValue = 0;

        // Description Field
        descriptionText = view.findViewById(R.id.add_expense_description);

        // Recurring Toggle Field
        recurringExpenseToggle = view.findViewById(R.id.add_expense_recurring_toggle);

        // Clear Button
        MaterialButton clearExpenseButton = view.findViewById(R.id.add_expense_clear_button);
        clearExpenseButton.setOnClickListener(v -> onClear());

        // Submit Button
        MaterialButton addExpenseButton = view.findViewById(R.id.add_expense_submit_button);
        addExpenseButton.setOnClickListener(v -> checkValues());

        return view;
    }

    /**
     * Check appropriate fields for validity. Submit if all valid.
     */
    private void checkValues() {
        if (checkValidField(expenseAmountText) && checkValidField(categoriesInput) &&
                checkValidField(datePickerText) && checkValidField(descriptionText)) {
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
        double amount = Double.parseDouble(expenseAmountText.getText().toString());
        String description = descriptionText.getText().toString();
        Long date = datePickerValue;
        String imageUrl = "";
        boolean recurring = recurringExpenseToggle.isChecked();


        // create an Expense
        Expense expense = new Expense(category, amount, description, date, imageUrl, recurring);

        firebaseHelper.createExpense(username, expense, v -> {
            Toast.makeText(getContext(), "Expense Created!", Toast.LENGTH_SHORT).show();
            onClear();
        });
    }

    /**
     * Clear/reset values. Clears errors as well.
     */
    private void onClear() {
        expenseAmountText.setText("");
        categoriesInput.setText("");
        datePickerText.setText("");
        datePickerValue = 0;
        descriptionText.setText("");
        recurringExpenseToggle.setChecked(false);

        // clear errors
        expenseAmountText.setError(null);
        categoriesInput.setError(null);
        datePickerText.setError(null);
        descriptionText.setError(null);
    }

    public void showDatePickerDialog() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            datePickerValue = selection;

            // update date picker text on modal positive button click
            datePickerText.setText(datePicker.getHeaderText());
        });

        // show picker date
        datePicker.show(getParentFragmentManager(), "EXPENSE_PAGE_DATE_PICKER");
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

