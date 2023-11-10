package com.example.myapplication;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    public FirebaseHelper() {
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    // Authentication methods

    public Task<AuthResult> registerUser(String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> loginUser(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    public void logoutUser() {
        auth.signOut();
    }

    // Budget methods

    public DatabaseReference getUserBudgetsReference(String userId) {
        return databaseReference.child("budgets").child(userId);
    }

    public Task<Void> addBudget(String userId, Budget budget) {
        DatabaseReference budgetRef = getUserBudgetsReference(userId).push();
        return budgetRef.setValue(budget);
    }

    public Task<Void> updateBudget(String userId, String budgetId, Budget updatedBudget) {
        DatabaseReference budgetRef = getUserBudgetsReference(userId).child(budgetId);
        return budgetRef.setValue(updatedBudget);
    }

    public Task<Void> deleteBudget(String userId, String budgetId) {
        DatabaseReference budgetRef = getUserBudgetsReference(userId).child(budgetId);
        return budgetRef.removeValue();
    }

    // Expense methods

    public DatabaseReference getUserExpensesReference(String userId) {
        return databaseReference.child("expenses").child(userId);
    }

    public Task<Void> addExpense(String userId, Expense expense) {
        DatabaseReference expenseRef = getUserExpensesReference(userId).push();
        return expenseRef.setValue(expense);
    }

    public Task<Void> updateExpense(String userId, String expenseId, Expense updatedExpense) {
        DatabaseReference expenseRef = getUserExpensesReference(userId).child(expenseId);
        return expenseRef.setValue(updatedExpense);
    }

    public Task<Void> deleteExpense(String userId, String expenseId) {
        DatabaseReference expenseRef = getUserExpensesReference(userId).child(expenseId);
        return expenseRef.removeValue();
    }

    // Notification methods

    public DatabaseReference getUserNotificationsReference(String userId) {
        return databaseReference.child("notifications").child(userId);
    }

    public Task<Void> addNotification(String userId, Notification notification) {
        DatabaseReference notificationRef = getUserNotificationsReference(userId).push();
        return notificationRef.setValue(notification);
    }

    // Other methods for visualization, image storage, etc., can be added based on your requirements.
}

