package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import com.example.myapplication.dao.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

public class NotificationPageActivity extends AppCompatActivity {
    ArrayList<Notification> notificationList = new ArrayList<>();
    private User user;
    //will hold category: aggregated expense amount
    private Dictionary<String, Integer> dictExpense = new Hashtable<>();
    private Dictionary<String, Integer> dictBudget = new Hashtable<>();

    //MOVE TO HOMESCREEN
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_page);

        TextView headerTitle = findViewById(R.id.headerTitle);
        //may change string to Notification Center
        headerTitle.setText(R.string.notification_history_string);

        FirebaseHelper helper = new FirebaseHelper();
        DatabaseReference budgetDatabaseRef = FirebaseDatabase.getInstance().getReference()
                .child("budgets")
                .child("kartik");
        DatabaseReference expensesDatabaseRef = FirebaseDatabase.getInstance().getReference()
                .child("expenses")
                //.child(user.getUsername)
                .child("kartik");

        budgetDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot shot : snapshot.getChildren()) {
                        //for (int i = 0; i < dataSnapshot.getChildren().size() {
                        System.out.println(snapshot.getKey());
                        if (snapshot.getValue() == "Food") {
                            System.out.println(snapshot.getValue());


                            //dictBudget.put("Food", dataSnapshot.getValue());
                        }
                    }

                    for (DataSnapshot shot : snapshot.getChildren()) {
                        if ("category".equalsIgnoreCase(shot.getKey())) {



                            //check budget/username/check if value is less than or equal to expenses/user/max value
                            //if less than
                            //homepage

                        }
                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }

    private void addNotification() {


    }









}