package com.example.myapplication;

import static android.content.Context.MODE_PRIVATE;
import static com.example.myapplication.MainActivity.PREFS_NAME;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.dao.Budget;
import com.example.myapplication.dao.Category;
import com.example.myapplication.dao.Expense;
import com.example.myapplication.dao.ExpenseItem;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class DataVisualizationFragment extends Fragment {

    private TextView textDate, textBudget, textRemaining, textExpenses;
    private ViewPager2 categoryViewPager;
    private RecyclerView expensesRecyclerView;
    private Calendar currentCalendar = Calendar.getInstance();
    private FirebaseHelper firebaseHelper;
    private List<Expense> expensesList;
    private List<ExpenseItem> expensesForMonth;
    private List<Budget> budgetList;
    private List<Category> categoryList;
    private ExpensesAdapter expensesAdapter;
    private String username;
    private CategoriesViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_visualization, container, false);

        TextView headerTitle = view.findViewById(R.id.headerTitle);
        headerTitle.setText(getText(R.string.data_viz_header));

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
        this.expensesAdapter = new ExpensesAdapter(new ArrayList<>());
        this.expensesRecyclerView.setAdapter(this.expensesAdapter);

        this.viewModel.getCategoryList().observe(getViewLifecycleOwner(), categories -> {
            this.categoryList = categories;
            addCategoriesToPager();
        });

        retrieveData();
        setupMonthIterationButtons(view);
        updateDateDisplay();

        return view;
    }

    private void addCategoriesToPager() {
        this.categoryViewPager.setAdapter(new CategoryAdapter(this.categoryList));

        this.categoryViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                retrieveData();
            }
        });
    }

    private boolean isCategorySame(Object obj) {
        try {
            String category = this.categoryList.get(this.categoryViewPager.getCurrentItem()).getCategory();
            Method getCategoryMethod = obj.getClass().getMethod("getCategory");
            return Objects.equals(getCategoryMethod.invoke(obj), category);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return false;
    }

    private double updateBudget() {
        double budget = 0;

        for (Budget categoryBudget : this.budgetList) {
            if (isCategorySame(categoryBudget)) {
                budget = categoryBudget.getAmount();
                String formattedBudget = String.format(getResources().getString(R.string.formatted_currency), budget);
                this.textBudget.setText(formattedBudget);
                break;
            }
        }

        return budget;
    }

    private double updateExpenses() {
        double totalExpenses = 0;

        for (Expense expense : this.expensesList) {
            if (inCurrentMonth(expense.getDate()) && isCategorySame(expense)) {
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
            if (inCurrentMonth(expense.getDate()) && isCategorySame(expense)) {
                this.expensesForMonth.add(new ExpenseItem(expense.getDescription(), expense.getAmount()));
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

    private boolean inCurrentMonth(long epochTime) {
        Calendar epochCalendar = Calendar.getInstance();
        epochCalendar.setTimeInMillis(epochTime);

        int epochMonth = epochCalendar.get(Calendar.MONTH);
        int epochYear = epochCalendar.get(Calendar.YEAR);

        int currentMonth = this.currentCalendar.get(Calendar.MONTH);
        int currentYear = this.currentCalendar.get(Calendar.YEAR);

        return epochMonth == currentMonth && epochYear == currentYear;
    }
}