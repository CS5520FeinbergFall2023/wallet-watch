package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginPageActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String LOGGED_IN_KEY = "isLoggedIn";
    private static final String USERNAME = "username";
    private EditText usernameEditText, passwordEditText;
    private FirebaseHelper firebaseHelper;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        firebaseHelper = new FirebaseHelper();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        usernameEditText = findViewById(R.id.editTextUsernameLogin);
        passwordEditText = findViewById(R.id.editTextPasswordLogin);
        Button loginButton = findViewById(R.id.buttonLogin);
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginPageActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseHelper.loginUser(username, password, task -> {
                if (task.isSuccessful() && task.getResult()) {

                    Toast.makeText(LoginPageActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(LOGGED_IN_KEY, true);
                    editor.putString(USERNAME,username);
                    editor.apply();

                    Intent intent = new Intent(LoginPageActivity.this, MainFragmentActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginPageActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}

