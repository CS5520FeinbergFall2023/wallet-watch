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
    private ExpensesAdapter expensesAdapter;
    private Calendar currentCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_visualization);

        TextView headerTitle = findViewById(R.id.headerTitle);
        headerTitle.setText(getText(R.string.data_viz_header));

        this.textDate = findViewById(R.id.textDate);
        this.textBudget = findViewById(R.id.textBudgetValue);
        this.textRemaining = findViewById(R.id.textRemainingValue);
        this.categoryViewPager = findViewById(R.id.categoryViewPager);
        this.expensesRecyclerView = findViewById(R.id.expensesRecyclerView);

        addCategoriesToPager();
        updateDateDisplay();
        setupMonthIterationButtons();

        this.expensesRecyclerView = findViewById(R.id.expensesRecyclerView);
        this.expensesRecyclerView.setAdapter(new ExpensesAdapter(testExpenses()));
    }

    private void addCategoriesToPager() {
        //TODO: This will be an API call
        this.categoryViewPager.setAdapter(new CategoryAdapter(testCategories()));
    }

    private void updateDateDisplay() {
        String monthYear = this.currentCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) +
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

    private List<Expense> testExpenses() {
        List<Expense> expensesList = new ArrayList<>();
        expensesList.add(new Expense("Lunch", 15.00));
        expensesList.add(new Expense("Train Ticket", 20.00));

        return expensesList;
    }
}
