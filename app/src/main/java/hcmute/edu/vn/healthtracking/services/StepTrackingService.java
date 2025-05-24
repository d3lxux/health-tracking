package hcmute.edu.vn.healthtracking.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.healthtracking.R;
import hcmute.edu.vn.healthtracking.database.DatabaseHelper;
import hcmute.edu.vn.healthtracking.models.Exercise;
import hcmute.edu.vn.healthtracking.models.UserProfile;
import hcmute.edu.vn.healthtracking.utils.ExerciseUtils;

public class StepTrackingService extends Service implements SensorEventListener {

    private static final String CHANNEL_ID = "StepTrackingChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final String ACTION_UPDATE_UI = "hcmute.edu.vn.healthtracking.ACTION_UPDATE_UI";

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private DatabaseHelper dbHelper;
    private UserProfile userProfile;
    private long initialStepCount = -1;
    private long lastStepCount = -1;
    private Date currentDate;
    private String currentDateStr;
    private long startTimeMillis;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("StepTrackingService", "Service created");

        // Check ACTIVITY_RECOGNITION permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("StepTrackingService", "ACTIVITY_RECOGNITION permission not granted");
            stopSelf();
            return;
        }

        // Initialize database and user profile
        dbHelper = new DatabaseHelper(this);
        userProfile = dbHelper.getUserProfile();
        if (userProfile == null) {
            userProfile = new UserProfile("Default User", 30, 170.0f, 70.0f, null);
            dbHelper.saveUserProfile(userProfile.getName(), userProfile.getAge(),
                    userProfile.getHeight(), userProfile.getWeight(), null);
        }

        // Initialize sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepCounterSensor == null) {
            Log.e("StepTrackingService", "Step counter sensor not available on this device");
            logAvailableSensors();
            stopSelf();
            return;
        } else {
            Log.d("StepTrackingService", "Step counter sensor found: " + stepCounterSensor.getName());
        }

        // Register sensor listener
        registerSensorListener();

        // Initialize date and start time
        currentDate = new Date();
        currentDateStr = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentDate);
        startTimeMillis = System.currentTimeMillis();

        // Start foreground service
        startForegroundService();

        // Simulate steps for testing (uncomment for debugging)
        // simulateStepsForTesting(100);
    }

    private void registerSensorListener() {
        if (stepCounterSensor != null) {
            boolean registered = sensorManager.registerListener(this, stepCounterSensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
            if (registered) {
                Log.d("StepTrackingService", "Successfully registered step counter sensor listener");
            } else {
                Log.e("StepTrackingService", "Failed to register step counter sensor listener");
                stopSelf();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("StepTrackingService", "Service started");
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_STEP_COUNTER) {
            Log.w("StepTrackingService", "Received event from unexpected sensor: " + event.sensor.getType());
            return;
        }

        // Log raw sensor data
        long currentStepCount = (long) event.values[0];
        Log.d("StepTrackingService", "Sensor event - Timestamp: " + event.timestamp +
                ", Accuracy: " + event.accuracy + ", Raw steps: " + currentStepCount);

        // Initialize initialStepCount on first event
        if (initialStepCount == -1) {
            initialStepCount = currentStepCount;
            lastStepCount = currentStepCount;
            Log.d("StepTrackingService", "Initialized initialStepCount: " + initialStepCount);
            return;
        }

        // Check for date change
        String newDateStr = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        if (!newDateStr.equals(currentDateStr)) {
            Log.d("StepTrackingService", "Date changed to " + newDateStr + ", resetting steps");
            currentDate = new Date();
            currentDateStr = newDateStr;
            initialStepCount = currentStepCount;
            lastStepCount = currentStepCount;
            startTimeMillis = System.currentTimeMillis();
            return;
        }

        // Calculate steps taken since last update
        int stepsTaken = (int) (currentStepCount - lastStepCount);
        if (stepsTaken < 0) {
            // Handle reset (e.g., device reboot)
            Log.w("StepTrackingService", "Step count reset detected, using current count");
            stepsTaken = (int) (currentStepCount - initialStepCount);
            initialStepCount = currentStepCount;
        }
        lastStepCount = currentStepCount;

        if (stepsTaken <= 0) {
            Log.d("StepTrackingService", "No new steps detected (stepsTaken: " + stepsTaken + ")");
            return;
        }

        Log.d("StepTrackingService", "Steps taken: " + stepsTaken);

        // Calculate metrics
        double distance = stepsTaken / 1100.0; // km
        long currentTime = System.currentTimeMillis();
        long duration = currentTime - startTimeMillis;

        // Update or create exercise record
        Exercise existingExercise = dbHelper.getWalkingExerciseByDate(currentDateStr);
        Exercise exercise;
        if (existingExercise != null) {
            exercise = existingExercise;
            exercise.setSteps(exercise.getSteps() + stepsTaken);
            exercise.setDistance(exercise.getDistance() + distance);
            exercise.setDuration(exercise.getDuration() + duration);
            exercise.setEndTime(new Date(currentTime));
            Log.d("StepTrackingService", "Updated exercise - Steps: " + exercise.getSteps() +
                    ", Distance: " + exercise.getDistance() + " km");
        } else {
            exercise = new Exercise(
                    "user1",
                    "WALKING",
                    new Date(startTimeMillis),
                    new Date(currentTime),
                    currentDate,
                    distance,
                    stepsTaken
            );
            exercise.setDuration(duration);
            Log.d("StepTrackingService", "Created exercise - Steps: " + stepsTaken +
                    ", Distance: " + distance + " km");
        }

        // Calculate calories
        exercise.setCaloriesBurned(ExerciseUtils.calculateCaloriesWithUserProfile(exercise, userProfile));

        // Save to database
        long exerciseId = dbHelper.addOrUpdateWalkingExercise(exercise);
        Log.d("StepTrackingService", "Saved exercise ID: " + exerciseId + ", Steps: " + exercise.getSteps());

        // Broadcast UI update
        Intent intent = new Intent(ACTION_UPDATE_UI);
        sendBroadcast(intent);
        Log.d("StepTrackingService", "Broadcast sent for UI update");

        // Update start time
        startTimeMillis = currentTime;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("StepTrackingService", "Sensor accuracy changed: " + accuracy);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            Log.d("StepTrackingService", "Unregistered sensor listener");
        }
        dbHelper.close();
        Log.d("StepTrackingService", "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startForegroundService() {
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Step Tracking")
                .setContentText("Tracking your steps in the background")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
        startForeground(NOTIFICATION_ID, notification);
        Log.d("StepTrackingService", "Foreground service started");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Step Tracking Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            Log.d("StepTrackingService", "Notification channel created");
        }
    }

    private void logAvailableSensors() {
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        Log.d("StepTrackingService", "Available sensors:");
        for (Sensor sensor : sensorList) {
            Log.d("StepTrackingService", " - " + sensor.getName() + " (Type: " + sensor.getType() + ")");
        }
    }

    private void simulateStepsForTesting(int steps) {
        Log.d("StepTrackingService", "Simulating " + steps + " steps");
        long currentStepCount = lastStepCount == -1 ? steps : lastStepCount + steps;
        // Simulate onSensorChanged logic
        processStepCount(currentStepCount);
    }

    private void processStepCount(long currentStepCount) {
        Log.d("StepTrackingService", "Processing step count: " + currentStepCount);

        // Initialize initialStepCount if not set
        if (initialStepCount == -1) {
            initialStepCount = currentStepCount;
            lastStepCount = currentStepCount;
            Log.d("StepTrackingService", "Initialized initialStepCount: " + initialStepCount);
            return;
        }

        // Check for date change
        String newDateStr = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        if (!newDateStr.equals(currentDateStr)) {
            Log.d("StepTrackingService", "Date changed to " + newDateStr + ", resetting steps");
            currentDate = new Date();
            currentDateStr = newDateStr;
            initialStepCount = currentStepCount;
            lastStepCount = currentStepCount;
            startTimeMillis = System.currentTimeMillis();
            return;
        }

        // Calculate steps taken
        int stepsTaken = (int) (currentStepCount - lastStepCount);
        if (stepsTaken < 0) {
            Log.w("StepTrackingService", "Step count reset detected, using current count");
            stepsTaken = (int) (currentStepCount - initialStepCount);
            initialStepCount = currentStepCount;
        }
        lastStepCount = currentStepCount;

        if (stepsTaken <= 0) {
            Log.d("StepTrackingService", "No new steps detected (stepsTaken: " + stepsTaken + ")");
            return;
        }

        Log.d("StepTrackingService", "Steps taken: " + stepsTaken);

        // Calculate metrics
        double distance = stepsTaken / 1100.0; // km
        long currentTime = System.currentTimeMillis();
        long duration = currentTime - startTimeMillis;

        // Update or create exercise
        Exercise existingExercise = dbHelper.getWalkingExerciseByDate(currentDateStr);
        Exercise exercise;
        if (existingExercise != null) {
            exercise = existingExercise;
            exercise.setSteps(exercise.getSteps() + stepsTaken);
            exercise.setDistance(exercise.getDistance() + distance);
            exercise.setDuration(exercise.getDuration() + duration);
            exercise.setEndTime(new Date(currentTime));
            Log.d("StepTrackingService", "Updated exercise - Steps: " + exercise.getSteps() +
                    ", Distance: " + exercise.getDistance() + " km");
        } else {
            exercise = new Exercise(
                    "user1",
                    "WALKING",
                    new Date(startTimeMillis),
                    new Date(currentTime),
                    currentDate,
                    distance,
                    stepsTaken
            );
            exercise.setDuration(duration);
            Log.d("StepTrackingService", "Created exercise - Steps: " + stepsTaken +
                    ", Distance: " + distance + " km");
        }

        // Calculate calories
        exercise.setCaloriesBurned(ExerciseUtils.calculateCaloriesWithUserProfile(exercise, userProfile));

        // Save to database
        long exerciseId = dbHelper.addOrUpdateWalkingExercise(exercise);
        Log.d("StepTrackingService", "Saved exercise ID: " + exerciseId + ", Steps: " + exercise.getSteps());

        // Broadcast UI update
        Intent intent = new Intent(ACTION_UPDATE_UI);
        sendBroadcast(intent);
        Log.d("StepTrackingService", "Broadcast sent for UI update");

        // Update start time
        startTimeMillis = currentTime;
    }
}