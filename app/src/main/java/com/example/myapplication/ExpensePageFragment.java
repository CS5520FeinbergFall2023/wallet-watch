package com.example.myapplication;

import static android.content.Context.MODE_PRIVATE;
import static com.example.myapplication.MainActivity.PREFS_NAME;

import android.Manifest;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.dao.Category;
import com.example.myapplication.dao.Expense;
import com.example.myapplication.wallet_watch_util.WalletWatchStorageUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExpensePageFragment extends Fragment {

    private AutoCompleteTextView categoriesInput;
    private ArrayAdapter<Category> adapter;
    private EditText expenseAmountText, datePickerText, descriptionText;
    private long datePickerValue;
    private TextView imageCapturedMsgView;
    private SwitchMaterial recurringExpenseToggle;
    private FirebaseHelper firebaseHelper;
    private String username;

    private ActivityResultLauncher<String> photoPermissionRequest;
    //    private ActivityResultLauncher<String> photoGalleryPermissionRequest;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;

    private Uri tempUri;
    private boolean isExpenseImageUploaded;

    private Expense expense;

    private LinearLayout loadingMessage;

    private final String logTag = "EXP-PAGE";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense_page, container, false);

        // Text header
        TextView headerTitle = view.findViewById(R.id.headerTitle);
        headerTitle.setText(getText(R.string.add_expense_header));

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

        CategoriesViewModel categoriesViewModel = new ViewModelProvider(requireActivity()).get(CategoriesViewModel.class);

        categoriesViewModel.getCategoryList().observe(getViewLifecycleOwner(), this::updateCategoryOptions);

        // Initialize the launchers in your onCreate or onCreateView method:
        photoPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                // Camera permission granted, proceed with taking a picture
                launchCamera();
            } else {
                Snackbar.make(requireView(), "Camera permission is required!", Snackbar.LENGTH_SHORT).show();
            }
        });

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result) {
                // Camera result
                isExpenseImageUploaded = true;
                imageCapturedMsgView.setVisibility(View.VISIBLE);
            } else {
                // Gallery result
                Log.d(logTag, "Gallery Result");
            }
        });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                selectedImageUri -> {
                    if (selectedImageUri != null) {
                        isExpenseImageUploaded = true;
                        imageCapturedMsgView.setVisibility(View.VISIBLE);

                        tempUri = selectedImageUri;
                    } else {
                        Snackbar.make(requireView(), "Failed to pick image", Snackbar.LENGTH_SHORT).show();
                    }
                }
        );

        // Upload Button
        MaterialButton uploadExpenseButton = view.findViewById(R.id.add_expense_upload_button);
        uploadExpenseButton.setOnClickListener(v -> dispatchTakePictureIntent());

        // Upload Msg
        imageCapturedMsgView = view.findViewById(R.id.add_expense_image_captured_msg);
        imageCapturedMsgView.setVisibility(View.INVISIBLE);
        isExpenseImageUploaded = false;

        // Date Field
        datePickerText = view.findViewById(R.id.add_expense_date_picker);
        datePickerText.setOnClickListener(this::showDatePickerDialog);
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

        // Loading Message
        loadingMessage = view.findViewById(R.id.expense_progress_layout);
        loadingMessage.setVisibility(View.INVISIBLE);

        // restore instance state
        if (savedInstanceState != null) {
            restoreValues(savedInstanceState);
        }

        // back button check
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // finish action if form is clear
                if (isFormClear()) {
                    requireActivity().finish();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setMessage("Are you sure you want to exit?\nYou will lose any values entered.");

                builder.setPositiveButton("Exit", (dialog, which) -> {
                    requireActivity().finish();
                });
                builder.setNegativeButton("Dismiss", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();
            }
        };

        OnBackPressedDispatcher onBackPressedDispatcher = requireActivity().getOnBackPressedDispatcher();
        onBackPressedDispatcher.addCallback(getViewLifecycleOwner(), onBackPressedCallback);

        return view;
    }


    public boolean isFormClear() {
        String amountText = expenseAmountText.getText().toString();
        String category = categoriesInput.getText().toString();
        String description = descriptionText.getText().toString();
        String dateText = datePickerText.getText().toString();
        boolean recurring = recurringExpenseToggle.isChecked();

        return
                amountText.isEmpty()
                && category.isEmpty()
                && description.isEmpty()
                && dateText.isEmpty()
                && !recurring &&
                !isExpenseImageUploaded;
    }

    private void dispatchTakePictureIntent() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("How would you like to upload an Expense ?")
                .setItems(new CharSequence[]{"Take Photo", "Upload Photo"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // Take Photo
                            requestCameraPermissionAndLaunchCamera();
                            break;
                        case 1:
                            // Upload Photo
                            launchGallery();
                            break;
                    }
                });
        builder.show();
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

    private void launchGallery() {
        pickImageLauncher.launch("image/*");
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
        boolean recurring = recurringExpenseToggle.isChecked();
        String imageUrl;

        if (isExpenseImageUploaded) {
            imageUrl = WalletWatchStorageUtil.expenseImageFileName(expense);

            // tempUri and imageUrl when taking photo
            // expense-c29cb015-f9d8-41ca-9d65-7230f2052410.jpg

            // tempUri when uploading
            // content://media/picker/0/com.android.providers.media.photopicker/media/1000000027

            // so, use imageUrl as file name
        } else {
            imageUrl = "";
        }

        String imageFileName = imageUrl;

        // set expense values
        expense.setValues(category, amount, description, date, imageUrl, recurring);

        // set loading spinner
        loadingMessage.setVisibility(View.VISIBLE);

        firebaseHelper.createExpense(username, expense, v -> {
            if (isExpenseImageUploaded) {

                firebaseHelper.uploadImage(tempUri, imageFileName, e -> {
                    Toast.makeText(getContext(), "Expense Created!", Toast.LENGTH_SHORT).show();
                    onClear();
                });
            } else {
                Toast.makeText(getContext(), "Expense Created!", Toast.LENGTH_SHORT).show();
                onClear();
            }
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

        // clear upload msg
        imageCapturedMsgView.setVisibility(View.INVISIBLE);

        // clear loading msg
        loadingMessage.setVisibility(View.INVISIBLE);

        // clear temp uri
        tempUri = null;
        isExpenseImageUploaded = false;

        // new Expense object
        expense = new Expense();
    }

    private void updateCategoryOptions(List<Category> options) {
        List<Category> tempList = new ArrayList<>();

        for (Category category : options) {
            if (!Objects.equals(category.getCategory(), "Swipe for categories >>>")) {
                tempList.add(category);
            }
        }

        adapter.clear();
        adapter.addAll(tempList);
        adapter.notifyDataSetChanged();
    }

    public void showDatePickerDialog(View view) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            datePickerValue = selection;

            // update date picker text on modal positive button click
            datePickerText.setText(datePicker.getHeaderText());
        });

        // show picker date
        datePicker.show(getParentFragmentManager(), "EXPENSE_PAGE_DATE_PICKER");
    }

    public void restoreValues(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            int amount = savedInstanceState.getInt(ExpenseFieldKeys.AMOUNT);
            String category = savedInstanceState.getString(ExpenseFieldKeys.CATEGORY);
            long date_value = savedInstanceState.getLong(ExpenseFieldKeys.DATE_VALUE);
            String date_text = savedInstanceState.getString(ExpenseFieldKeys.DATE_TEXT);
            String description = savedInstanceState.getString(ExpenseFieldKeys.DESCRIPTION);
            boolean isExpUploaded = savedInstanceState.getBoolean(ExpenseFieldKeys.IS_EXPENSE_UPLOADED);
            boolean recurring = savedInstanceState.getBoolean(ExpenseFieldKeys.RECURRING);
            String savedUri = savedInstanceState.getString(ExpenseFieldKeys.TEMP_URI);

            Log.d(logTag, String.format(
                    "Restoring view with values:\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s", amount, category, date_value,
                    date_text, description, isExpUploaded, recurring, savedUri));

            expenseAmountText.setText(String.valueOf(amount));
            categoriesInput.setText(category);
            datePickerValue = date_value;
            datePickerText.setText(date_text);
            descriptionText.setText(description);
            isExpenseImageUploaded = isExpUploaded;
            recurringExpenseToggle.setChecked(recurring);

            if (savedUri != null && !savedUri.isEmpty()) {
                tempUri = Uri.parse(savedUri);
            }

            if (isExpenseImageUploaded) {
                imageCapturedMsgView.setVisibility(View.VISIBLE);
            }

            expenseAmountText.invalidate();
            categoriesInput.invalidate();
        }
    }

    private static class ExpenseFieldKeys {
        public static final String AMOUNT = "amount";
        public static final String CATEGORY = "category";
        public static final String DESCRIPTION = "description";
        public static final String DATE_VALUE = "date_value";
        public static final String DATE_TEXT = "date_text";
        public static final String RECURRING = "recurring";
        public static final String TEMP_URI = "temp_uri";
        public static final String IS_EXPENSE_UPLOADED = "is_expense_uploaded";
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        String category = categoriesInput.getText().toString();
        long date = datePickerValue;
        String dateText = datePickerText.getText().toString();
        String description = descriptionText.getText().toString();
        boolean recurring = recurringExpenseToggle.isChecked();

        int amount;
        String amountStr = expenseAmountText.getText().toString();
        if (amountStr.isEmpty()) {
            amount = 0;
        } else {
            amount = Integer.parseInt(expenseAmountText.getText().toString());
        }

        String savedUri;
        if (tempUri == null) {
            savedUri = "";
        } else {
            savedUri = tempUri.toString();
        }

        outState.putInt(ExpenseFieldKeys.AMOUNT, amount);
        outState.putString(ExpenseFieldKeys.CATEGORY, category);
        outState.putLong(ExpenseFieldKeys.DATE_VALUE, date);
        outState.putString(ExpenseFieldKeys.DATE_TEXT, dateText);
        outState.putString(ExpenseFieldKeys.DESCRIPTION, description);
        outState.putBoolean(ExpenseFieldKeys.IS_EXPENSE_UPLOADED, isExpenseImageUploaded);
        outState.putBoolean(ExpenseFieldKeys.RECURRING, recurring);
        outState.putString(ExpenseFieldKeys.TEMP_URI, savedUri);
    }

}

