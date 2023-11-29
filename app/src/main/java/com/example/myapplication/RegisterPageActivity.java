package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class RegisterPageActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    FirebaseHelper firebaseHelper;

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String LOGGED_IN_KEY = "isLoggedIn";
    private static final String USERNAME = "username";


    private SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        firebaseHelper = new FirebaseHelper();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        usernameEditText = findViewById(R.id.editTextUsername);
        passwordEditText = findViewById(R.id.editTextPassword);
        Button registerButton = findViewById(R.id.buttonRegister);

        TextView loginLink = findViewById(R.id.loginLink);

        // Set OnClickListener for the Register TextView
        loginLink.setOnClickListener(v -> {
            // Navigate to the RegistrationActivity (replace with your actual registration activity)
            Intent intent = new Intent(RegisterPageActivity.this, LoginPageActivity.class);
            startActivity(intent);
        });

        registerButton.setOnClickListener(view -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterPageActivity.this, "Please enter valid username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(username, password);
        });
    }

    private void registerUser(String username, String password) {
        firebaseHelper.registerOrLoginUser(username, password, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RegisterPageActivity.this, "User registration successful.", Toast.LENGTH_SHORT).show();

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(LOGGED_IN_KEY, true);
                editor.putString(USERNAME,username);
                editor.apply();

                Intent intent = new Intent(RegisterPageActivity.this, MainFragmentActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(RegisterPageActivity.this, "Registration failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}

