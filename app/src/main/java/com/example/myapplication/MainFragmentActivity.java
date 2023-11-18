package com.example.myapplication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainFragmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_fragment);

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            // This does not work as a switch... don't waste your time
            if (itemId == R.id.home) {
                selectedFragment = new HomePageFragment();
            } else if (itemId == R.id.budget) {
                selectedFragment = new BudgetFragment();
            } else if (itemId == R.id.expense) {
                selectedFragment = new ExpensePageFragment();
            } else if (itemId == R.id.data) {
                selectedFragment = new DataVisualizationFragment();
            } else if (itemId == R.id.account) {
                selectedFragment = new AccountPageFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        bottomNavigationView.setSelectedItemId(R.id.home);
    }
}
