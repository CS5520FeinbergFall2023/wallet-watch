package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Login
        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homepageActivity();
            }
        }));

    }

    public void homepageActivity(){

        Intent intent = new Intent(this, HomePageActivity.class);
        startActivity(intent);

    }
}