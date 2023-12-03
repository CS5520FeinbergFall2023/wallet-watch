package com.example.myapplication;

import static com.example.myapplication.MainActivity.PREFS_NAME;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
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
import java.util.Random;

import com.example.myapplication.dao.Category;

public class NotificationPageActivity extends AppCompatActivity {
    RecyclerView notificationRV;
    NotificationManager notificationManager;

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

                    Notification notification = snapshot.getValue(Notification.class);
                    Log.d("notice of notification", notification.toString());

                    Log.d("notice of notification", notificationList.toString());
                    if (notification != null) {
                        newNotificationReceived.add(notification);
                        notificationList.add(notification);


                    }


                }
                adapter.setNotificationsReceived(notificationList);
                adapter.notifyDataSetChanged();
                Log.d("NOTIFICATIONLISTADAPTER", notificationList.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationPageActivity.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });




        TextView headerTitle = findViewById(R.id.headerTitle);
        //may change string to Notification Center
        headerTitle.setText(R.string.notification_history_string);
        //nestFunction();
        /*
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
                    for (DataSnapshot shot : snapshot.getChildren()) {
                        if (Objects.requireNonNull(shot.getKey()).equalsIgnoreCase("amount")) {
                            amount = (Long) shot.getValue();
                        }
                        if ("category".equalsIgnoreCase(shot.getKey()) && "Food".equalsIgnoreCase(shot.getValue().toString())) {

                            foodAmount += amount;
                            dictBudget.put((String) shot.getValue(), foodAmount);
                        }
                        else if ("category".equalsIgnoreCase(shot.getKey()) && "Entertainment".equalsIgnoreCase(shot.getValue().toString())) {
                            entertainmentAmount += amount;
                            dictBudget.put((String) shot.getValue(), entertainmentAmount);
                        }
                        else if ("category".equalsIgnoreCase(shot.getKey()) && "Travel".equalsIgnoreCase(shot.getValue().toString())) {
                            travelAmount += amount;
                            dictBudget.put((String) shot.getValue(), travelAmount);
                        }
                        else if ("category".equalsIgnoreCase(shot.getKey()) && "School".equalsIgnoreCase(shot.getValue().toString())) {
                            schoolAmount += amount;
                            dictBudget.put((String) shot.getValue(), schoolAmount);
                        }
                        else if ("category".equalsIgnoreCase(shot.getKey()) && "Utilities".equalsIgnoreCase(shot.getValue().toString())) {
                            utilitiesAmount += amount;
                            dictBudget.put((String) shot.getValue(), utilitiesAmount);
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
                    overBudget(dictBudget, dictExpense);
                    Log.d("NOTIFICATIONBUDGET", dictBudget.toString());
                    Log.d("NOTIFICATIONEXPENSE", dictExpense.toString());
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
        */


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
                    //notificationList.add(newNotification);
                    Log.d("NOTIFICATIONLIST", notificationList.toString());



                    NotificationType notificationType = NotificationType.OVER_BUDGET;
                    newNotification.setNotificationType(notificationType);
                    /*
                    if (!notificationList.contains(newNotification)) {
                        firebaseHelper.createNotification(username, newNotification);
                        notificationList.add(newNotification);

                    }
                    */

                    for (Notification notification : notificationList) {
                        String stringDate = notification.getDate();
                        Long longDate = Long.valueOf(stringDate);
                        if (newNotification.getType() != notification.getType() && newNotification.getMonth(currentTimestamp)
                                != notification.getMonth((longDate)) && notification.getBudgetAmount()
                                != newNotification.getBudgetAmount() && newNotification.getNotificationType() != notification.getNotificationType()) {
                            firebaseHelper.createNotification(username, newNotification);
                            Log.d("FIREBASEHELPER NOTIFICATION CALLED", "fire is called");

                            //notificationList.add(newNotification);
                            //showNotification();
                            //showNotification(message); //IF ONLY CALLED HERE WILL ALERT USER ONLY ONCE

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
                    notificationList.add(newNotification);

                    for (Notification notification : notificationList) {
                        String stringDate = notification.getDate();
                        Long longDate = Long.valueOf(stringDate);
                        if (newNotification.getType() != notification.getType() && newNotification.getMonth(currentTimestamp)
                                != notification.getMonth((longDate)) && notification.getBudgetAmount()
                                != newNotification.getBudgetAmount() && newNotification.getNotificationType() != notification.getNotificationType()) {
                            firebaseHelper.createNotification(username, newNotification);
                            Log.d("FIREBASEHELPER NOTIFICATION CALLED LIMIT", "fire is called");
                            //notificationList.add(newNotification);
                            //showNotification(message);

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
                    notificationList.add(newNotification);
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
                            Log.d("FIREBASEHELPER NOTIFICATION CALLED 20", "fire is called");

                            //showNotification(message);

                        }
                    }

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



    public void showNotification(String message) throws IllegalAccessException, InstantiationException {
        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;
        //CHANNEL_ID = m;

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
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);

        // Show the notification

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(NotificationPageActivity.this);
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

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Notification permission granted
                // You can perform actions related to notifications here
            } else {
                Toast.makeText(this, "Notification permission is required to receive updates.", Toast.LENGTH_SHORT).show();
                // Notification permission denied
                // Handle if permission is denied, possibly show a message
            }
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

        String notes = "notifications/" + username;

        DatabaseReference notificationsReference = FirebaseDatabase.getInstance().getReference(notes);

        notificationsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificationList.clear();
                ArrayList<Notification> newNotificationReceived = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("notice", snapshot.toString());

                    Notification notification = snapshot.getValue(Notification.class);
                    Log.d("notice of notification", notification.toString());

                    Log.d("notice of notification", notificationList.toString());
                    if (notification != null) {
                        newNotificationReceived.add(notification);
                        notificationList.add(notification);


                    }


                }
                adapter.setNotificationsReceived(notificationList);
                adapter.notifyDataSetChanged();
                Log.d("NOTIFICATIONLISTADAPTER", notificationList.toString());

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
                    //copiedDictBudget.putAll(dictBudget);
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
                    //copiedDictBudget.putAll(dictBudget);
                }
            });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationPageActivity.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }











}