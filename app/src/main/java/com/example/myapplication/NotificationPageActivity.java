package com.example.myapplication;

import static com.example.myapplication.MainActivity.PREFS_NAME;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.dao.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.example.myapplication.dao.Category;

public class NotificationPageActivity extends AppCompatActivity {
    RecyclerView notificationRV;
    NotificationManager notificationManager;

    private static final String CHANNEL_ID = "CHANNEL";
    private NotificationAdapter adapter;
    ArrayList<Notification> notificationList = new ArrayList<>();
    private User user;
    //will hold category: aggregated expense amount
    private HashMap<String, Long> dictExpense = new HashMap<>();
    private HashMap<String, Long> dictBudget = new HashMap<>();
    HashMap<String, Boolean> overBudgetTracker = new HashMap<>();
    private Calendar currentCalendar = Calendar.getInstance();
    private String username;
    FirebaseHelper firebaseHelper = new FirebaseHelper();
    //counter

    //MOVE TO HOMESCREEN
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_notification_page);
        notificationRV = (RecyclerView) findViewById(R.id.notifications_rv);
        // Get username from local storage
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        username = prefs.getString("username","");


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        notificationRV.setLayoutManager(layoutManager);

        ArrayList<Notification> receivednotifications = new ArrayList<>();

        adapter = new NotificationAdapter(this, receivednotifications);
        notificationRV.setAdapter(adapter);
        String notes = "notifications/" + username;

        DatabaseReference notificationsReference = FirebaseDatabase.getInstance().getReference(notes);

        notificationsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificationList.clear();
                ArrayList<Notification> newNotificationReceived = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("notice", snapshot.toString());
                    /*
                    for (DataSnapshot shot : snapshot.getChildren()) {
                        Log.d("notice of notificatoin", shot.getValue().toString());
                        if ("date".equalsIgnoreCase(shot.getKey())) {

                        }

                    }

                     */
                    //Notification notification = new Notification("Alert", snapshot.child("message").getValue().toString(), (Date)snapshot.child("date").getValue())
                    Notification notification = snapshot.getValue(Notification.class);
                    Log.d("notice of notification", notification.toString());
                    //notificationList.add(notification);
                    Log.d("notice of notification", notificationList.toString());
                    if (notification != null) {
                        newNotificationReceived.add(notification);
                        notificationList.add(notification);


                    }


                }
                adapter.setNotificationsReceived(notificationList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationPageActivity.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


        TextView headerTitle = findViewById(R.id.headerTitle);
        //may change string to Notification Center
        headerTitle.setText(R.string.notification_history_string);

        FirebaseHelper helper = new FirebaseHelper();
        DatabaseReference budgetDatabaseRef = FirebaseDatabase.getInstance().getReference()
                .child("budgets")
                .child(username);
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
                    //Log.d("notice of budget", snapshot.getValue().toString());
                    //Log.d("notice of budget", snapshot.child("amount").getValue().toString());
                    //Log.d("notice of budget", snapshot.child("category").getValue().toString());
                    //dictBudget.put(snapshot.child("category").getValue().toString(),
                    //(Long) snapshot.child("amount").getValue());


                    for (DataSnapshot shot : snapshot.getChildren()) {
                        //for (int i = 0; i < dataSnapshot.getChildren().size() {
                        //System.out.println(snapshot.getKey());
                        if (Objects.requireNonNull(shot.getKey()).equalsIgnoreCase("amount")) {
                            amount = (Long) shot.getValue();
                        }
                        if ("category".equalsIgnoreCase(shot.getKey()) && "Food".equalsIgnoreCase(shot.getValue().toString())) {
                            //System.out.println(shot.getValue());
                            foodAmount += amount;
                            dictBudget.put((String) shot.getValue(), foodAmount);
                            continue;
                        }
                        if ("category".equalsIgnoreCase(shot.getKey()) && "Entertainment".equalsIgnoreCase(shot.getValue().toString())) {
                            //System.out.println(shot.getValue());
                            entertainmentAmount += amount;
                            dictBudget.put((String) shot.getValue(), entertainmentAmount);
                            continue;
                        }
                        if ("category".equalsIgnoreCase(shot.getKey()) && "Travel".equalsIgnoreCase(shot.getValue().toString())) {
                            //System.out.println(shot.getValue());
                            travelAmount += amount;
                            dictBudget.put((String) shot.getValue(), travelAmount);
                            continue;
                        }
                        if ("category".equalsIgnoreCase(shot.getKey()) && "School".equalsIgnoreCase(shot.getValue().toString())) {
                            //System.out.println(shot.getValue());
                            schoolAmount += amount;
                            dictBudget.put((String) shot.getValue(), schoolAmount);
                            continue;
                        }
                        if ("category".equalsIgnoreCase(shot.getKey()) && "Utilities".equalsIgnoreCase(shot.getValue().toString())) {
                            //System.out.println(shot.getValue());
                            utilitiesAmount += amount;
                            //utilitiesAmount += (Long) snapshot.child("amount").getValue();

                            dictBudget.put((String) shot.getValue(), utilitiesAmount);
                            continue;
                        }


                    }
                }
                //Log.d("notice of dict Budget", dictBudget.toString());


                //System.out.println(dictBudget);


            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        expensesDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Log.d("notice of dict", dataSnapshot.toString());
                Long entertainmentAmount = 0L;
                Long foodAmount = 0L;
                Long utilitiesAmount = 0L;
                Long travelAmount = 0L;
                Long amount = 0L;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    /*
                    if ("Food".equalsIgnoreCase((String) snapshot.child("category").getValue())) {
                        foodAmount += (Long) snapshot.child("amount").getValue();
                        dictExpense.put(snapshot.child("category").getValue().toString(),
                                (Long) snapshot.child("amount").getValue());
                        continue;
                    }
                    if (snapshot.child("category").getValue() == "Entertainment") {
                        entertainmentAmount += (Long) snapshot.child("amount").getValue();
                        dictExpense.put(snapshot.child("category").getValue().toString(),
                                (Long) snapshot.child("amount").getValue());
                    }
                    if (snapshot.child("category").getValue() == "Utilities") {
                        utilitiesAmount += (Long) snapshot.child("amount").getValue();
                        dictExpense.put(snapshot.child("category").getValue().toString(),
                                (Long) snapshot.child("amount").getValue());
                    }

                    */
                    for (DataSnapshot shot : snapshot.getChildren()) {
                        //for (int i = 0; i < dataSnapshot.getChildren().size() {
                        //System.out.println(shot.getKey());
                        //Log.d("notice", shot.getKey().toString());
                        if ("amount".equalsIgnoreCase(shot.getKey())) {
                            //Log.d("amountKEY", shot.getValue().toString());
                            amount = (Long) shot.getValue();

                        }
                        /*
                        if (Objects.equals(shot.getKey(), "category")) {
                            System.out.println(shot.getValue());
                            dictExpense.put((String) shot.getValue(), amount);
                            continue;
                        }

                         */
                        if ("category".equalsIgnoreCase(shot.getKey()) && "Entertainment".equalsIgnoreCase(shot.getValue().toString())) {
                            //System.out.println(shot.getValue());
                            if (inCurrentMonth((Long) snapshot.child("date").getValue())) {
                                entertainmentAmount += amount;
                                //entertainmentAmount += (Long) snapshot.child("amount").getValue();
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


                    }

                }
                //System.out.println(dictExpense);
                //Log.d("notice of dict Expense", dictExpense.toString());

                try {
                    overBudget(dictBudget, dictExpense);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                }
                //Log.d("notice of overbudget", "GOT HERE");
                //Log.d("notice", overBudget(dictBudget, dictExpense));


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void addNotification() {


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
                    showNotification();

                    long currentTimestamp = System.currentTimeMillis();
                    String message = "You are over-budget for" + " " + entry.getKey() + " in" + " " + this.getMonthYearFromTimestamp(currentTimestamp);
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

                        }
                    }
                   //if (newNotification.getMonth(currentTimestamp) != )


                    //return true;
                }

                if (remaining == 0) {
                    Log.d("notice of overbudget", remaining.toString());
                    isOverBudget.put(entry.getKey(), true);
                    showNotification();

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

                        }
                    }
                    //if (newNotification.getMonth(currentTimestamp) != )


                    //return true;
                }

                if (remaining <= 0.20 * (entry.getValue()) && remaining > 0) {
                    Log.d("notice of overbudget", remaining.toString());
                    isOverBudget.put(entry.getKey(), true);
                    showNotification();

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

                        }
                    }
                    //if (newNotification.getMonth(currentTimestamp) != )


                    //return true;
                }


                else {
                    isOverBudget.put(entry.getKey(), false);

                }
            }

        }
        Log.d("notice of overbudget", dictBudget.toString());
        Log.d("notice of overbudget", dictExpense.toString());
        return isOverBudget;
    }

    public boolean isOverBudget(Long budget, Long expense) throws IllegalAccessException, InstantiationException {
        Long remaining = budget - expense;
        if (remaining < 0) {
            showNotification();


            return true;
        }
        return false;
    }

    public void showNotification() throws IllegalAccessException, InstantiationException {
        Intent intent = new Intent (getApplicationContext(), DataVisualizationFragment.class);
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
                .setContentText("You are overbudget")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);

        // Show the notification

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(NotificationPageActivity.this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1, builder.build());
        //managerCompat.notify(1, builder.build());
        if (builder == null) {
            Log.d("isNULLBUILDER", "isNULLBUILDER ");
        }

    }



    //for notifications, need to keep track of month, budget limit,

    private boolean inCurrentMonth(long epochTime) {
        Calendar epochCalendar = Calendar.getInstance();
        epochCalendar.setTimeInMillis(epochTime);

        int epochMonth = epochCalendar.get(Calendar.MONTH);
        int epochYear = epochCalendar.get(Calendar.YEAR);

        int currentMonth = this.currentCalendar.get(Calendar.MONTH);
        int currentYear = this.currentCalendar.get(Calendar.YEAR);

        return epochMonth == currentMonth && epochYear == currentYear;
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









}