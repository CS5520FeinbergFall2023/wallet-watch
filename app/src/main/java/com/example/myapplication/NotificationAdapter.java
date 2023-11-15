package com.example.myapplication;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Notification> notificationsReceived;
    private User user;

    public NotificationAdapter(Context context, ArrayList<Notification> notificationsReceived) {
        this.context = context;
        this.notificationsReceived = notificationsReceived;

    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflating the layout (ie giving a look to our our items/rows)
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {
        //assigning values to the views we created in the recycler view layout file based on the
        //position of the recycler view

    }

    @Override
    public int getItemCount() {
        // how many notifications does a user have
        return notificationsReceived.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //grabbing the image view and text views and assigning them to their values
        ImageView imageView;
        TextView header, date, message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.categoryImageIcon);
            header = itemView.findViewById(R.id.header_rv_text);
            message = itemView.findViewById(R.id.notifications_rv);
        }
    }
}
