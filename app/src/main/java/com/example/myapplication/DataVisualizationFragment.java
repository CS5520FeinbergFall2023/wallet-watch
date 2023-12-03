package com.example.myapplication;

import static android.content.Context.MODE_PRIVATE;
import static com.example.myapplication.MainActivity.PREFS_NAME;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.dao.Budget;
import com.example.myapplication.dao.Category;
import com.example.myapplication.dao.Expense;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class DataVisualizationFragment extends Fragment implements ExpenseDeleteCallback {

    private TextView textDate, textBudget, textRemaining, textExpenses;
    private ViewPager2 categoryViewPager;
    private RecyclerView expensesRecyclerView;
    private Calendar currentCalendar = Calendar.getInstance();
    private FirebaseHelper firebaseHelper;
    private List<Expense> expensesList;
    private List<Expense> expensesForMonth;
    private List<Budget> budgetList;
    private List<Category> categoryList;
    private ExpensesAdapter expensesAdapter;
    private String username;
    private CategoriesViewModel viewModel;
    private int savedCategoryPosition;
    private long savedDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_visualization, container, false);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            TextView headerTitle = view.findViewById(R.id.headerTitle);
            headerTitle.setText(getText(R.string.data_viz_header));
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        this.username = prefs.getString("username","");

        this.firebaseHelper = new FirebaseHelper();

        this.viewModel = new ViewModelProvider(requireActivity()).get(CategoriesViewModel.class);

        this.expensesList = new ArrayList<>();
        this.expensesForMonth = new ArrayList<>();
        this.budgetList = new ArrayList<>();

        this.textDate = view.findViewById(R.id.textDate);
        this.textBudget = view.findViewById(R.id.textBudgetValue);
        this.textRemaining = view.findViewById(R.id.textRemainingValue);
        this.textExpenses = view.findViewById(R.id.textExpenseValue);
        this.categoryViewPager = view.findViewById(R.id.categoryViewPager);

        this.expensesRecyclerView = view.findViewById(R.id.expensesRecyclerView);
        this.expensesRecyclerView.setHasFixedSize(true);
        this.expensesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        this.expensesAdapter = new ExpensesAdapter(new ArrayList<>(), getContext(), username, this);
        this.expensesRecyclerView.setAdapter(this.expensesAdapter);

        this.viewModel.getCategoryList().observe(getViewLifecycleOwner(), categories -> {
            this.categoryList = categories;

            addCategoriesToPager(this.categoryList);
        });

        if (savedInstanceState != null) {
            this.savedCategoryPosition = savedInstanceState.getInt("savedCategoryPosition", -1);
            this.savedDate = savedInstanceState.getLong("savedDate", -1);

            if (this.savedDate != -1) {
                currentCalendar.setTimeInMillis(this.savedDate);
                updateDateDisplay();
            }
        }

        retrieveData();
        setupMonthIterationButtons(view);
        updateDateDisplay();

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("savedCategoryPosition", this.categoryViewPager.getCurrentItem());
        outState.putLong("savedDate", this.currentCalendar.getTimeInMillis());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (this.savedCategoryPosition != -1) {
            this.categoryViewPager.post(() -> this.categoryViewPager.setCurrentItem(this.savedCategoryPosition, false));
        }
    }

    private void addCategoriesToPager(List<Category> tempList) {
        this.categoryViewPager.setAdapter(new CategoryAdapter(tempList));

        this.categoryViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                retrieveData();
            }
        });
    }

    private boolean isCategorySame(String category) {
        return this.categoryList
                .get(this.categoryViewPager.getCurrentItem())
                .getCategory()
                .equals(category);
    }

    private double updateBudget() {
        double budget = 0;

        for (Budget categoryBudget : this.budgetList) {
            if (isCategorySame(categoryBudget.getCategory())) {
                budget = categoryBudget.getAmount();
                break;
            }
        }
        String formattedBudget = String.format(getResources().getString(R.string.formatted_currency), budget);
        this.textBudget.setText(formattedBudget);

        return budget;
    }

    private double updateExpenses() {
        double totalExpenses = 0;

        for (Expense expense : this.expensesList) {
            if (includeDetailedExpense(expense.getDate(), expense.getRecurring())
                    && isCategorySame(expense.getCategory())) {
                totalExpenses += expense.getAmount();
            }
        }

        String formattedExpenses = String.format(getResources().getString(R.string.formatted_currency), totalExpenses);
        this.textExpenses.setText(formattedExpenses);
        return totalExpenses;
    }

    private void updateRemaining(double remaining) {
        String formattedRemaining = String.format(getResources().getString(R.string.formatted_currency), remaining);
        this.textRemaining.setText(formattedRemaining);
    }

    private void updateExpenseDetails() {
        this.expensesForMonth.clear();

        for (Expense expense : this.expensesList) {
            if (includeDetailedExpense(expense.getDate(), expense.getRecurring())
                    && isCategorySame(expense.getCategory())) {
                this.expensesForMonth.add(expense);
            }
        }

        this.expensesAdapter.setExpenses(this.expensesForMonth);
        this.expensesAdapter.notifyDataSetChanged();
    }

    private void updateMonetaryValues() {
        double budget = updateBudget();
        double expense = updateExpenses();
        updateRemaining(budget - expense);
        updateExpenseDetails();
    }

    /*
    Forcing synchronization here, based on:
    https://stackoverflow.com/questions/44548932/grab-data-from-firebase-with-java
     */
    private void retrieveData() {
        CountDownLatch latch = new CountDownLatch(2);

        String userId = this.username;

        this.firebaseHelper.getUserExpenses(userId, expensesList -> {
            this.expensesList.clear();
            this.expensesList.addAll(expensesList);
            latch.countDown();
        });

        this.firebaseHelper.getUserBudgets(userId, budgetList -> {
            this.budgetList.clear();
            this.budgetList.addAll(budgetList);
            latch.countDown();
        });

        new Thread(() -> {
            try {
                latch.await();
                if (isAdded()) {
                    getActivity().runOnUiThread(this::updateMonetaryValues);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
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

    private void setupMonthIterationButtons(View view) {
        Button btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth);
        Button btnNextMonth = view.findViewById(R.id.btnNextMonth);

        btnPreviousMonth.setOnClickListener(v -> {
            this.currentCalendar.add(Calendar.MONTH, -1);
            updateDateDisplay();
            retrieveData();
        });

        btnNextMonth.setOnClickListener(v -> {
            this.currentCalendar.add(Calendar.MONTH, 1);
            updateDateDisplay();
            retrieveData();
        });
    }

    private boolean includeDetailedExpense(long epochTime, boolean recurring) {
        Calendar epochCalendar = Calendar.getInstance();
        epochCalendar.setTimeInMillis(epochTime);

        int epochMonth = epochCalendar.get(Calendar.MONTH);
        int epochYear = epochCalendar.get(Calendar.YEAR);

        int currentMonth = this.currentCalendar.get(Calendar.MONTH);
        int currentYear = this.currentCalendar.get(Calendar.YEAR);

        if (recurring) {
            return epochYear < currentYear || (epochYear == currentYear && epochMonth <= currentMonth);
        } else {
            return epochMonth == currentMonth && epochYear == currentYear;
        }
    }

    @Override
    public void onExpenseDeleted() {
        updateMonetaryValues();
    }
}