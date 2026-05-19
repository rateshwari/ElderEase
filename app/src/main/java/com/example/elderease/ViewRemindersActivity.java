package com.example.elderease;

// Import necessary Android and Firebase classes
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// ViewRemindersActivity displays a list of the user's medicine reminders.
public class ViewRemindersActivity extends AppCompatActivity {

    // A constant for logging purposes.
    private static final String TAG = "ViewRemindersActivity";

    // UI element for displaying the list of reminders.
    ListView listView;
    // Firebase Firestore and Authentication objects.
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    // An ArrayList to store the reminder data.
    ArrayList<Map<String, String>> reminderList;
    // A SimpleAdapter to connect the reminder data to the ListView.
    SimpleAdapter adapter;

    // This method is called when the activity is first created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass method.
        super.onCreate(savedInstanceState);
        // Set the layout for this activity.
        setContentView(R.layout.activity_view_reminders);

        // Initialize the UI elements by finding them in the layout file.
        listView = findViewById(R.id.listViewReminders);
        // Initialize the FirebaseFirestore and FirebaseAuth instances.
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get the currently signed-in user.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // If no user is signed in, redirect to the LoginActivity.
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize the ArrayList to store the reminder data.
        reminderList = new ArrayList<>();

        // Initialize the SimpleAdapter.
        adapter = new SimpleAdapter(
                this,
                reminderList,
                R.layout.list_item_reminder,
                new String[]{"medicine_name", "time"},
                new int[]{R.id.text1, R.id.text2});

        // Set the adapter for the ListView.
        listView.setAdapter(adapter);

        // Load the reminders for the current user.
        loadReminders(currentUser.getUid());
    }

    // This method loads the reminders for the given user ID from Firestore.
    private void loadReminders(String userId) {
        // Query the "reminders" collection for documents where the "userId" field matches the current user's ID.
        db.collection("reminders")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    // If the query is successful...
                    if (task.isSuccessful()) {
                        // Clear the reminder list to avoid duplicates.
                        reminderList.clear();
                        // For each document in the result...
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Create a map to store the reminder data.
                            Map<String, String> reminder = new HashMap<>();
                            // Get the medicine name and time from the document.
                            reminder.put("medicine_name", document.getString("medicine_name"));
                            reminder.put("time", document.getString("time"));
                            // Add the reminder to the list.
                            reminderList.add(reminder);
                        }

                        // If the reminder list is empty, add a placeholder message.
                        if (reminderList.isEmpty()) {
                            Map<String, String> empty = new HashMap<>();
                            empty.put("medicine_name", "No reminders saved yet.");
                            empty.put("time", "");
                            reminderList.add(empty);
                        }

                        // Notify the adapter that the data has changed, so it can update the ListView.
                        adapter.notifyDataSetChanged();
                    } else {
                        // If the query fails, log the error and show a toast message.
                        Log.e(TAG, "Error getting documents: ", task.getException());
                        Toast.makeText(this, "Error loading reminders", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
