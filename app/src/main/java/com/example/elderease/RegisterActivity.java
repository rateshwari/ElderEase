package com.example.elderease;

// Import necessary Android and Firebase classes
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

// RegisterActivity handles the user registration process.
public class RegisterActivity extends AppCompatActivity {

    // UI elements for the registration form.
    private EditText etName, etAge, etContact, etAddress, etEmail, etPassword;
    private Button btnRegister;
    private ProgressBar progressBar;

    // Firebase Authentication and Firestore objects.
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // This method is called when the activity is first created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass method.
        super.onCreate(savedInstanceState);
        // Set the layout for this activity.
        setContentView(R.layout.activity_register);

        // Initialize the UI elements by finding them in the layout file.
        etName = findViewById(R.id.etRegName);
        etAge = findViewById(R.id.etRegAge);
        etContact = findViewById(R.id.etRegContact);
        etAddress = findViewById(R.id.etRegAddress);
        etEmail = findViewById(R.id.etRegEmail);
        etPassword = findViewById(R.id.etRegPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBarRegister);

        // Initialize the FirebaseAuth and FirebaseFirestore instances.
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set a click listener for the register button.
        btnRegister.setOnClickListener(v -> {
            // Get the user's input from the EditText fields.
            String name = etName.getText().toString().trim();
            String age = etAge.getText().toString().trim();
            String contact = etContact.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validate that all fields are filled.
            if (name.isEmpty() || age.isEmpty() || contact.isEmpty() || address.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate that the password is at least 6 characters long.
            if (password.length() < 6) {
                Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show the progress bar to indicate that registration is in progress.
            progressBar.setVisibility(View.VISIBLE);

            // Create a new user with Firebase Authentication.
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        // If the user creation is successful...
                        if (task.isSuccessful()) {
                            // Get the newly created user.
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            // If the user is not null...
                            if (firebaseUser != null) {
                                // Get the user's unique ID.
                                String userId = firebaseUser.getUid();
                                // Create a map to store the user's profile data.
                                Map<String, Object> user = new HashMap<>();
                                user.put("name", name);
                                user.put("age", age);
                                user.put("contact", contact);
                                user.put("address", address);

                                // Save the user's profile data to Firestore.
                                db.collection("users").document(userId)
                                        .set(user)
                                        .addOnSuccessListener(aVoid -> {
                                            // Hide the progress bar.
                                            progressBar.setVisibility(View.GONE);
                                            // Show a success message.
                                            Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                            // Create an intent to open the MainActivity.
                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                            // Clear the activity stack so the user can't go back to the registration screen.
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            // Start the MainActivity.
                                            startActivity(intent);
                                            // Finish this activity.
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Hide the progress bar.
                                            progressBar.setVisibility(View.GONE);
                                            // Show an error message if the profile data could not be saved.
                                            Toast.makeText(RegisterActivity.this, "Error saving profile data.", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            // If the user creation fails, hide the progress bar and show an error message.
                            progressBar.setVisibility(View.GONE);
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Registration failed.";
                            Toast.makeText(RegisterActivity.this, "Registration Failed: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
