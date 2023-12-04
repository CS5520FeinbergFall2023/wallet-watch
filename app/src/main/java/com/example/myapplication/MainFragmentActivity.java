package com.example.myapplication;

import static com.example.myapplication.MainActivity.PREFS_NAME;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;


public class MainFragmentActivity extends AppCompatActivity {

    private final String CURRENT_FRAGMENT = "currFrag";
    // Home activity is the default activity
    private int selectedFragment = R.id.home;
    private BottomNavigationView bottomNavigationView;
    private FirebaseHelper firebaseHelper;
    private CategoriesViewModel categoriesViewModel;


    public static final int NOTIFICATION_PERMISSION_CODE = 5520;

    public static final String CHANNEL_ID = "CHANNEL";
    public NotificationAdapter adapter;
    ArrayList<Notification> notificationList = new ArrayList<>();
    //public User user;
    //will hold category: aggregated expense amount
    public HashMap<String, Long> dictExpense = new HashMap<>();
    public HashMap<String, Long> dictBudget = new HashMap<>();
    public HashMap<String, Boolean> overBudget = new HashMap<>();
    //HashMap<String, Boolean> overBudgetTracker = new HashMap<>();
    public Calendar currentCalendar = Calendar.getInstance();
    public String username;
    //FirebaseHelper firebaseHelper = new FirebaseHelper();
    HashMap<String, Long> copiedDictBudget = new HashMap<>();

    String fragmentTag;
    String previousFragmentTag;


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

        //NEW NOTIFICATION
        // Get username from local storage
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        username = prefs.getString("username", "");
        //readNotifications();
        nestFunction();
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
            Fragment selectedFragment;
            int itemId = item.getItemId();
            previousFragmentTag = fragmentTag;
            fragmentTag = "";

            // This does not work as a switch... don't waste your time
            if (itemId == R.id.home) {
                selectedFragment = new HomePageFragment();
                fragmentTag = "home";
            } else if (itemId == R.id.budget) {
                selectedFragment = new BudgetFragment();
                fragmentTag = "budget";
            } else if (itemId == R.id.expense) {
                fragmentTag = "expense";

                Fragment existingFragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);

