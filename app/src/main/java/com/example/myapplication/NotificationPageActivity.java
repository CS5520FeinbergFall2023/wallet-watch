package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Objects;

public class NotificationPageActivity extends AppCompatActivity {
    ArrayList<Notification> notificationList = new ArrayList<>();
    private User user;
    //will hold category: aggregated expense amount
    private Dictionary<String, Long> dictExpense = new Hashtable<>();
    private Dictionary<String, Long> dictBudget = new Hashtable<>();

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
                .child("david");
        DatabaseReference expensesDatabaseRef = FirebaseDatabase.getInstance().getReference()
                .child("expenses")
                //.child(user.getUsername)
                .child("david");

        budgetDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long amount = 0L;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot shot : snapshot.getChildren()) {
                        //for (int i = 0; i < dataSnapshot.getChildren().size() {
                        //System.out.println(snapshot.getKey());
                        if (Objects.requireNonNull(shot.getKey()).equalsIgnoreCase("amount")) {
                            amount = (Long) shot.getValue();
                        }
                        if (shot.getKey() == "category" && shot.getValue() == "Food") {
                            System.out.println(shot.getValue());
                            dictBudget.put((String) shot.getValue(), amount);
                            continue;
                        }
                        if (shot.getKey() == "category" && shot.getValue() == "Entertainment") {
                            System.out.println(shot.getValue());
                            dictBudget.put((String) shot.getValue(), amount);
                            continue;
                        }
                        if (shot.getKey() == "category" && shot.getValue() == "Travel") {
                            System.out.println(shot.getValue());
                            dictBudget.put((String) shot.getValue(), amount);
                            continue;
                        }
                        if (shot.getKey() == "category" && shot.getValue() == "School") {
                            System.out.println(shot.getValue());
                            dictBudget.put((String) shot.getValue(), amount);
                            continue;
                        }
                        if (shot.getKey() == "category" && shot.getValue() == "Utilities") {
                            System.out.println(shot.getValue());
                            dictBudget.put((String) shot.getValue(), amount);
                            continue;
                        }


                        }
                    }

                //System.out.println(dictBudget);



                }





            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        expensesDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long amount = 0L;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot shot : snapshot.getChildren()) {
                        //for (int i = 0; i < dataSnapshot.getChildren().size() {
                        //System.out.println(shot.getKey());
                        if (shot.getKey() == "amount") {
                            amount = (Long) snapshot.getValue();
                        }
                        if (Objects.equals(shot.getKey(), "category")) {
                            System.out.println(shot.getValue());
                            dictExpense.put((String) shot.getValue(), amount);
                            continue;
                        }
                        if (shot.getKey() == "category" && shot.getValue() == "Entertainment") {
                            System.out.println(shot.getValue());
                            dictExpense.put((String) shot.getValue(), amount);
                            continue;
                        }
                        if (shot.getKey() == "category" && shot.getValue() == "Travel") {
                            System.out.println(shot.getValue());
                            dictExpense.put((String) shot.getValue(), amount);
                            continue;
                        }
                        if (shot.getKey() == "category" && shot.getValue() == "School") {
                            System.out.println(shot.getValue());
                            dictExpense.put((String) shot.getValue(), amount);
                            continue;
                        }
                        if (shot.getKey() == "category" && shot.getValue() == "Utilities") {
                            System.out.println(shot.getValue());
                            dictExpense.put((String) shot.getValue(), amount);
                            continue;
                        }


                    }
                }
                //System.out.println(dictExpense);
                Log.d("notice of dict", dictExpense.toString());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }

    private void addNotification() {


    }

    private boolean overBudget() {
        return false;
    }









}