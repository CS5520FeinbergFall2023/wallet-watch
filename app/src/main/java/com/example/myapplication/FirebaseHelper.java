package com.example.myapplication;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.myapplication.callback.BudgetsCallback;
import com.example.myapplication.callback.ExpensesCallback;
import com.example.myapplication.callback.CategoriesCallback;
import com.example.myapplication.dao.Budget;
import com.example.myapplication.dao.Category;
import com.example.myapplication.dao.Expense;
import com.example.myapplication.dao.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.google.firebase.storage.UploadTask;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FirebaseHelper {

    private final DatabaseReference databaseReference;
    private final StorageReference storageReference;

    public FirebaseHelper() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference().child("images");
    }

    // Authentication methods
    public void registerUser(String username, String password, OnCompleteListener<Void> onCompleteListener) {

        // Create a user entry in the database
        DatabaseReference userRef = databaseReference.child("users").child(username);
        User newUser = new User(username, password, Instant.now().toEpochMilli());

        userRef.setValue(newUser)
                .addOnCompleteListener(onCompleteListener);
    }

    public void uploadImage(Uri imageUri, String imageName, OnCompleteListener<Uri> onCompleteListener) {
        if (imageUri == null) {
            return;
        }

        StorageReference riversRef = storageReference.child("" + imageName);
        UploadTask uploadTask = riversRef.putFile(imageUri);

        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            }
            return riversRef.getDownloadUrl();
        }).addOnCompleteListener(onCompleteListener);
    }

    public void getImageFirebase(String imageName, ImageView imageView, Context context) {
        storageReference.child(imageName).getDownloadUrl().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Uri downloadUri = task.getResult();
                Glide.with(context)
                        .load(downloadUri.toString())
                        .into(imageView);
            } else {

            }
        });
    }

    public void loginUser(String username, String password, OnCompleteListener<Boolean> onCompleteListener) {
        DatabaseReference usersRef = databaseReference.child("users");

        usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean userFound = false;

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);

                    // Check if the entered password matches the stored password
                    if (user != null && user.getPassword().equals(password)) {
                        userFound = true;
                        break;
                    }
                }

                onCompleteListener.onComplete(Tasks.forResult(userFound));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                onCompleteListener.onComplete(Tasks.forException(databaseError.toException()));
            }
        });

    }

    public void registerOrLoginUser(String username, String password, OnCompleteListener<Void> onCompleteListener) {
        Query userQuery = databaseReference.child("users").orderByChild("username").equalTo(username);
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    onCompleteListener.onComplete(Tasks.forException(new Exception("Username already exists")));
                } else {
                    registerUser(username, password, onCompleteListener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                onCompleteListener.onComplete(Tasks.forException(databaseError.toException()));
            }
        });
    }


    //todo: i have created all the method definitions, so i have written methods in 2 ways, one way you can see above
    // where return type is void and i am handling it through callback, or you can handle it directly in the activity
    // where these methods are being called from, i would suggest the above way.

    // Budget methods

    public void getUserBudgets(String userId, BudgetsCallback callback) {
        DatabaseReference userBudgetsRef = FirebaseDatabase.getInstance().getReference().child("budgets").child(userId);
        userBudgetsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Budget> budgetList = new ArrayList<>();
                for (DataSnapshot budgetSnapshot : dataSnapshot.getChildren()) {
                    budgetList.add(budgetSnapshot.getValue(Budget.class));
                }
                callback.onCallback(budgetList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //TODO: Error handling
            }
        });
    }


    // Budgets


    public void updateBudgetAmount(String username, String category, int amount) {
        // Navigate to the correct node in the Firebase database for the user
        DatabaseReference userBudgetRef = FirebaseDatabase.getInstance().getReference()
                .child("budgets")
                .child(username);

        // Find the correct budget entry for the specified category
        Query categoryQuery = userBudgetRef.orderByChild("category").equalTo(category);
        categoryQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Category exists, update the existing entry
                    for (DataSnapshot budgetSnapshot : snapshot.getChildren()) {
                        budgetSnapshot.getRef().child("amount").setValue(amount);
                    }
                } else {
                    // Category doesn't exist, create a new budget entry
                    String budgetId = userBudgetRef.push().getKey();
                    DatabaseReference newBudgetRef = userBudgetRef.child(budgetId);
                    newBudgetRef.child("category").setValue(category);
                    newBudgetRef.child("amount").setValue(amount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void getBudgetAmount(String username, ValueEventListener valueEventListener) {
        DatabaseReference userBudgetRef = FirebaseDatabase.getInstance().getReference()
                .child("budgets")
                .child(username);

        userBudgetRef.addListenerForSingleValueEvent(valueEventListener);
    }


    // Expense methods

    public void getUserExpenses(String userId, ExpensesCallback callback) {
        DatabaseReference userExpensesRef = FirebaseDatabase.getInstance().getReference().child("expenses").child(userId);
        userExpensesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Expense> expensesList = new ArrayList<>();
                for (DataSnapshot expenseSnapshot : dataSnapshot.getChildren()) {
                    Expense expense = expenseSnapshot.getValue(Expense.class);
                    expensesList.add(expense);
                }
                callback.onCallback(expensesList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //TODO: Error handling
            }
        });
    }

    /**
     * Delete an expense for a user.
     *
     * @param expense            Expense
     * @param username           user to create the expense for
     * @param onCompleteListener listener for after expense is created
     */
    public void deleteExpense(String username, Expense expense, OnCompleteListener<Void> onCompleteListener) {
        DatabaseReference expensesRef = FirebaseDatabase.getInstance().getReference("expenses").child(username);

        String expenseId = expense.getId();
        String expenseImageName = expense.getImageUrl();

        // Find the expense by id
        Query expenseQuery = expensesRef.orderByChild("id").equalTo(expenseId);

        expenseQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Expense id exists, delete the expense
                    // Iterate through the results (though there should be only one)
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        // Get the key of the expense node
                        String expenseKey = dataSnapshot.getKey();

                        if (expenseKey != null) {
                            expensesRef.child(expenseKey).removeValue()
                                    .addOnCompleteListener(task -> {
                                        if (expenseImageName.isEmpty()) {
                                            onCompleteListener.onComplete(task);
                                        } else {
                                            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/" + expenseImageName);
                                            storageReference.delete().addOnCompleteListener(onCompleteListener);
                                        }
                                    });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    // Categories

    public void getCategories(CategoriesCallback callback) {
        DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference("categories");
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Category> categories = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String categoryName = snapshot.getValue(String.class);
                    categories.add(new Category(categoryName));
                }
                callback.onCallback(categories);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    /**
     * Get the categories of a user from each category in their budget.
     *
     * @param username           username to get categories
     * @param valueEventListener callback after categories is fetched
     */
    public void getCategories(String username, ValueEventListener valueEventListener) {
        DatabaseReference budgetsRef = FirebaseDatabase.getInstance().getReference("budgets").child(username);

        budgetsRef.addListenerForSingleValueEvent(valueEventListener);
    }


    /**
     * Create an expense.
     *
     * @param expense            Expense
     * @param username           user to create the expense for
     * @param onCompleteListener listener for after expense is created
     */
    public void createExpense(String username, Expense expense, OnCompleteListener<Void> onCompleteListener) {
        DatabaseReference expensesRef = FirebaseDatabase.getInstance().getReference("expenses").child(username);

        DatabaseReference newExpenseRef = expensesRef.push();
        newExpenseRef.setValue(expense).addOnCompleteListener(onCompleteListener);
    }

    public void createNotification(String username, Notification notification) {
        DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference().child("notifications").child(username);

        DatabaseReference newNotificationRef = notificationRef.push();
        newNotificationRef.setValue(notification);


    }


}

