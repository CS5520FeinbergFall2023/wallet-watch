package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

public class NotificationPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_page);

        TextView headerTitle = findViewById(R.id.headerTitle);
        headerTitle.setText(R.string.notification_history_string);

        FirebaseHelper helper = new FirebaseHelper();







    }









}