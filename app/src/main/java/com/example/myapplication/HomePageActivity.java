package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class HomePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Button dataButton = findViewById(R.id.dataButton);
            dataButton.setOnClickListener(view -> {
            Intent intent = new Intent(HomePageActivity.this, DataVisualizationActivity.class);
            startActivity(intent);
        });
    }


}