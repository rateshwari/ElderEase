package com.example.elderease;

// Import necessary Android classes
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;

// HealthLogActivity allows users to log and view their daily health data.
public class HealthLogActivity extends AppCompatActivity {

    // UI elements for the health log form.
    EditText etDate, etBP, etSugar, etPulse;
    Button btnSave, btnView;
    TextView tvLogs;
    // File names for storing the health log data.
    String fileNameTxt = "health_log.txt";
    String fileNameCsv = "health_log.csv";

    // This method is called when the activity is first created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass method.
        super.onCreate(savedInstanceState);
        // Set the layout for this activity.
        setContentView(R.layout.activity_health_log);

        // Initialize the UI elements by finding them in the layout file.
        etDate = findViewById(R.id.etDate);
        etBP = findViewById(R.id.etBP);
        etSugar = findViewById(R.id.etSugar);
        etPulse = findViewById(R.id.etPulse);
        btnSave = findViewById(R.id.btnSave);
        btnView = findViewById(R.id.btnView);
        tvLogs = findViewById(R.id.tvLogs);

        // --- Date Picker Dialog ---
        // Set a click listener for the date EditText to show a DatePickerDialog.
        etDate.setOnClickListener(v -> {
            // Get the current date.
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new DatePickerDialog.
            DatePickerDialog datePicker = new DatePickerDialog(this,
                    (DatePicker view, int year1, int month1, int dayOfMonth) -> {
                        // When a date is selected, format it and set it as the text of the EditText.
                        etDate.setText(dayOfMonth + "/" + (month1 + 1) + "/" + year1);
                    }, year, month, day);
            // Show the date picker dialog.
            datePicker.show();
        });

        // Set click listeners for the save and view buttons.
        btnSave.setOnClickListener(v -> saveHealthLog());
        btnView.setOnClickListener(v -> viewHealthLogs());
    }

    // This method saves the health log data to internal storage.
    private void saveHealthLog() {
        // Get the user's input from the EditText fields.
        String date = etDate.getText().toString().trim();
        String bp = etBP.getText().toString().trim();
        String sugar = etSugar.getText().toString().trim();
        String pulse = etPulse.getText().toString().trim();

        // Validate that all fields are filled.
        if (date.isEmpty() || bp.isEmpty() || sugar.isEmpty() || pulse.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the data strings for the text and CSV files.
        String dataLine = date + " | BP: " + bp + " | Sugar: " + sugar + " | Pulse: " + pulse + "\n";
        String csvLine = date + "," + bp + "," + sugar + "," + pulse + "\n";

        try {
            // Save the data to the text file.
            FileOutputStream fosTxt = openFileOutput(fileNameTxt, MODE_APPEND);
            OutputStreamWriter oswTxt = new OutputStreamWriter(fosTxt);
            oswTxt.write(dataLine);
            oswTxt.close();

            // Save the data to the CSV file.
            FileOutputStream fosCsv = openFileOutput(fileNameCsv, MODE_APPEND);
            OutputStreamWriter oswCsv = new OutputStreamWriter(fosCsv);
            oswCsv.write(csvLine);
            oswCsv.close();

            // Show a success message and clear the form fields.
            Toast.makeText(this, "Health log saved successfully!", Toast.LENGTH_SHORT).show();
            etDate.setText("");
            etBP.setText("");
            etSugar.setText("");
            etPulse.setText("");

        } catch (Exception e) {
            // If an error occurs, print the stack trace and show an error message.
            e.printStackTrace();
            Toast.makeText(this, "Error saving data!", Toast.LENGTH_SHORT).show();
        }
    }

    // This method reads the health log data from internal storage and displays it.
    private void viewHealthLogs() {
        try {
            // Open the text file for reading.
            BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput(fileNameTxt)));
            // Create a StringBuilder to store the file contents.
            StringBuilder sb = new StringBuilder();
            String line;

            // Read the file line by line.
            while ((line = reader.readLine()) != null) {
                // Append each line to the StringBuilder.
                sb.append(line).append("\n");
            }

            // Close the reader.
            reader.close();
            // Set the text of the TextView to the file contents.
            tvLogs.setText(sb.toString());

        } catch (Exception e) {
            // If an error occurs (e.g., the file doesn't exist), show a toast message.
            Toast.makeText(this, "No saved logs found!", Toast.LENGTH_SHORT).show();
        }
    }
}
