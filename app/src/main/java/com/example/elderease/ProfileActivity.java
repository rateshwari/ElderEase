package com.example.elderease;

// Import necessary Android and Firebase classes
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

// ProfileActivity handles the user's profile, including viewing, editing, and saving data.
public class ProfileActivity extends AppCompatActivity {

    // Constants for permission requests and logging.
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final String TAG = "ProfileActivity";

    // UI elements for the profile screen.
    ImageView profileImage;
    EditText nameInput, ageInput, contactInput, addressInput;
    Button editBtn, saveBtn, changePhotoBtn, btnSignOut;
    Uri imageUri;

    // A flag to track whether the profile is in edit mode.
    boolean isEditMode = false;

    // Firebase Firestore and Authentication objects.
    private FirebaseFirestore db;
    private DocumentReference userRef;
    private FirebaseAuth mAuth;

    // An ActivityResultLauncher to handle the result of the image picker intent.
    private ActivityResultLauncher<Intent> pickImageLauncher;

    // This method is called when the activity is first created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass method.
        super.onCreate(savedInstanceState);
        // Set the layout for this activity.
        setContentView(R.layout.activity_profile);

        // Initialize the UI elements by finding them in the layout file.
        profileImage = findViewById(R.id.imgProfile);
        nameInput = findViewById(R.id.etName);
        ageInput = findViewById(R.id.etAge);
        contactInput = findViewById(R.id.etContact);
        addressInput = findViewById(R.id.etAddress);
        editBtn = findViewById(R.id.btnEditProfile);
        saveBtn = findViewById(R.id.btnSaveProfile);
        changePhotoBtn = findViewById(R.id.btnChangePhoto);
        btnSignOut = findViewById(R.id.btnSignOut);

        // Initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();
        // Get the currently signed-in user.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // If no user is signed in, redirect to the LoginActivity.
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize the FirebaseFirestore instance and get a reference to the user's document.
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("users").document(currentUser.getUid());

        // Load the user's profile data.
        loadProfile();
        // Set the profile fields to be uneditable by default.
        setEditable(false);

