package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DataVisualizationActivity extends AppCompatActivity {

    private TextView textDate, textBudget, textRemaining;
    private ViewPager2 categoryViewPager;
    private RecyclerView expensesRecyclerView;
    private Calendar currentCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_visualization);

        TextView headerTitle = findViewById(R.id.headerTitle);
        headerTitle.setText(getText(R.string.data_viz_header));

        this.textDate = findViewById(R.id.textDate);
        this.textBudget = findViewById(R.id.textBudget);
        this.textRemaining = findViewById(R.id.textRemaining);
        this.categoryViewPager = findViewById(R.id.categoryViewPager);
        this.expensesRecyclerView = findViewById(R.id.expensesRecyclerView);

        addCategoriesToPager();
        updateDateDisplay();
        setupMonthIterationButtons();
    }

    private void addCategoriesToPager() {
        this.categoryViewPager.setAdapter(new CategoryAdapter(testCategories()));
    }

    private void updateDateDisplay() {
        String monthYear = this.currentCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) +
                " " +
                this.currentCalendar.get(Calendar.YEAR);

        this.textDate.setText(monthYear);
    }

    private void setupMonthIterationButtons() {
        Button btnPreviousMonth = findViewById(R.id.btnPreviousMonth);
        Button btnNextMonth = findViewById(R.id.btnNextMonth);

        btnPreviousMonth.setOnClickListener(v -> {
            this.currentCalendar.add(Calendar.MONTH, -1);
            updateDateDisplay();
        });

        btnNextMonth.setOnClickListener(v -> {
            this.currentCalendar.add(Calendar.MONTH, 1);
            updateDateDisplay();
        });
    }

    private List<BudgetCategory> testCategories() {
        List<BudgetCategory> categories = new ArrayList<>();
        categories.add(new BudgetCategory("Food"));
        categories.add(new BudgetCategory("Travel"));

        return categories;
    }
}
