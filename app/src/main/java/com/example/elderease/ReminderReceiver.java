package com.example.elderease;

// Import necessary Android classes
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// ReminderReceiver is a BroadcastReceiver that is triggered by the AlarmManager.
// It is responsible for showing the medicine reminder notification.
public class ReminderReceiver extends BroadcastReceiver {

    // This method is called when the BroadcastReceiver receives an Intent broadcast.
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the title and message from the intent that was sent by the AlarmManager.
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        // Call the showNotification method in NotificationHelper to display the notification.
        NotificationHelper.showNotification(context, title, message);
    }
}
