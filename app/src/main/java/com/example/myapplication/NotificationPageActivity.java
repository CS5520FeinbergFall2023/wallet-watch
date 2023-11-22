package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import com.example.myapplication.dao.Category;

public class NotificationPageActivity extends AppCompatActivity {
    RecyclerView notificationRV;
    private NotificationAdapter adapter;
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
        notificationRV = (RecyclerView) findViewById(R.id.notifications_rv);



        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        notificationRV.setLayoutManager(layoutManager);

        ArrayList<Notification> receivednotifications = new ArrayList<>();

        adapter = new NotificationAdapter(this, receivednotifications);
        notificationRV.setAdapter(adapter);
        String notes = "notifications/"+"kartik";

        DatabaseReference notificationsReference = FirebaseDatabase.getInstance().getReference(notes);
        notificationsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Notification> newNotificationReceived = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("notice", snapshot.toString());

                    /*
                    Notification notification = snapshot.getValue(Notification.class);
                    if (notification != null) {
                        newNotificationReceived.add(notification);
                    }

                     */
                }
                adapter.setNotificationsReceived(newNotificationReceived);
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
                .child("david");
        DatabaseReference expensesDatabaseRef = FirebaseDatabase.getInstance().getReference()
                .child("expenses")
                //.child(user.getUsername)
                .child("david");


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
                    Log.d("notice of dict Budget", dictBudget.toString());



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
                        if("category".equalsIgnoreCase(shot.getKey()) && "Entertainment".equalsIgnoreCase(shot.getValue().toString())) {
                            //System.out.println(shot.getValue());
                            entertainmentAmount += amount;
                            //entertainmentAmount += (Long) snapshot.child("amount").getValue();
                            dictExpense.put((String) shot.getValue(), entertainmentAmount);
                            continue;
                        }
                        if ("category".equalsIgnoreCase(shot.getKey()) && "Food".equalsIgnoreCase(shot.getValue().toString())) {
                            //System.out.println(shot.getValue());
                            foodAmount += amount;
                            //foodAmount += (Long) snapshot.child("amount").getValue();
                            dictExpense.put((String) shot.getValue(), foodAmount);
                            continue;
                        }
                        if ("category".equalsIgnoreCase(shot.getKey()) && "Travel".equalsIgnoreCase(shot.getValue().toString())) {
                            //System.out.println(shot.getValue());
                            travelAmount += amount;
                            //foodAmount += (Long) snapshot.child("amount").getValue();
                            dictExpense.put((String) shot.getValue(), amount);
                            continue;
                        }
                        if ("category".equalsIgnoreCase(shot.getKey()) && "Utilities".equalsIgnoreCase(shot.getValue().toString())) {
                            //System.out.println(shot.getValue());
                            utilitiesAmount += amount;
                            //utilitiesAmount += (Long) snapshot.child("amount").getValue();
                            dictExpense.put((String) shot.getValue(), utilitiesAmount);
                            continue;
                        }


                    }

                }
                //System.out.println(dictExpense);
                Log.d("notice of dict Expense", dictExpense.toString());


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