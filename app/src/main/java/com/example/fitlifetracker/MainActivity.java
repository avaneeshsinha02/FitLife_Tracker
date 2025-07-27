package com.example.fitlifetracker;

import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.example.fitlifetracker.auth.LoginActivity;
import com.example.fitlifetracker.services.TrackingService;
import com.example.fitlifetracker.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int SPEECH_REQUEST_CODE = 101;
    private static final int PERMISSION_REQUEST_CODE = 102;
    private static final String NOTIFICATION_CHANNEL_ID = "fitlife_channel";
    private TextView stepsValueText, caloriesValueText, distanceValueText;
    private ProgressBar progressBar;
    private ImageButton toggleTrackingButton, micButton;
    private boolean isTracking = false;
    private int steps = 0;
    private int lastMilestone = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SessionManager sessionManager;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        setupToolbar();
        bindViews();
        setupListeners();
        checkPermissions();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void bindViews() {
        stepsValueText = findViewById(R.id.textView_steps_value);
        caloriesValueText = findViewById(R.id.textView_calories_value);
        distanceValueText = findViewById(R.id.textView_distance_value);
        progressBar = findViewById(R.id.progressBar);
        toggleTrackingButton = findViewById(R.id.button_toggle_tracking);
        micButton = findViewById(R.id.button_mic);
    }

    private void setupListeners() {
        toggleTrackingButton.setOnClickListener(v -> toggleTracking());
        micButton.setOnClickListener(v -> startSpeechRecognition());
        findViewById(R.id.button_summary).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SummaryActivity.class)));
    }

    private void toggleTracking() {
        isTracking = !isTracking;
        animateToggleButton();
        if (isTracking) {
            startTracking();
            startService(new Intent(this, TrackingService.class));
            showReminderNotification();
        } else {
            stopTracking();
            stopService(new Intent(this, TrackingService.class));
        }
    }

    private void animateToggleButton() {
        toggleTrackingButton.animate().scaleX(0.8f).scaleY(0.8f).setDuration(150).withEndAction(() -> {
            toggleTrackingButton.setImageResource(isTracking ? R.drawable.ic_pause : R.drawable.ic_play);
            toggleTrackingButton.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
        }).start();
    }

    private final Runnable stepSimulator = new Runnable() {
        @Override
        public void run() {
            if (isTracking) {
                steps += (int) (Math.random() * 5) + 1;
                updateUI();
                handler.postDelayed(this, 1000);
            }
        }
    };

    private void startTracking() {
        handler.post(stepSimulator);
    }

    private void stopTracking() {
        handler.removeCallbacks(stepSimulator);
    }

    private void updateUI() {
        stepsValueText.setText(String.valueOf(steps));
        ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", steps);
        animator.setDuration(500);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();

        double calories = steps * 0.04;
        double distance = steps * 0.000762; // in km
        caloriesValueText.setText(String.format(Locale.US, "%.0f kcal", calories));
        distanceValueText.setText(String.format(Locale.US, "%.2f km", distance));

        if (steps / 1000 > lastMilestone) {
            lastMilestone = steps / 1000;
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            }
            Toast.makeText(this, "🎉 Milestone! " + (lastMilestone * 1000) + " steps!", Toast.LENGTH_SHORT).show();
        }
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak 'start' or 'stop' tracking");

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Speech recognition not supported on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String command = results.get(0).toLowerCase();
                if (command.contains("start") && !isTracking) {
                    toggleTracking();
                    Toast.makeText(this, "Started tracking via voice command.", Toast.LENGTH_SHORT).show();
                } else if (command.contains("stop") && isTracking) {
                    toggleTracking();
                    Toast.makeText(this, "Stopped tracking via voice command.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showReminderNotification() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_calories)
                .setContentTitle("Stay Active!")
                .setContentText("Time for a walk to reach your goal.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

    private void checkPermissions() {
        String[] permissions = {android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.POST_NOTIFICATIONS};
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            sessionManager.logoutUser();
            stopService(new Intent(this, TrackingService.class));
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}