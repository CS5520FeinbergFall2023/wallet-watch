package com.example.myapplication;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FirebaseHelper {

    private final DatabaseReference databaseReference;
    private final StorageReference storageReference;

    // Define a callback interface
    public interface CategoriesCallback {
        void onCategoriesLoaded(Set<String> categories);

        void onCancelled(DatabaseError databaseError);
    }

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

    public void uploadImage(Uri imageUri) {
        if (imageUri != null) {

            StorageReference riversRef = storageReference.child("" + imageUri.getLastPathSegment());
            UploadTask uploadTask = riversRef.putFile(imageUri);

            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }

                // Continue with the task to get the download URL
                return riversRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String imageUrl = downloadUri.toString();
                } else {
                    // Handle failures
                    // ...
                }
            });
        }
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
                    registerUser(password, username, onCompleteListener);
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

//    public Task<Void> addBudget(String userId, Budget budget) {
//        DatabaseReference budgetRef = getUserBudgetsReference(userId).push();
//        return budgetRef.setValue(budget);
//    }
//
//    public Task<Void> updateBudget(String userId, String budgetId, Budget updatedBudget) {
//        DatabaseReference budgetRef = getUserBudgetsReference(userId).child(budgetId);
//        return budgetRef.setValue(updatedBudget);
//    }
//
//    public Task<Void> deleteBudget(String userId, String budgetId) {
//        DatabaseReference budgetRef = getUserBudgetsReference(userId).child(budgetId);
//        return budgetRef.removeValue();
//    }

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
//
//    public Task<Void> addExpense(String userId, Expense expense) {
//        DatabaseReference expenseRef = getUserExpensesReference(userId).push();
//        return expenseRef.setValue(expense);
//    }
//
//    public Task<Void> updateExpense(String userId, String expenseId, Expense updatedExpense) {
//        DatabaseReference expenseRef = getUserExpensesReference(userId).child(expenseId);
//        return expenseRef.setValue(updatedExpense);
//    }
//
//    public Task<Void> deleteExpense(String userId, String expenseId) {
//        DatabaseReference expenseRef = getUserExpensesReference(userId).child(expenseId);
//        return expenseRef.removeValue();
//    }
//
//    // Notification methods
//
//    public DatabaseReference getUserNotificationsReference(String userId) {
//        return databaseReference.child("notifications").child(userId);
//    }
//
//    public Task<Void> addNotification(String userId, Notification notification) {
//        DatabaseReference notificationRef = getUserNotificationsReference(userId).push();
//        return notificationRef.setValue(notification);
//    }

    // Categories

//    public void getCategories(CategoriesCallback callback) {
//        DatabaseReference userBudgetsRef = FirebaseDatabase.getInstance().getReference().child("categories");
//        userBudgetsRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                List<Category> categoryList = new ArrayList<>();
//                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
//                    String categoryName = categorySnapshot.getValue(String.class);
//                    if (categoryName != null) {
//                        categoryList.add(new Category(categoryName));
//                    }
//                }
//                callback.onCallback(categoryList);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                //TODO: Error handling
//            }
//        });
//    }

    /**
     * Get the categories of a user from each category in their budget.
     * @param  username username to get categories
     * @param valueEventListener callback after categories is fetched
     */
    public void getCategories(String username,ValueEventListener valueEventListener) {
        DatabaseReference budgetsRef = FirebaseDatabase.getInstance().getReference("budgets").child(username);

        budgetsRef.addListenerForSingleValueEvent(valueEventListener);
    }


    /**
     * Create an expense.
     * @param expense Expense
     * @param username user to create the expense for
     * @param onCompleteListener listener for after expense is created
     */
    public void createExpense(String username, Expense expense, OnCompleteListener<Void> onCompleteListener) {
        DatabaseReference expensesRef = FirebaseDatabase.getInstance().getReference("expenses").child(username);

        DatabaseReference newExpenseRef = expensesRef.push();
        newExpenseRef.setValue(expense).addOnCompleteListener(onCompleteListener);
    }
}

