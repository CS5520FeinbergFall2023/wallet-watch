package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DataVisualizationActivity extends AppCompatActivity {

    private TextView textDate, textBudget, textRemaining, textExpenses;
    private ViewPager2 categoryViewPager;
    private RecyclerView expensesRecyclerView;
    private Calendar currentCalendar = Calendar.getInstance();
    private FirebaseHelper firebaseHelper;
    private List<Expense> expensesList;
    private List<ExpenseItem> expensesForMonth;
    private List<Budget> budgetList;
    private List<BudgetCategory> budgetCategoriesList;
    private ExpensesAdapter expensesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_visualization);

        TextView headerTitle = findViewById(R.id.headerTitle);
        headerTitle.setText(getText(R.string.data_viz_header));

        this.firebaseHelper = new FirebaseHelper();

        this.expensesList = new ArrayList();
        this.expensesForMonth = new ArrayList<>();
        this.budgetList = new ArrayList();
        this.budgetCategoriesList = new ArrayList();

        this.textDate = findViewById(R.id.textDate);
        this.textBudget = findViewById(R.id.textBudgetValue);
        this.textRemaining = findViewById(R.id.textRemainingValue);
        this.textExpenses = findViewById(R.id.textExpenseValue);
        this.categoryViewPager = findViewById(R.id.categoryViewPager);

        this.expensesRecyclerView = findViewById(R.id.expensesRecyclerView);
        this.expensesRecyclerView.setHasFixedSize(true);
        this.expensesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.expensesAdapter = new ExpensesAdapter(testExpenses());
        this.expensesRecyclerView.setAdapter(this.expensesAdapter);

        retrieveData();
        addCategoriesToPager();
        updateVisualValues();
        setupMonthIterationButtons();
    }

    private void addCategoriesToPager() {
        this.categoryViewPager.setAdapter(new CategoryAdapter(this.budgetCategoriesList));
    }

    private void updateVisualValues() {

        for (Budget budget : this.budgetList) {
            if (Objects.equals(budget.getCategory(), this.budgetCategoriesList.get(this.categoryViewPager.getCurrentItem()).getCategoryName())) {
                this.textBudget.setText(String.valueOf(budget.getAmount()));
            }
        }

        this.expensesForMonth.clear();
        double total = 0;

        for (Expense expense : this.expensesList) {
            if (inCurrentMonth(expense.getDate())) {
                this.expensesForMonth.add(new ExpenseItem(expense.getDescription(), expense.getAmount()));
                total += expense.getAmount();
            }
        }

        this.expensesAdapter.setExpenses(this.expensesForMonth);
        this.expensesAdapter.notifyDataSetChanged();

        this.textExpenses.setText(String.valueOf(total));

        updateDateDisplay();
    }

    private void retrieveData() {
        String userId = "kartik";

        this.firebaseHelper.getUserExpenses(userId, expensesList -> {
            this.expensesList.clear();
            this.expensesList.addAll(expensesList);
        });

        this.firebaseHelper.getUserBudgets(userId, budgetList -> {
            this.budgetList.clear();
            this.budgetList.addAll(budgetList);
        });

        testCategories();

        Log.v("Expenses", this.expensesList.toString());
        Log.v("Budgets", this.budgetList.toString());
    }

    private void updateDateDisplay() {
        String monthYear = this.currentCalendar.getDisplayName(
                Calendar.MONTH,
                Calendar.SHORT,
                Locale.getDefault()) +
                " " +
                this.currentCalendar.get(Calendar.YEAR);

        this.textDate.setText(monthYear);
    }

    private void setupMonthIterationButtons() {
        Button btnPreviousMonth = findViewById(R.id.btnPreviousMonth);
        Button btnNextMonth = findViewById(R.id.btnNextMonth);

        btnPreviousMonth.setOnClickListener(v -> {
            this.currentCalendar.add(Calendar.MONTH, -1);
            retrieveData();
            updateVisualValues();
        });

        btnNextMonth.setOnClickListener(v -> {
            this.currentCalendar.add(Calendar.MONTH, 1);
            retrieveData();
            updateVisualValues();
        });
    }

    private void testCategories() {
        this.budgetCategoriesList.add(new BudgetCategory("Food"));
        this.budgetCategoriesList.add(new BudgetCategory("Travel"));
        this.budgetCategoriesList.add(new BudgetCategory("Entertainment"));
    }

    private boolean inCurrentMonth(long epochTime) {
        Calendar epochCalendar = Calendar.getInstance();
        epochCalendar.setTimeInMillis(epochTime);

        int epochMonth = epochCalendar.get(Calendar.MONTH);
        int epochYear = epochCalendar.get(Calendar.YEAR);

        int currentMonth = this.currentCalendar.get(Calendar.MONTH);
        int currentYear = this.currentCalendar.get(Calendar.YEAR);

        return epochMonth == currentMonth && epochYear == currentYear;
    }

    private List<ExpenseItem> testExpenses() {
        List<ExpenseItem> expensesList = new ArrayList<>();
        expensesList.add(new ExpenseItem("El Oriental De Cuba", 15.00));
        expensesList.add(new ExpenseItem("Tres Gatos", 20.00));

        return expensesList;
    }
}
