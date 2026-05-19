package com.example.elderease;

// Import necessary Android and Firebase classes
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Locale;

// MainActivity is the main dashboard of the app.
// It implements TextToSpeech.OnInitListener to handle text-to-speech functionality.
public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    // A constant for the audio recording permission request code.
    private static final int RECORD_AUDIO_PERMISSION_CODE = 102;

    // UI elements for the dashboard buttons.
    CardView btnMedicine, btnSOS, btnHealth, btnMap, btnProfile;
    FloatingActionButton btnVoice;

    // Objects for text-to-speech and speech recognition.
    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;

    // Firebase Authentication object.
    private FirebaseAuth mAuth;

    // This method is called when the activity is first created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass method.
        super.onCreate(savedInstanceState);
        // Set the layout for this activity.
        setContentView(R.layout.activity_main);

        // Initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();

        // Get the currently signed-in user.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // If no user is signed in, redirect to the StartActivity.
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(intent);
            // Finish this activity so the user can't go back to it without signing in.
            finish();
            // Stop executing the rest of the code in this method.
            return;
        }

        // Initialize the UI elements by finding them in the layout file.
        btnMedicine = findViewById(R.id.btnMedicine);
        btnSOS = findViewById(R.id.btnSOS);
        btnHealth = findViewById(R.id.btnHealth);
        btnMap = findViewById(R.id.btnMap);
        btnProfile = findViewById(R.id.btnProfile);
        btnVoice = findViewById(R.id.btnVoice);

        // Set click listeners for each of the dashboard buttons.
        // When a button is clicked, it will open the corresponding activity.
        btnMedicine.setOnClickListener(v -> startActivity(new Intent(this, MedicineActivity.class)));
        btnSOS.setOnClickListener(v -> startActivity(new Intent(this, SOSActivity.class)));
        btnHealth.setOnClickListener(v -> startActivity(new Intent(this, HealthLogActivity.class)));
        btnMap.setOnClickListener(v -> startActivity(new Intent(this, MapActivity.class)));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        // --- Voice Mode Setup ---
        // Initialize the TextToSpeech and SpeechRecognizer objects.
        textToSpeech = new TextToSpeech(this, this);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        // Create an intent for the speech recognizer.
        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // Set the language model to free-form speech.
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Set the language to the user's default device language.
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        // Set a listener to handle the results of the speech recognition.
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            // This method is called when the recognizer is ready for speech.
            @Override
            public void onReadyForSpeech(Bundle params) {
                // Show a toast message to let the user know the app is listening.
                Toast.makeText(MainActivity.this, "Listening...", Toast.LENGTH_SHORT).show();
            }

            // These methods are part of the RecognitionListener interface but are not used in this app.
            @Override
            public void onBeginningOfSpeech() {}
            @Override
            public void onRmsChanged(float rmsdB) {}
            @Override
            public void onBufferReceived(byte[] buffer) {}
            @Override
            public void onEndOfSpeech() {}

            // This method is called when an error occurs during recognition.
            @Override
            public void onError(int error) {
                // Get a user-friendly error message.
                String errorMessage = getErrorText(error);
                // Log the error for debugging.
                Log.e("SpeechRecognizer", "Error: " + errorMessage);
                // Speak the error message to the user.
                if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                    speak("I didn't catch that. Please try again.");
                } else if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    speak("I didn't hear anything. Please try again.");
                } else {
                    speak("Sorry, something went wrong with the voice recognition.");
                }
            }

            // This method is called when speech recognition is successful.
            @Override
            public void onResults(Bundle results) {
                // Get the list of recognized words.
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                // If there are any matches, process the first one.
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    // Log the recognized text for debugging.
                    Log.d("SpeechRecognizer", "Recognized: " + spokenText);
                    // Process the voice command.
                    processVoiceCommand(spokenText);
                } else {
                    // If no results are found, let the user know.
                    Log.d("SpeechRecognizer", "No results found.");
                    speak("I didn't catch that. Please try again.");
                }
            }

            // These methods are part of the RecognitionListener interface but are not used in this app.
            @Override
            public void onPartialResults(Bundle partialResults) {}
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        // Set a click listener for the voice button.
        btnVoice.setOnClickListener(view -> {
            // Check if the app has permission to record audio.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                // If not, request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_CODE);
            } else {
                // If permission is already granted, start listening for speech.
                speechRecognizer.startListening(speechRecognizerIntent);
            }
        });
    }

    // This method processes the recognized voice command.
    private void processVoiceCommand(String command) {
        // Convert the command to lowercase for easier matching.
        command = command.toLowerCase();
        // Check for keywords in the command and launch the corresponding activity.
        if (command.contains("add reminder") || command.contains("new reminder") || command.contains("medicine")) {
            speak("Opening medicine reminder screen.");
            startActivity(new Intent(this, MedicineActivity.class));
        } else if (command.contains("view reminders") || command.contains("show reminders") || command.contains("open reminders") || command.contains("reminder")) {
            speak("Opening your reminders now.");
            startActivity(new Intent(this, ViewRemindersActivity.class));
        } else if (command.contains("map") || command.contains("hospital") || command.contains("nearby hospital")) {
            speak("Opening the map now.");
            startActivity(new Intent(this, MapActivity.class));
        } else if (command.contains("sos") || command.contains("emergency") || command.contains("help")) {
            speak("Initiating SOS.");
            startActivity(new Intent(this, SOSActivity.class));
        } else if (command.contains("profile") || command.contains("my profile") || command.contains("user info")) {
            speak("Opening your profile.");
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (command.contains("health") || command.contains("log") || command.contains("health log")) {
            speak("Opening your health log.");
            startActivity(new Intent(this, HealthLogActivity.class));
        } else {
            // If no keywords are matched, let the user know.
            speak("Sorry, I didn't understand that command.");
        }
    }

    // This method speaks the given text using TextToSpeech.
    private void speak(String text) {
        // Check the Android version to use the correct speak method.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // For Android 5.0 and above.
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            // For older versions.
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    // This method is called when the TextToSpeech engine is initialized.
    @Override
    public void onInit(int status) {
        // If initialization is successful, set the language.
        if (status == TextToSpeech.SUCCESS) {
            // Set the language to US English.
            int result = textToSpeech.setLanguage(Locale.US);
            // If the language is not supported, show a toast message.
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
            }
        } else {
            // If initialization fails, show a toast message.
            Toast.makeText(this, "Initialization failed", Toast.LENGTH_SHORT).show();
        }
    }

    // This method is called when the activity is being destroyed.
    @Override
    protected void onDestroy() {
        // Release the resources of the TextToSpeech and SpeechRecognizer objects.
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        // Call the superclass method.
        super.onDestroy();
    }

    // This method is called when the user responds to a permission request.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Call the superclass method.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check if the permission request is for audio recording.
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            // If the permission is granted, let the user know.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted. Tap the voice button again.", Toast.LENGTH_SHORT).show();
            } else {
                // If the permission is denied, let the user know.
                Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // This method converts SpeechRecognizer error codes into user-friendly strings.
    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "Recognizer busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "Error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
}
