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

                if (previousFragmentTag != null && previousFragmentTag.equals("expense") && !fragmentTag.equals("expense") && !((ExpensePageFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentByTag(previousFragmentTag))).isFormClear()) {
                    // alert for when navigating away from the Expense Page
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

    private HashMap<String, Boolean> overBudget(HashMap<String, Long> dictBudget, HashMap<String, Long> dictExpense) throws IllegalAccessException, InstantiationException {
        HashMap<String, Boolean> isOverBudget = new HashMap<>();
        if (dictBudget.isEmpty() || dictExpense.isEmpty()) {
            Log.d("notice of overbudget", "isEMPTY: ");
            return isOverBudget;
        }
        Long remaining = 0L;
        for (Map.Entry<String, Long> entry : dictBudget.entrySet()) {
            if (dictExpense.get(entry.getKey()) != null) {
                remaining = entry.getValue() - dictExpense.get(entry.getKey());


                if (remaining < 0) {
                    Log.d("notice of overbudget", remaining.toString());
                    isOverBudget.put(entry.getKey(), true);
                    //OLD LOCATION OF showNotification()


                    long currentTimestamp = System.currentTimeMillis();
                    String message = "You are over-budget for" + " " + entry.getKey() + " in" + " " + this.getMonthYearFromTimestamp(currentTimestamp);
                    //showNotification(message);
                    Notification newNotification = new Notification(entry.getKey(), message, String.valueOf(currentTimestamp), entry.getValue());
                    NotificationType notificationType = NotificationType.OVER_BUDGET;
                    newNotification.setNotificationType(notificationType);
                    for (Notification notification : notificationList) {
                        String stringDate = notification.getDate();
                        Long longDate = Long.valueOf(stringDate);
                        if (newNotification.getType() != notification.getType() && newNotification.getMonth(currentTimestamp)
                                != notification.getMonth((longDate)) && notification.getBudgetAmount()
                                != newNotification.getBudgetAmount() && newNotification.getNotificationType() != notification.getNotificationType()) {
                            firebaseHelper.createNotification(username, newNotification);
                            //showNotification();
                            Log.d("FIREBASEHELPER MAIN CALLED", "fire is called");
                            showNotification(message); //IF ONLY CALLED HERE WILL ALERT USER ONLY ONCE

                        }
                    }

                }

                if (remaining == 0) {
                    Log.d("notice of overbudget", remaining.toString());
                    isOverBudget.put(entry.getKey(), true);
                    //OLD LOCATION OF showNotification()


                    long currentTimestamp = System.currentTimeMillis();
                    String message = "You have reached your budget limit for" + " " + entry.getKey() + " in" + " " + this.getMonthYearFromTimestamp(currentTimestamp);
                    Notification newNotification = new Notification(entry.getKey(), message, String.valueOf(currentTimestamp), entry.getValue());
                    NotificationType notificationType = NotificationType.AT_LIMIT;
                    newNotification.setNotificationType(notificationType);
                    for (Notification notification : notificationList) {
                        String stringDate = notification.getDate();
                        Long longDate = Long.valueOf(stringDate);
                        if (newNotification.getType() != notification.getType() && newNotification.getMonth(currentTimestamp)
                                != notification.getMonth((longDate)) && notification.getBudgetAmount()
                                != newNotification.getBudgetAmount() && newNotification.getNotificationType() != notification.getNotificationType()) {
                            firebaseHelper.createNotification(username, newNotification);
                            showNotification(message);
                            Log.d("FIREBASEHELPER MAIN CALLED LIMIT", "fire is called");

                        }
                    }

                }

                if (remaining <= 0.20 * (entry.getValue()) && remaining > 0) {
                    Log.d("notice of overbudget", remaining.toString());
                    isOverBudget.put(entry.getKey(), true);
                    //OLD LOCATION OF showNotification()

                    long currentTimestamp = System.currentTimeMillis();
                    String message = "You are at most 20% away from reaching your budget for " + " " + entry.getKey() + " in" + " " + this.getMonthYearFromTimestamp(currentTimestamp);
                    Notification newNotification = new Notification(entry.getKey(), message, String.valueOf(currentTimestamp), entry.getValue());
                    NotificationType notificationType = NotificationType.LIMIT_APPROACHING;
                    newNotification.setNotificationType(notificationType);
                    for (Notification notification : notificationList) {
                        String stringDate = notification.getDate();
                        Long longDate = Long.valueOf(stringDate);
                        if (newNotification.getType() != notification.getType() && newNotification.getMonth(currentTimestamp)
                                != notification.getMonth((longDate)) && notification.getBudgetAmount()
                                != newNotification.getBudgetAmount() && newNotification.getNotificationType()
                                != notification.getNotificationType()) {
                            firebaseHelper.createNotification(username, newNotification);
                            showNotification(message);
                            Log.d("FIREBASEHELPER MAIN CALLED 20", "fire is called");

                        }
                    }

                } else {
                    isOverBudget.put(entry.getKey(), false);

                }
            }

        }
        Log.d("notice of overbudget", dictBudget.toString());
        Log.d("notice of overbudget", dictExpense.toString());
        return isOverBudget;
    }

    public void showNotification(String message) throws IllegalAccessException, InstantiationException {
        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;
        Intent intent = new Intent(getApplicationContext(), DataVisualizationFragment.class);
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
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Budget Alert")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);

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

    public String getMonthYearFromTimestamp(long timestamp) {
        // Create a Calendar instance and set the time based on the provided timestamp
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        // Get the month and year using the Calendar instance
        int monthNumber = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        // Convert the month number to a month name (using Locale for proper localization)
        String monthName = new SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.getTime());

        // Concatenate month and year and return as a string
        return monthName + " " + year;
    }

    public void nestFunction() {
        //FirebaseHelper helper = new FirebaseHelper();
        DatabaseReference budgetDatabaseRef = FirebaseDatabase.getInstance().getReference()
                .child("budgets")
                .child(username);
        //dictBudget = new HashMap<>();

        DatabaseReference expensesDatabaseRef = FirebaseDatabase.getInstance().getReference()
                .child("expenses")
                //.child(user.getUsername)
                .child(username);


        budgetDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long amount = 0L;
                Long utilitiesAmount = 0L;
                Long foodAmount = 0L;
                Long entertainmentAmount = 0L;
                Long schoolAmount = 0L;
                Long travelAmount = 0L;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot shot : snapshot.getChildren()) {
                        if (Objects.requireNonNull(shot.getKey()).equalsIgnoreCase("amount")) {
                            amount = (Long) shot.getValue();
                        }
                        if ("category".equalsIgnoreCase(shot.getKey()) && "Food".equalsIgnoreCase(shot.getValue().toString())) {

                            foodAmount += amount;
                            dictBudget.put((String) shot.getValue(), foodAmount);
                            continue;
                        }
                        if ("category".equalsIgnoreCase(shot.getKey()) && "Entertainment".equalsIgnoreCase(shot.getValue().toString())) {
                            entertainmentAmount += amount;
                            Log.d("MAINBUDGETBUDGET", shot.getValue().toString());
                            dictBudget.put((String) shot.getValue(), entertainmentAmount);
                            Log.d("MAINBUDGETBUDGET", dictBudget.toString());
                            continue;
                        }
                        if ("category".equalsIgnoreCase(shot.getKey()) && "Travel".equalsIgnoreCase(shot.getValue().toString())) {
                            travelAmount += amount;
                            dictBudget.put((String) shot.getValue(), travelAmount);
                            continue;
                        }
                        if ("category".equalsIgnoreCase(shot.getKey()) && "School".equalsIgnoreCase(shot.getValue().toString())) {
                            schoolAmount += amount;
                            dictBudget.put((String) shot.getValue(), schoolAmount);
                            continue;
                        }
                        if ("category".equalsIgnoreCase(shot.getKey()) && "Utilities".equalsIgnoreCase(shot.getValue().toString())) {
                            utilitiesAmount += amount;
                            dictBudget.put((String) shot.getValue(), utilitiesAmount);
                            continue;
                        }


                    }
                }
                Log.d("notice of dict Budget", dictBudget.toString());
                copiedDictBudget.putAll(dictBudget);
                //Log.d("noice of dict Budget COPIED", copiedDictBudget.toString());


                //System.out.println(dictBudget);
                //START EXPENSE HERE
                Log.d("MAINBUDGETBUDGETBUDGET", dictBudget.toString());
                //use thread
                expensesDatabaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //Log.d("notice of dict", dataSnapshot.toString());
                        Long entertainmentAmount = 0L;
                        Long foodAmount = 0L;
                        Long utilitiesAmount = 0L;
                        Long travelAmount = 0L;
                        Long schoolAmount = 0L;
                        Long amount = 0L;

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            for (DataSnapshot shot : snapshot.getChildren()) {

                                if ("amount".equalsIgnoreCase(shot.getKey())) {
                                    //Log.d("amountKEY", shot.getValue().toString());
                                    amount = (Long) shot.getValue();

                                }

                                if ("category".equalsIgnoreCase(shot.getKey()) && "Entertainment".equalsIgnoreCase(shot.getValue().toString())) {
                                    //System.out.println(shot.getValue());
                                    if (inCurrentMonth((Long) snapshot.child("date").getValue())) {
                                        entertainmentAmount += amount;
                                        //entertainmentAmount += (Long) snapshot.child("amount").getValue();
                                        Log.d("MAINBUDGET", shot.getValue().toString());
                                        dictExpense.put((String) shot.getValue(), entertainmentAmount);
                                        Log.d("notice of time", "true: ");


                                    }
                                    continue;

                                }
                                if ("category".equalsIgnoreCase(shot.getKey()) && "Food".equalsIgnoreCase(shot.getValue().toString())) {
                                    if (inCurrentMonth((Long) snapshot.child("date").getValue())) {
                                        //System.out.println(shot.getValue());
                                        foodAmount += amount;
                                        //foodAmount += (Long) snapshot.child("amount").getValue();
                                        dictExpense.put((String) shot.getValue(), foodAmount);
                                        //isOverBudget(dictBudget.get(shot.getKey()), foodAmount);

                                        continue;

                                    }

                                }
                                if ("category".equalsIgnoreCase(shot.getKey()) && "Travel".equalsIgnoreCase(shot.getValue().toString())) {
                                    if (inCurrentMonth((Long) snapshot.child("date").getValue())) {
                                        //System.out.println(shot.getValue());
                                        travelAmount += amount;
                                        //foodAmount += (Long) snapshot.child("amount").getValue();
                                        dictExpense.put((String) shot.getValue(), amount);
                                        //isOverBudget(dictBudget.get(shot.getKey()), travelAmount);
                                        continue;
                                    }


                                }
                                if ("category".equalsIgnoreCase(shot.getKey()) && "Utilities".equalsIgnoreCase(shot.getValue().toString())) {
                                    if (inCurrentMonth((Long) snapshot.child("date").getValue())) {
                                        //System.out.println(shot.getValue());
                                        utilitiesAmount += amount;
                                        //utilitiesAmount += (Long) snapshot.child("amount").getValue();
                                        dictExpense.put((String) shot.getValue(), utilitiesAmount);
                                        //isOverBudget(dictBudget.get(shot.getKey()), utilitiesAmount);

                                    }
                                }
                                if ("category".equalsIgnoreCase(shot.getKey()) && "School".equalsIgnoreCase(shot.getValue().toString())) {
                                    if (inCurrentMonth((Long) snapshot.child("date").getValue())) {
                                        //System.out.println(shot.getValue());
                                        schoolAmount += amount;
                                        //utilitiesAmount += (Long) snapshot.child("amount").getValue();
                                        dictExpense.put((String) shot.getValue(), utilitiesAmount);
                                        //isOverBudget(dictBudget.get(shot.getKey()), utilitiesAmount);

                                    }
                                }


                            }

                        }


                        try {
                            //TROUBLESHOOT dictBudget is EMPTY, DON"T KNOW WHY
                            overBudget(dictBudget, dictExpense);
                            //overBudget(copiedDictBudget, dictExpense);
                            Log.d("MAINBUDGETCOPIED", dictBudget.toString());
                            Log.d("MAINBUDGET", dictExpense.toString());
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (InstantiationException e) {
                            throw new RuntimeException(e);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                copiedDictBudget.putAll(dictBudget);
            }
        });

    }
}
