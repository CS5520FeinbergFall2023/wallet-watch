package com.example.myapplication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainFragmentActivity extends AppCompatActivity {

    private final String CURRENT_FRAGMENT = "currFrag";
    // Home activity is the default activity
    private int selectedFragment = R.id.home;
    private BottomNavigationView bottomNavigationView;
    private FirebaseHelper firebaseHelper;
    private CategoriesViewModel categoriesViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_fragment);

        this.firebaseHelper = new FirebaseHelper();

        this.categoriesViewModel = new ViewModelProvider(this).get(CategoriesViewModel.class);
        getCategories();

        this.bottomNavigationView = findViewById(R.id.bottomNavigationView);

        if (savedInstanceState != null) {
            this.selectedFragment = savedInstanceState.getInt(this.CURRENT_FRAGMENT);
        }

        setupBottomNavigation();
        this.bottomNavigationView.setSelectedItemId(this.selectedFragment);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(this.CURRENT_FRAGMENT, this.selectedFragment);
    }

    private void getCategories() {
        this.firebaseHelper.getCategories(categoryList -> {
            this.categoriesViewModel.setCategoryList(categoryList);
        });
    }

    private void setupBottomNavigation() {
        this.bottomNavigationView.setOnItemSelectedListener(item -> {
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
                this.selectedFragment = itemId;
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }
}
