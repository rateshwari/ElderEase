package com.example.elderease;

// Import necessary Android classes
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

// NotificationHelper is a utility class for creating and showing notifications.
public class NotificationHelper {

    // This static method can be called from anywhere in the app to show a notification.
    public static void showNotification(Context context, String title, String message) {
        // Define a unique ID for the notification channel.
        String channelId = "medicine_reminder_channel";
        // Get an instance of the NotificationManager system service.
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // For Android 8.0 (Oreo) and above, notifications must be assigned to a channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create a new notification channel.
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Medicine Reminders", // The user-visible name of the channel.
                    NotificationManager.IMPORTANCE_HIGH); // Set the importance level of the channel.
            // Create the notification channel.
            manager.createNotificationChannel(channel);
        }

        // Create an intent to open the MainActivity when the notification is tapped.
        Intent intent = new Intent(context, MainActivity.class);
        // Create a PendingIntent to wrap the intent.
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Use NotificationCompat.Builder to create the notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.logo) // Set the small icon for the notification.
                .setContentTitle(title) // Set the title of the notification.
                .setContentText(message) // Set the message text of the notification.
                .setContentIntent(pendingIntent) // Set the intent to be fired when the notification is tapped.
                .setAutoCancel(true) // Automatically dismiss the notification when it is tapped.
                .setPriority(NotificationCompat.PRIORITY_HIGH); // Set the priority of the notification for older Android versions.

        // Show the notification.
        // We use the current time in milliseconds as a unique ID for the notification.
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
