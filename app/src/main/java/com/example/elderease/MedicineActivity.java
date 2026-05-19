package com.example.elderease;

// Import necessary Android and Firebase classes
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MedicineActivity extends AppCompatActivity {

    private static final String TAG = "MedicineActivity";
    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    EditText etMedicineName;
    Spinner spinnerFrequency;
    TextView tvDate, tvTime;
    Button btnDate, btnTime, btnSaveReminder, btnViewReminders;
    RadioGroup radioGroupType;
    CheckBox chkRepeat;

    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine);

        etMedicineName = findViewById(R.id.etMedicineName);
        spinnerFrequency = findViewById(R.id.spinnerFrequency);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        btnDate = findViewById(R.id.btnDate);
        btnTime = findViewById(R.id.btnTime);
        radioGroupType = findViewById(R.id.radioGroupType);
        chkRepeat = findViewById(R.id.chkRepeat);
        btnSaveReminder = findViewById(R.id.btnSaveReminder);
        btnViewReminders = findViewById(R.id.btnViewReminders);

        // **CRITICAL FIX**: Request notification permission on modern Android.
        requestNotificationPermission();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        btnViewReminders.setOnClickListener(v -> {
            startActivity(new Intent(MedicineActivity.this, ViewRemindersActivity.class));
        });

        String[] freqOptions = {"Once a Day", "Twice a Day", "Thrice a Day"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, freqOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapter);

        btnDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, y, m, d) -> {
                        selectedYear = y;
                        selectedMonth = m;
                        selectedDay = d;
                        tvDate.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
                    }, year, month, day);
            datePickerDialog.show();
        });

        btnTime.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, h, m) -> {
                        selectedHour = h;
                        selectedMinute = m;
                        tvTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
                    }, hour, minute, true);
            timePickerDialog.show();
        });

        btnSaveReminder.setOnClickListener(v -> saveReminder());
    }
    
    private void saveReminder() {
        String medName = etMedicineName.getText().toString().trim();
        String frequency = spinnerFrequency.getSelectedItem().toString();

        int selectedTypeId = radioGroupType.getCheckedRadioButtonId();
        RadioButton selectedType = findViewById(selectedTypeId);
        boolean repeat = chkRepeat.isChecked();

        if (medName.isEmpty() || tvDate.getText().toString().isEmpty() || tvTime.getText().toString().isEmpty() || selectedTypeId == -1) {
            Toast.makeText(this, "Please fill all details!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to save a reminder.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        Map<String, Object> reminder = new HashMap<>();
        reminder.put("userId", userId);
        reminder.put("medicine_name", medName);
        reminder.put("type", selectedType.getText().toString());
        reminder.put("frequency", frequency);
        reminder.put("date", tvDate.getText().toString());
        reminder.put("time", tvTime.getText().toString());
        reminder.put("repeat", repeat);

        db.collection("reminders")
                .add(reminder)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Reminder Saved Successfully!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());

                    scheduleAlarm(medName, selectedType.getText().toString(), documentReference.getId().hashCode());

                    etMedicineName.setText("");
                    tvDate.setText("");
                    tvTime.setText("");
                    radioGroupType.clearCheck();
                    chkRepeat.setChecked(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving reminder!", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error adding document", e);
                });
    }

    private void scheduleAlarm(String medicineName, String medicineType, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("title", "Medicine Reminder");
        intent.putExtra("message", "It\'s time to take your " + medicineName + " (" + medicineType + ").");

        // **DEFINITIVE FIX**: Use FLAG_MUTABLE to allow the PendingIntent to be updated.
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0);
        long timeInMillis = calendar.getTimeInMillis();

        if (timeInMillis <= System.currentTimeMillis()) {
            Toast.makeText(this, "The selected time has already passed!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                Toast.makeText(this, "Reminder set for " + tvDate.getText() + " at " + tvTime.getText(), Toast.LENGTH_SHORT).show();
            } else {
                Intent permissionIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(permissionIntent);
                Toast.makeText(this, "Please grant permission to schedule exact alarms.", Toast.LENGTH_LONG).show();
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            Toast.makeText(this, "Reminder set for " + tvDate.getText() + " at " + tvTime.getText(), Toast.LENGTH_SHORT).show();
        }
    }

    // **NEW**: Method to request notification permission for Android 13+.
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    // Handle the result of the permission request.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied. Reminders may not appear.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
