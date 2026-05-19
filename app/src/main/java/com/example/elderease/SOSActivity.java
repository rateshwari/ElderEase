package com.example.elderease;

// Import necessary Android classes
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;

// SOSActivity handles the emergency contact functionality.
public class SOSActivity extends AppCompatActivity {

    // UI elements for the SOS screen.
    EditText etContactNumber;
    Button btnSaveNumber, btnCallSOS, btnMessageSOS;
    // SharedPreferences for storing the emergency contact number.
    SharedPreferences sharedPreferences;

    // A constant for the call permission request code.
    private static final int REQUEST_CALL_PERMISSION = 1;

    // This method is called when the activity is first created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass method.
        super.onCreate(savedInstanceState);
        // Set the layout for this activity.
        setContentView(R.layout.activity_sos);

        // Initialize the UI elements by finding them in the layout file.
        etContactNumber = findViewById(R.id.etContactNumber);
        btnSaveNumber = findViewById(R.id.btnSaveNumber);
        btnCallSOS = findViewById(R.id.btnCallSOS);
        btnMessageSOS = findViewById(R.id.btnMessageSOS);

        // Initialize the SharedPreferences object.
        sharedPreferences = getSharedPreferences("ElderEasePrefs", MODE_PRIVATE);

        // Load the saved emergency contact number.
        String savedNumber = sharedPreferences.getString("emergency_number", "");
        // If a number is saved, display it in the EditText.
        if (!savedNumber.isEmpty()) {
            etContactNumber.setText(savedNumber);
        }

        // Set click listeners for the buttons.
        btnSaveNumber.setOnClickListener(v -> saveNumber());
        btnCallSOS.setOnClickListener(v -> showCallDialog());
        btnMessageSOS.setOnClickListener(v -> sendSOSMessage());
    }

    // This method saves the emergency contact number to SharedPreferences.
    private void saveNumber() {
        // Get the number from the EditText.
        String number = etContactNumber.getText().toString().trim();
        // If the number is empty, show a toast message and do nothing.
        if (number.isEmpty()) {
            Toast.makeText(this, "Please enter a valid number!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the number to SharedPreferences.
        sharedPreferences.edit().putString("emergency_number", number).apply();
        // Show a success message.
        Toast.makeText(this, "Emergency contact saved!", Toast.LENGTH_SHORT).show();
    }

    // This method shows a confirmation dialog before making a phone call.
    private void showCallDialog() {
        // Get the saved emergency contact number.
        String number = sharedPreferences.getString("emergency_number", "");
        // If no number is saved, show a toast message and do nothing.
        if (number.isEmpty()) {
            Toast.makeText(this, "Please save a contact first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new AlertDialog.
        new AlertDialog.Builder(this)
                .setTitle("Confirm Call")
                .setMessage("Do you want to call your emergency contact?")
                // Set the positive button to make the phone call.
                .setPositiveButton("Yes", (dialog, which) -> makePhoneCall(number))
                // Set the negative button to do nothing.
                .setNegativeButton("No", null)
                // Show the dialog.
                .show();
    }

    // This method makes a phone call to the given number.
    private void makePhoneCall(String number) {
        // Create an intent to make a phone call.
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        // Set the data for the intent (the phone number).
        callIntent.setData(Uri.parse("tel:" + number));

        // Check if the app has permission to make phone calls.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // If not, request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
            return;
        }

        // Start the call intent.
        startActivity(callIntent);
    }

    // This method sends an SOS message to the saved emergency contact number.
    private void sendSOSMessage() {
        // Get the saved emergency contact number.
        String number = sharedPreferences.getString("emergency_number", "");
        // If no number is saved, show a toast message and do nothing.
        if (number.isEmpty()) {
            Toast.makeText(this, "Please save a contact first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create an intent to send an SMS message.
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        // Set the data for the intent (the phone number).
        smsIntent.setData(Uri.parse("sms:" + number));
        // Set the body of the SMS message.
        smsIntent.putExtra("sms_body", "🚨 SOS! I need immediate help. Please contact me as soon as possible.");
        // Start the SMS intent.
        startActivity(smsIntent);
    }
}
