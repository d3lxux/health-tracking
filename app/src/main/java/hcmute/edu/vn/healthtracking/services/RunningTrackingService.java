package hcmute.edu.vn.healthtracking.services;

import static java.security.AccessController.getContext;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Date;

import hcmute.edu.vn.healthtracking.R;
import hcmute.edu.vn.healthtracking.database.DatabaseHelper;
import hcmute.edu.vn.healthtracking.models.Exercise;
import hcmute.edu.vn.healthtracking.models.UserProfile;
import hcmute.edu.vn.healthtracking.utils.ExerciseUtils;

public class RunningTrackingService extends Service implements LocationListener {

    private static final String CHANNEL_ID = "RunningTrackingChannel";
    private static final int NOTIFICATION_ID = 2;
    private static final String TAG = "RunningTrackingService";
    
    // Actions
    public static final String ACTION_START_RUNNING = "START_RUNNING";
    public static final String ACTION_STOP_RUNNING = "STOP_RUNNING";
    public static final String ACTION_UPDATE_RUNNING_UI = "hcmute.edu.vn.healthtracking.ACTION_UPDATE_RUNNING_UI";
    
    // Intent extras for broadcasting
    public static final String EXTRA_DISTANCE = "EXTRA_DISTANCE";
    public static final String EXTRA_DURATION = "EXTRA_DURATION";
    public static final String EXTRA_CALORIES = "EXTRA_CALORIES";
    public static final String EXTRA_IS_RUNNING = "EXTRA_IS_RUNNING";

    private LocationManager locationManager;
    private DatabaseHelper dbHelper;
    private UserProfile userProfile;
    
    // Tracking variables
    private boolean isTracking = false;
    private long startTimeMillis;
    private double totalDistance = 0.0; // kilometers
    private Location lastLocation;
    private Handler timerHandler;
    private Runnable timerRunnable;
    
    // Minimum distance between updates (meters)
    private static final float MIN_DISTANCE_CHANGE = 1.0f;
    // Minimum time between updates (milliseconds)
    private static final long MIN_TIME_BETWEEN_UPDATES = 1000;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");

        // Check location permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted");
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

        // Initialize location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        // Initialize timer
        timerHandler = new Handler(Looper.getMainLooper());
        
