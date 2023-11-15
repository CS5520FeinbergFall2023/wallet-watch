package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class NotificationPageActivity extends AppCompatActivity {
    ArrayList<Notification> notificationList = new ArrayList<>();
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_page);

        TextView headerTitle = findViewById(R.id.headerTitle);
        headerTitle.setText(R.string.notification_history_string);

        FirebaseHelper helper = new FirebaseHelper();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("budgets")
                .child(user.getUsername());


    }

    private void addNotification() {

    }









}