                if (existingFragment == null) {
                    selectedFragment = new ExpensePageFragment();
                } else {
                    selectedFragment = existingFragment;
                }
            } else if (itemId == R.id.data) {
                fragmentTag = "data";

                Fragment existingFragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);

                if (existingFragment == null) {
                    selectedFragment = new DataVisualizationFragment();
                } else {
                    selectedFragment = existingFragment;
                }
            } else if (itemId == R.id.account) {
                selectedFragment = new AccountPageFragment();
                fragmentTag = "account";
            } else {
                selectedFragment = null;
            }

            if (selectedFragment != null) {
                this.selectedFragment = itemId;

                if (previousFragmentTag != null && previousFragmentTag.equals("expense") && !fragmentTag.equals("expense")) {
                    // Alert for when navigating away from the Expense Page
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainFragmentActivity.this);
                    builder.setMessage("Are you sure you want to Navigate away?\nYou will lose all saved values.");

                    builder.setPositiveButton("Exit", (dialog, which) -> {
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment, fragmentTag)
                                .commit();
                    });
                    builder.setNegativeButton("Dismiss", (dialog, which) -> {
                        dialog.dismiss();
                        bottomNavigationView.setSelectedItemId(R.id.expense);
                    });

                    builder.show();
                } else {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment, fragmentTag)
                            .commit();
                }
            }
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_top_toolbar, menu);
        return true;
    }


    public void onClickNotification(View view) {
        int clickedId = view.getId();
        if (clickedId == R.id.button_notifications_center) {
            startActivity(new Intent(MainFragmentActivity.this, NotificationPageActivity.class));
        }
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

    private void overBudget() {
        this.overBudget.clear();

        for (Map.Entry<String, Long> entry : this.dictBudget.entrySet()) {
            Log.v("Entry", String.valueOf(entry));

            Long remaining = entry.getValue() - this.dictExpense.getOrDefault(entry.getKey(), 0L);
            String category = entry.getKey();
            Long budgetAmount = entry.getValue();

            if (remaining < 0) {
                handleNotification(
                        category,
                        budgetAmount,
                        NotificationType.OVER_BUDGET,
                        "You are over-budget for: " + category);
                overBudget.put(category, true);
            } else if (remaining == 0) {
                handleNotification(
                        category,
                        budgetAmount,
                        NotificationType.AT_LIMIT,
                        "Budget limit reached for: " + category);
                overBudget.put(category, true);
            } else if (remaining <= 0.20 * budgetAmount) {
                handleNotification(
                        category,
                        budgetAmount,
                        NotificationType.LIMIT_APPROACHING,
                        "Nearing budget limit for: " + category);
                overBudget.put(category, true);
            } else {
                handleNotification(
                        category,
                        budgetAmount,
                        NotificationType.REMOVE,
                        "N/A");
                overBudget.put(category, false);
            }
        }

        Log.v("Overbudget", String.valueOf(this.overBudget));
    }

    private void handleNotification(String category, Long budgetAmount, NotificationType type, String message) {
        Notification newNotification = new Notification(category, message, String.valueOf(System.currentTimeMillis()), budgetAmount);
        newNotification.setNotificationType(type);

        if (newNotification.getNotificationType() != NotificationType.REMOVE) {
            firebaseHelper.createNotification(username, newNotification);
            try {
                showNotification(newNotification.getMessage());
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        } else {
            // Delete said notification if it exists
            Log.d("REMOVE-handleNotification", "handleNotification: ");
            firebaseHelper.removeNotification(username, newNotification);




        }
    }

    public void showNotification(String message) throws IllegalAccessException, InstantiationException {
        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;
        Intent intent = new Intent(getApplicationContext(), NotificationPageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        //NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)


        //NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // Create a notification channel (required for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Budget App Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                //.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setColor(Color.GREEN)
                .setContentTitle("Budget Alert")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        // Show the notification

        //NotificationManagerCompat managerCompat = NotificationManagerCompat.from(NotificationPageActivity.this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS});
            // here to request the missing permissions, and then overriding
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(m, builder.build());
        //managerCompat.notify(1, builder.build());
        if (builder == null) {
            Log.d("isNULLBUILDER", "isNULLBUILDER ");
        }

    }

//    public String getMonthYearFromTimestamp(long timestamp) {
//        // Create a Calendar instance and set the time based on the provided timestamp
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(timestamp);
//
//        // Get the month and year using the Calendar instance
//        int monthNumber = calendar.get(Calendar.MONTH);
//        int year = calendar.get(Calendar.YEAR);
//
//        // Convert the month number to a month name (using Locale for proper localization)
//        String monthName = new SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.getTime());
//
//        // Concatenate month and year and return as a string
//        return monthName + " " + year;
//    }

//    public void readNotifications() {
//        String notes = "notifications/" + username;
//
//        DatabaseReference notificationsReference = FirebaseDatabase.getInstance().getReference(notes);
//
//        notificationsReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                notificationList.clear();
//                ArrayList<Notification> newNotificationReceived = new ArrayList<>();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    Log.d("notice", snapshot.toString());
//
//                    Notification notification = snapshot.getValue(Notification.class);
//                    Log.d("notice of notification", notification.toString());
//
//
//                    if (notification != null) {
//                        newNotificationReceived.add(notification);
//                        notificationList.add(notification);
//                        Log.d("notice of notification2", notificationList.toString());
//
//
//                    }
//
//
//                }
//                //adapter.setNotificationsReceived(notificationList);
//                //adapter.notifyDataSetChanged();
//                try {
//                    overBudget(dictBudget, dictExpense);
//                } catch (IllegalAccessException e) {
//                    throw new RuntimeException(e);
//                } catch (InstantiationException e) {
//                    throw new RuntimeException(e);
//                }
//                Log.d("NOTIFICATIONLISTADAPTER", notificationList.toString());
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(MainFragmentActivity.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//
//            }
//        });
//    }

    public void nestFunction() {
        DatabaseReference budgetDatabaseRef = getDatabaseReference("budgets");
        DatabaseReference expensesDatabaseRef = getDatabaseReference("expenses");

        budgetDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                processBudgetData(dataSnapshot);
                Log.v("Budget", "Budget");
                overBudget();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        expensesDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                processExpenseData(dataSnapshot);
                Log.v("Expense", "Expense");
                overBudget();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private DatabaseReference getDatabaseReference(String childName) {
        return FirebaseDatabase.getInstance().getReference()
                .child(childName)
                .child(username);
    }

    private void processBudgetData(DataSnapshot dataSnapshot) {
        this.dictBudget.clear();

        // Iterate for each category
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            String category = snapshot.child("category").getValue(String.class);
            Long amount = snapshot.child("amount").getValue(Long.class);

            if (category != null && amount != null) {
                Long updatedAmount = this.dictBudget.getOrDefault(category, 0L) + amount;
                this.dictBudget.put(category, updatedAmount);
            }
        }
        Log.d("Dict Budget:", this.dictBudget.toString());
    }

    private void processExpenseData(DataSnapshot dataSnapshot) {
        this.dictExpense.clear();

        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            String category = snapshot.child("category").getValue(String.class);
            Long amount = snapshot.child("amount").getValue(Long.class);

            if (category != null && amount != null && inCurrentMonth((Long) snapshot.child("date").getValue())) {
                Long currentTotal = this.dictExpense.getOrDefault(category, 0L);
                this.dictExpense.put(category, currentTotal + amount);
            }
        }

        Log.d("Expense Budget:", this.dictExpense.toString());
    }
}
