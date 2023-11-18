package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String LOGGED_IN_KEY = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check if the user is already logged in
        if (prefs.getBoolean(LOGGED_IN_KEY, false)) {
            Intent intent = new Intent(MainActivity.this, MainFragmentActivity.class);
            startActivity(intent);
            finish();
        }

        // Login
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener((view -> loginPageActivity()));

        // Register
        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener((view -> registerPageActivity()));
    }

    public void loginPageActivity(){
        Intent intent = new Intent(this, LoginPageActivity.class);
        startActivity(intent);
    }

    public void registerPageActivity(){
        Intent intent = new Intent(this, RegisterPageActivity.class);
        startActivity(intent);
    }
}