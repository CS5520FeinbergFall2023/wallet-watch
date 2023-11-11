package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomePageActivity extends AppCompatActivity {

    private Button budgetButton, expenseButton, dataButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // Budget
        budgetButton = (Button) findViewById(R.id.budgetButton);
        budgetButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                budgetActivity();
            }
        }));


        // Expense
        expenseButton = (Button) findViewById(R.id.expenseButton);
        expenseButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expenseActivity();
            }
        }));


        // Data Visualization
        dataButton = (Button) findViewById(R.id.dataButton);
        dataButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataActivity();
            }
        }));


    }

    public void budgetActivity(){

        Intent intent = new Intent(this, budgetPageActivity.class);
        startActivity(intent);
    }

    public void expenseActivity(){

        Intent intent = new Intent(this, expensePageActivity.class);
        startActivity(intent);
    }

    public void dataActivity(){

        Intent intent = new Intent(this, dataPageActivity.class);
        startActivity(intent);
    }

}