        // Initialize the ActivityResultLauncher for picking an image.
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // If the user picked an image successfully...
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Get the URI of the selected image.
                        imageUri = result.getData().getData();
                        // If the URI is not null...
                        if (imageUri != null) {
                            try {
                                // Persist the permission to read the image URI across device restarts.
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    final int takeFlags = result.getData().getFlags()
                                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                    getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                                }
                            } catch (Exception e) {
                                // Log any errors.
                                e.printStackTrace();
                            }
                            // Set the selected image as the profile image.
                            profileImage.setImageURI(imageUri);
                        } else {
                            // If no image was selected, show a toast message.
                            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Set a click listener for the "Change Photo" button.
        changePhotoBtn.setOnClickListener(v -> {
            // If not in edit mode, show a toast and do nothing.
            if (!isEditMode) {
                Toast.makeText(this, "Enable edit mode to change photo", Toast.LENGTH_SHORT).show();
                return;
            }

            // If the app doesn't have storage permission, request it.
            if (!hasStoragePermission()) {
                requestStoragePermission();
            } else {
                // If permission is already granted, open the gallery to pick an image.
                openGallery();
            }
        });

        // Set a click listener for the "Save" button.
        saveBtn.setOnClickListener(v -> {
            // Save the profile data.
            saveProfile();
            // Disable edit mode.
            setEditable(false);
            isEditMode = false;
        });

        // Set a click listener for the "Edit" button.
        editBtn.setOnClickListener(v -> {
            // Enable edit mode.
            isEditMode = true;
            setEditable(true);
            // Show a toast message to let the user know edit mode is enabled.
            Toast.makeText(this, "Edit Mode Enabled", Toast.LENGTH_SHORT).show();
        });

        // Set a click listener for the "Sign Out" button.
        btnSignOut.setOnClickListener(v -> {
            // Sign the user out of Firebase.
            mAuth.signOut();
            // Create an intent to open the LoginActivity.
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            // Clear the activity stack so the user can't go back to the profile screen.
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // Start the LoginActivity.
            startActivity(intent);
            // Finish this activity.
            finish();
        });
    }

    // This method checks if the app has permission to read from storage.
    private boolean hasStoragePermission() {
        // For Android 13 and above, check for READ_MEDIA_IMAGES permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            // For older versions, check for READ_EXTERNAL_STORAGE permission.
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    // This method requests storage permission from the user.
    private void requestStoragePermission() {
        // For Android 13 and above, request READ_MEDIA_IMAGES permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
        } else {
            // For older versions, request READ_EXTERNAL_STORAGE permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    // This method opens the device's gallery to allow the user to pick an image.
    private void openGallery() {
        // Create an intent to open the document picker.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Add the CATEGORY_OPENABLE category to only show selectable files.
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Set the MIME type to images only.
        intent.setType("image/*");
        // Add flags to grant read and persistable URI permissions.
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        // Launch the intent using the ActivityResultLauncher.
        pickImageLauncher.launch(intent);
    }

    // This method loads the user's profile data from Firestore.
    private void loadProfile() {
        // Get the user's document from Firestore.
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            // If the document exists...
            if (documentSnapshot.exists()) {
                // Get the data from the document.
                String name = documentSnapshot.getString("name");
                String age = documentSnapshot.getString("age");
                String contact = documentSnapshot.getString("contact");
                String address = documentSnapshot.getString("address");
                String imageUriString = documentSnapshot.getString("imageUri");

                // Set the text of the EditText fields.
                nameInput.setText(name);
                ageInput.setText(age);
                contactInput.setText(contact);
                addressInput.setText(address);

                // If there is an image URI...
                if (imageUriString != null) {
                    try {
                        // Parse the URI string and set the image.
                        Uri uri = Uri.parse(imageUriString);
                        profileImage.setImageURI(uri);
                        imageUri = uri;
                    } catch (Exception e) {
                        // Log any errors.
                        Log.e(TAG, "Error loading image: " + e.getMessage());
                    }
                }
            } else {
                // If the document doesn't exist, log a message.
                Log.d(TAG, "No such document");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error getting document", e));
    }

    // This method saves the user's profile data to Firestore.
    private void saveProfile() {
        // Get the data from the EditText fields.
        String name = nameInput.getText().toString().trim();
        String age = ageInput.getText().toString().trim();
        String contact = contactInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();

        // Create a map to store the user's data.
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("age", age);
        user.put("contact", contact);
        user.put("address", address);
        // If there is a new image URI, add it to the map.
        if (imageUri != null) {
            user.put("imageUri", imageUri.toString());
        }

        // Save the user data to Firestore.
        userRef.set(user)
                .addOnSuccessListener(aVoid -> {
                    // If the save is successful, show a toast message.
                    Toast.makeText(ProfileActivity.this, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "DocumentSnapshot successfully written!");
                })
                .addOnFailureListener(e -> {
                    // If the save fails, show a toast message and log the error.
                    Toast.makeText(ProfileActivity.this, "Error saving profile", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error writing document", e);
                });
    }

    // This method enables or disables the profile editing fields.
    private void setEditable(boolean enabled) {
        // Enable or disable the EditText fields.
        nameInput.setEnabled(enabled);
        ageInput.setEnabled(enabled);
        contactInput.setEnabled(enabled);
        addressInput.setEnabled(enabled);
        // Enable or disable the "Change Photo" button.
        changePhotoBtn.setEnabled(enabled);

        // Show or hide the "Save" and "Edit" buttons based on the edit mode.
        saveBtn.setVisibility(enabled ? Button.VISIBLE : Button.GONE);
        editBtn.setVisibility(enabled ? Button.GONE : Button.VISIBLE);
    }

    // This method is called when the user responds to a permission request.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Call the superclass method.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If the permission request is for storage...
        if (requestCode == STORAGE_PERMISSION_CODE) {
            // If the permission is granted, open the gallery.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                // If the permission is denied, show a toast message.
                Toast.makeText(this, "Permission denied! Cannot change photo.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
