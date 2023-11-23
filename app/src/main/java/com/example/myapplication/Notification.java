package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Notification {

    private static final String CHANNEL_ID = "CHANNEL";
    private String type; //category
    private String message;
    private String date;
    private double budgetAmount;
    private NotificationType notificationType;


    public Notification(String type, String message, String date, double budgetAmount) {
        this.type = type;
        this.message = message;
        this.date = date;
        this.budgetAmount = budgetAmount;
        this.notificationType = null;
    }

    public Notification() {

    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }
    public double getBudgetAmount() {return budgetAmount;}

    // Add getters and setters
    public void setType(String type) {
        this.type = type;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setNotificationType(NotificationType type) {
        this.notificationType = type;
    }

    public NotificationType getNotificationType() {
        return this.notificationType;
    }

    public String formatDate(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        return dateFormat.format(date);
    }

    public int getMonth(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        // Get the month using the Calendar instance
        return calendar.get(Calendar.MONTH);
    }



    public void setBudgetAmount(double budgetAmount) {this.budgetAmount = budgetAmount;}



    public static void showNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                //.setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Show the notification
        notificationManager.notify(1, builder.build());
    }









}