        // Create notification channel
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started with action: " + (intent != null ? intent.getAction() : "null"));
        
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_START_RUNNING.equals(action)) {
                startRunning();
            } else if (ACTION_STOP_RUNNING.equals(action)) {
                stopRunning();
            }
        }
        
        return START_STICKY;
    }

    private void startRunning() {
        if (isTracking) {
            Log.d(TAG, "Already tracking");
            return;
        }

        Log.d(TAG, "Starting running tracking");
        isTracking = true;
        startTimeMillis = System.currentTimeMillis();
        totalDistance = 0.0;
        lastLocation = null;

        // Start location updates
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BETWEEN_UPDATES,
                    MIN_DISTANCE_CHANGE,
                    this
            );
            
            // Also try network provider as backup
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BETWEEN_UPDATES,
                    MIN_DISTANCE_CHANGE,
                    this
            );
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission denied", e);
            stopSelf();
            return;
        }

        // Start timer updates
        startTimer();
        
        // Start foreground service
        startForegroundService();
        
        // Broadcast initial state
        broadcastUpdate();
    }

    private void stopRunning() {
        if (!isTracking) {
            Log.d(TAG, "Not currently tracking");
            return;
        }

        Log.d(TAG, "Stopping running tracking");
        isTracking = false;

        locationManager.removeUpdates(this);
        stopTimer();
        saveRunningSession();
        broadcastUpdate();

        stopForeground(true);
        stopSelf();

        // Send broadcast to update UI
        Intent updateIntent = new Intent("hcmute.edu.vn.healthtracking.ACTION_UPDATE_UI");
        sendBroadcast(updateIntent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);
        Log.d("RunningFragment", "Sent ACTION_UPDATE_UI broadcast to refresh HomeFragment");
    }
    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isTracking) {
                    broadcastUpdate();
                    timerHandler.postDelayed(this, 1000); // Update every second
                }
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void stopTimer() {
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    private void saveRunningSession() {
        if (totalDistance > 0.001) { // Only save if moved at least 1 meters
            long duration = System.currentTimeMillis() - startTimeMillis;
            Date startTime = new Date(startTimeMillis);
            Date endTime = new Date();

            // Create Exercise object using the basic constructor
            Exercise exercise = new Exercise(
                    "user1",
                    "RUNNING",
                    startTime,
                    endTime,
                    new Date(),
                    totalDistance,
                    (int) (totalDistance * 1100) // steps approximation
            );

            // Set duration manually since constructor calculates it differently
            exercise.setDuration(duration);

            // Calculate calories
            exercise.setCaloriesBurned(ExerciseUtils.calculateCaloriesWithUserProfile(exercise, userProfile));

            // Save to database (this will create a new session, not update existing)
            long exerciseId = dbHelper.addExercise(exercise);
            Log.d(TAG, "Saved running session with ID: " + exerciseId +
                    ", Distance: " + totalDistance + "km, Duration: " + duration + "ms");
        } else {
            Log.d(TAG, "Running session too short, not saving");
        }
    }

    private void broadcastUpdate() {
        Intent intent = new Intent(ACTION_UPDATE_RUNNING_UI);
        intent.putExtra(EXTRA_DISTANCE, totalDistance);
        intent.putExtra(EXTRA_DURATION, isTracking ? System.currentTimeMillis() - startTimeMillis : 0);
        intent.putExtra(EXTRA_CALORIES, calculateCurrentCalories());
        intent.putExtra(EXTRA_IS_RUNNING, isTracking);
        
        long currentDuration = isTracking ? System.currentTimeMillis() - startTimeMillis : 0;
        int currentCalories = calculateCurrentCalories();
        
        Log.d(TAG, "Broadcasting update - Distance: " + totalDistance + 
                ", Duration: " + currentDuration + ", Calories: " + currentCalories + ", isTracking: " + isTracking);
        
        // Send both global and local broadcast for better compatibility
        sendBroadcast(intent);
        
        // Also try using LocalBroadcastManager
        try {
            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.d(TAG, "Local broadcast sent with action: " + ACTION_UPDATE_RUNNING_UI);
        } catch (Exception e) {
            Log.e(TAG, "Error sending local broadcast", e);
        }
        
        Log.d(TAG, "Global broadcast sent with action: " + ACTION_UPDATE_RUNNING_UI);
    }

    private int calculateCurrentCalories() {
        if (!isTracking || totalDistance <= 0) return 0;
        
        long currentDuration = System.currentTimeMillis() - startTimeMillis;
        double durationInHours = currentDuration / (1000.0 * 60 * 60);
        return (int) (ExerciseUtils.RUNNING_MET * userProfile.getWeight() * durationInHours);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!isTracking) return;

        Log.d(TAG, "Location changed: " + location.getLatitude() + ", " + location.getLongitude() + 
                ", Accuracy: " + location.getAccuracy());

        if (lastLocation != null) {
            float distance = lastLocation.distanceTo(location);
            
            // Filter out inaccurate readings
            if (location.getAccuracy() <= 20 && distance >= 1.0f) {
                totalDistance += distance / 1000.0; // Convert to kilometers
                Log.d(TAG, "Distance updated: +" + (distance/1000.0) + "km, Total: " + totalDistance + "km");
            }
        }
        
        lastLocation = location;
        broadcastUpdate();
    }

    @Override
    public void onStatusChanged(String provider, int status, android.os.Bundle extras) {
        Log.d(TAG, "Provider status changed: " + provider + " = " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "Provider enabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "Provider disabled: " + provider);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        
        if (isTracking) {
            stopRunning();
        }
        
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        
        stopTimer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startForegroundService() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Đang theo dõi chạy bộ")
                .setContentText("Khoảng cách: " + String.format("%.2f km", totalDistance))
                .setSmallIcon(R.drawable.ic_running) // Make sure this icon exists
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Running Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Theo dõi hoạt động chạy bộ");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
} 