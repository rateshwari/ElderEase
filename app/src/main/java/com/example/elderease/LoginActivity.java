package com.example.elderease;

// Import necessary Android and Firebase classes
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// LoginActivity handles the user login process.
public class LoginActivity extends AppCompatActivity {

    // A constant for logging purposes.
    private static final String TAG = "LoginActivity";

    // UI elements for email, password, and the login button.
    private EditText etEmail, etPassword;
    private Button btnLogin;

    // Firebase Authentication object.
    private FirebaseAuth mAuth;

    // This method is called when the activity is first created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass method.
        super.onCreate(savedInstanceState);
        // Set the layout for this activity.
        setContentView(R.layout.activity_login);

        // Initialize the UI elements by finding them in the layout file.
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();

        // Set a click listener for the login button.
        btnLogin.setOnClickListener(v -> {
            // Get the email and password from the EditText fields.
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validate that the email and password are not empty.
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Sign in the user with Firebase Authentication.
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        // If the sign-in is successful...
                        if (task.isSuccessful()) {
                            // Log the success for debugging.
                            Log.d(TAG, "signInWithEmail:success");
                            // Get the currently signed-in user.
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Update the UI.
                            updateUI(user);
                        } else {
                            // If the sign-in fails, log the error and show a toast message.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Authentication failed.";
                            Toast.makeText(LoginActivity.this, "Login Failed: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                            // Clear the UI.
                            updateUI(null);
                        }
                    });
        });
    }

    // This method is called when the activity is becoming visible to the user.
    @Override
    public void onStart() {
        // Call the superclass method.
        super.onStart();
        // Check if a user is already signed in.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // If a user is signed in, update the UI.
        if(currentUser != null){
            updateUI(currentUser);
        }
    }

    // This method updates the UI based on the user's sign-in status.
    private void updateUI(FirebaseUser user) {
        // If a user is signed in...
        if (user != null) {
            // Create an intent to open the MainActivity.
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            // Start the MainActivity.
            startActivity(intent);
            // Finish this activity so the user can't go back to it.
            finish();
        } else {
            // If no user is signed in, stay on the login screen.
        }
    }
}
