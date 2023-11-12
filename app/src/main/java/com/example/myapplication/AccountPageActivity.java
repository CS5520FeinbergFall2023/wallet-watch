package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AccountPageActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String LOGGED_IN_KEY = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_page);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.budget);


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
                        startActivity(new Intent(getApplicationContext(), ExpensePageActivity.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                        return true;
                    } else if (item.getItemId() == R.id.data) {
                        startActivity(new Intent(getApplicationContext(), DataVisualizationActivity.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                        return true;
                    } else if (item.getItemId() == R.id.account) {
                        return true;
                    }

                    return false;
                });


        Button logoutButton = findViewById(R.id.buttonLogout);

                logoutButton.setOnClickListener(v -> {

            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putBoolean(LOGGED_IN_KEY, false)
                    .apply();

            Intent intent = new Intent(AccountPageActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}

