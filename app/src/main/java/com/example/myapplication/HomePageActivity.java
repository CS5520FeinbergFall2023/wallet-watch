package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class HomePageActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String LOGGED_IN_KEY = "isLoggedIn";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Button logoutButton = findViewById(R.id.buttonLogout);

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
}