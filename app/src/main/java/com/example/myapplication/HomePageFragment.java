package com.example.myapplication;

import static android.content.Context.MODE_PRIVATE;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class HomePageFragment extends Fragment {

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String LOGGED_IN_KEY = "isLoggedIn";

    private static final String USERNAME = "username";

    private String username;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Get username from local storage
        username = prefs.getString(USERNAME,"");

        View view = inflater.inflate(R.layout.fragment_home_page, container, false);


        return view;
    }


    //code for image launcher
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    FirebaseHelper firebaseHelper = new FirebaseHelper();
                    firebaseHelper.uploadImage(result, v -> {
                        Toast.makeText(getContext(), "Image Uploaded!", Toast.LENGTH_SHORT).show();
                    });
                }
            }
    );

    //code for image launcher
    private void openImageChooser() {
        pickImageLauncher.launch("image/*");
    }
}