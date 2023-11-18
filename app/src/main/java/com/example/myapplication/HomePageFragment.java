package com.example.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class HomePageFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);

        Button chooseImageButton = view.findViewById(R.id.buttonUpload);
        chooseImageButton.setOnClickListener(v -> openImageChooser());

        return view;
    }

    //code for image launcher
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    FirebaseHelper firebaseHelper = new FirebaseHelper();
                    firebaseHelper.uploadImage(result);
                }
            }
    );

    //code for image launcher
    private void openImageChooser() {
        pickImageLauncher.launch("image/*");
    }
}