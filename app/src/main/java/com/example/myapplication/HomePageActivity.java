package com.example.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class HomePageActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String LOGGED_IN_KEY = "isLoggedIn";

    //code for image launcher
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    FirebaseHelper firebaseHelper = new FirebaseHelper();
                    firebaseHelper.uploadImage(result);
                }
            }
    );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Button dataButton = findViewById(R.id.dataButton);
            dataButton.setOnClickListener(view -> {
            Intent intent = new Intent(HomePageActivity.this, DataVisualizationActivity.class);
            startActivity(intent);
        });

        Button logoutButton = findViewById(R.id.buttonLogout);
        Button chooseImageButton = findViewById(R.id.buttonUpload);

        chooseImageButton.setOnClickListener(v -> openImageChooser());

        logoutButton.setOnClickListener(v -> {

            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putBoolean(LOGGED_IN_KEY, false)
                    .apply();

            Intent intent = new Intent(HomePageActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    //code for image launcher
    private void openImageChooser() {
        pickImageLauncher.launch("image/*");
    }

}