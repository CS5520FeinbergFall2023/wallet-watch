package com.example.myapplication;

import static android.content.Context.MODE_PRIVATE;
import static com.example.myapplication.MainActivity.PREFS_NAME;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.myapplication.dao.Category;
import com.example.myapplication.dao.Expense;
import com.example.myapplication.wallet_watch_util.WalletWatchStorageUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
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
    private FirebaseHelper firebaseHelper;
    private String username;

    private ActivityResultLauncher<String> photoPermissionRequest;
    private ActivityResultLauncher<Intent> takePictureLauncherIntent;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri tempUri;

    private Expense expense;

//    private final LinearLayout loadingMessage;

    private final String logTag = "EXP-PAGE";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense_page, container, false);

        // Expense Model
        expense = new Expense();

        // Get username from local storage
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        username = prefs.getString("username", "");

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

        // Initialize the launchers in your onCreate or onCreateView method:
        photoPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                // Camera permission granted, proceed with taking a picture
                launchCamera();
            } else {
                Snackbar.make(requireView(), "Camera permission is required!", Snackbar.LENGTH_SHORT).show();
            }
        });

//        takePictureLauncherIntent = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
//                        Log.d(logTag, "Here 1");
//                        Log.d(logTag, result.toString());
//                    }
//                }
//        );

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if(result) {
                Log.d(logTag, "URI to be used: " + tempUri);
                firebaseHelper.uploadImage(tempUri, v -> {
                    Toast.makeText(getContext(), "Image Uploaded!", Toast.LENGTH_SHORT).show();
                    Log.d(logTag, "Done with: " + v);
                });
            }
        });

        // Upload Button
        MaterialButton uploadExpenseButton = view.findViewById(R.id.add_expense_upload_button);
        uploadExpenseButton.setOnClickListener(v -> dispatchTakePictureIntent());


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

    private void dispatchTakePictureIntent() {
        requestCameraPermissionAndLaunchCamera();
    }

    // Function to request camera permission and launch the camera
    private void requestCameraPermissionAndLaunchCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // Camera permission is already granted, proceed with taking a picture
            launchCamera();
        } else {
            // Request camera permission
            photoPermissionRequest.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        tempUri = createImageUri();

        if (tempUri != null) {
            takePictureIntent.putExtra("uri", tempUri);
            takePictureLauncherIntent.launch(takePictureIntent);
        } else {
            // Handle the case where creating the URI failed
            Log.e(logTag, "Failed to create image URI");
        }
    }

    private void launchCamera() {
        tempUri = createImageUri();

        if (tempUri != null) {
            takePictureLauncher.launch(tempUri);
        } else {
            // Handle the case where creating the URI failed
            Log.e(logTag, "Failed to create image URI");
        }
    }

    private Uri createImageUri() {
        File imageFile = createImageFile();
        return FileProvider.getUriForFile(requireContext(), "com.example.myapplication.fileprovider", imageFile);
    }

    private File createImageFile() {
        // Generate an image filename from an Expense id
        String imageFileName = WalletWatchStorageUtil.expenseImageFileName(expense);

        // Get the app's cache directory
        File storageDir = requireContext().getCacheDir();

        // Create the temporary file
        File imageFile = new File(storageDir, imageFileName);

        // Save a file: path for use with ACTION_VIEW intents or other app-related logic
        String currentPhotoPath = imageFile.getAbsolutePath();
        Log.d(logTag, "Creating file at image path: " + currentPhotoPath);

        return imageFile;
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

        // set expense values
        expense.setValues(category, amount, description, date, imageUrl, recurring);

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

        // new Expense object
        expense = new Expense();
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

