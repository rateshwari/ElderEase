package com.example.elderease;

// Import necessary Android classes
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

// StartActivity is the first screen the user sees.
// It provides options to either log in or register.
public class StartActivity extends AppCompatActivity {

    // This method is called when the activity is first created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass method.
        super.onCreate(savedInstanceState);
        // Set the layout for this activity.
        setContentView(R.layout.activity_start);

        // Initialize the UI elements by finding them in the layout file.
        Button btnLogin = findViewById(R.id.btnGoToLogin);
        Button btnRegister = findViewById(R.id.btnGoToRegister);

        // Set a click listener for the login button.
        // When the button is clicked, it will open the LoginActivity.
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(StartActivity.this, LoginActivity.class));
        });

        // Set a click listener for the register button.
        // When the button is clicked, it will open the RegisterActivity.
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(StartActivity.this, RegisterActivity.class));
        });
    }
}
