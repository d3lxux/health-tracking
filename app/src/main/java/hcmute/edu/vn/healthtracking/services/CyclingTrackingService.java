package hcmute.edu.vn.healthtracking.services;

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

public class CyclingTrackingService extends Service implements LocationListener {

    private static final String CHANNEL_ID = "CyclingTrackingChannel";
    private static final int NOTIFICATION_ID = 3;
    private static final String TAG = "CyclingTrackingService";
    
    // Actions
    public static final String ACTION_START_CYCLING = "START_CYCLING";
    public static final String ACTION_STOP_CYCLING = "STOP_CYCLING";
    public static final String ACTION_REQUEST_STATUS = "REQUEST_STATUS";
    public static final String ACTION_UPDATE_CYCLING_UI = "hcmute.edu.vn.healthtracking.ACTION_UPDATE_CYCLING_UI";
    
    // Intent extras for broadcasting
    public static final String EXTRA_DISTANCE = "EXTRA_DISTANCE";
    public static final String EXTRA_DURATION = "EXTRA_DURATION";
    public static final String EXTRA_CALORIES = "EXTRA_CALORIES";
    public static final String EXTRA_AVG_SPEED = "EXTRA_AVG_SPEED";
    public static final String EXTRA_MAX_SPEED = "EXTRA_MAX_SPEED";
    public static final String EXTRA_IS_CYCLING = "EXTRA_IS_CYCLING";

    private LocationManager locationManager;
    private DatabaseHelper dbHelper;
    private UserProfile userProfile;
    
    // Tracking variables
    private boolean isTracking = false;
    private long startTimeMillis;
    private double totalDistance = 0.0; // kilometers
    private double maxSpeed = 0.0; // km/h
    private Location lastLocation;
    private Handler timerHandler;
    private Runnable timerRunnable;
    
    // GPS noise filtering variables
    private long lastMaxSpeedUpdateTime = 0;
    private static final long MAX_SPEED_UPDATE_INTERVAL = 3000; // 3 seconds
    private static final double MIN_SPEED_THRESHOLD = 2.0; // 2 km/h minimum speed to register movement
    private static final double MAX_REASONABLE_SPEED = 60.0; // 60 km/h maximum reasonable cycling speed
    
    // Minimum distance between updates (meters)
    private static final float MIN_DISTANCE_CHANGE = 2.0f;
    // Minimum time between updates (milliseconds)
    private static final long MIN_TIME_BETWEEN_UPDATES = 2000;

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
            userProfile = new UserProfile("Default User", 30, 170.0f, 70.0f, null, "male");
            dbHelper.saveUserProfile(userProfile.getName(), userProfile.getAge(),
                    userProfile.getHeight(), userProfile.getWeight(), null, userProfile.getGender());
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
            if (ACTION_START_CYCLING.equals(action)) {
                startCycling();
            } else if (ACTION_STOP_CYCLING.equals(action)) {
                stopCycling();
            } else if (ACTION_REQUEST_STATUS.equals(action)) {
                // Immediately broadcast current status
                broadcastUpdate();
                Log.d(TAG, "Status requested - broadcasting current state: isTracking=" + isTracking);
            }
        }
        
        return START_STICKY;
    }

    private void startCycling() {
        if (isTracking) {
            Log.d(TAG, "Already tracking");
            return;
        }

        Log.d(TAG, "Starting cycling tracking");
        isTracking = true;
        startTimeMillis = System.currentTimeMillis();
        totalDistance = 0.0;
        maxSpeed = 0.0;
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

    private void stopCycling() {
        if (!isTracking) {
            Log.d(TAG, "Not currently tracking");
            return;
        }

        Log.d(TAG, "Stopping cycling tracking");
        isTracking = false;

        // Stop location updates
        locationManager.removeUpdates(this);
        
        // Stop timer
        stopTimer();

        // Save to database
        saveCyclingSession();
        
        // Broadcast final state
        broadcastUpdate();
        
        // Stop foreground service
        stopForeground(true);
        stopSelf();
        Intent updateIntent = new Intent("hcmute.edu.vn.healthtracking.ACTION_UPDATE_UI");
        sendBroadcast(updateIntent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);
        Log.d("CyclingFragment4", "Sent ACTION_UPDATE_UI broadcast to refresh HomeFragment");
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

    private void saveCyclingSession() {
        if (totalDistance > 0.01) { // Only save if moved at least 10 meters
            long duration = System.currentTimeMillis() - startTimeMillis;
            Date startTime = new Date(startTimeMillis);
            Date endTime = new Date();

            // Create Exercise object
            Exercise exercise = new Exercise(
                    "user1",
                    "CYCLING",
                    startTime,
                    endTime,
                    new Date(),
                    totalDistance,
                    0 // No steps for cycling
            );

            // Set duration manually
            exercise.setDuration(duration);

            // Calculate calories using cycling MET value
            exercise.setCaloriesBurned(ExerciseUtils.calculateCaloriesWithUserProfile(exercise, userProfile));

            // Save to database
            long exerciseId = dbHelper.addExercise(exercise);
            Log.d(TAG, "Saved cycling session with ID: " + exerciseId +
                    ", Distance: " + totalDistance + "km, Duration: " + duration + "ms");
        } else {
            Log.d(TAG, "Cycling session too short, not saving");
        }
    }

    private void broadcastUpdate() {
        Intent intent = new Intent(ACTION_UPDATE_CYCLING_UI);
        intent.putExtra(EXTRA_DISTANCE, totalDistance);
        intent.putExtra(EXTRA_DURATION, isTracking ? System.currentTimeMillis() - startTimeMillis : 0);
        intent.putExtra(EXTRA_CALORIES, calculateCurrentCalories());
        intent.putExtra(EXTRA_AVG_SPEED, calculateAverageSpeed());
        intent.putExtra(EXTRA_MAX_SPEED, maxSpeed);
        intent.putExtra(EXTRA_IS_CYCLING, isTracking);
        
        long currentDuration = isTracking ? System.currentTimeMillis() - startTimeMillis : 0;
        int currentCalories = calculateCurrentCalories();
        double avgSpeed = calculateAverageSpeed();
        
        Log.d(TAG, "Broadcasting update - Distance: " + totalDistance + 
                ", Duration: " + currentDuration + ", Calories: " + currentCalories + 
                ", AvgSpeed: " + avgSpeed + ", MaxSpeed: " + maxSpeed + ", isTracking: " + isTracking);
        
        // Send both global and local broadcast for better compatibility
        sendBroadcast(intent);
        
        // Also try using LocalBroadcastManager
        try {
            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.d(TAG, "Local broadcast sent with action: " + ACTION_UPDATE_CYCLING_UI);
        } catch (Exception e) {
            Log.e(TAG, "Error sending local broadcast", e);
        }
        
        Log.d(TAG, "Global broadcast sent with action: " + ACTION_UPDATE_CYCLING_UI);
    }

    private int calculateCurrentCalories() {
        if (!isTracking || totalDistance <= 0) return 0;
        
        long currentDuration = System.currentTimeMillis() - startTimeMillis;
        double durationInHours = currentDuration / (1000.0 * 60 * 60);
        // Use cycling MET value (6.0)
        return (int) (6.0 * userProfile.getWeight() * durationInHours);
    }

    private double calculateAverageSpeed() {
        if (!isTracking || totalDistance <= 0) return 0.0;
        
        long currentDuration = System.currentTimeMillis() - startTimeMillis;
        double durationInHours = currentDuration / (1000.0 * 60 * 60);
        return durationInHours > 0 ? totalDistance / durationInHours : 0.0;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!isTracking) return;

        Log.d(TAG, "Location changed: " + location.getLatitude() + ", " + location.getLongitude() + 
                ", Accuracy: " + location.getAccuracy() + ", Speed: " + location.getSpeed());

        // Enhanced GPS noise filtering
        if (lastLocation != null) {
            float distance = lastLocation.distanceTo(location);
            long timeDiff = location.getTime() - lastLocation.getTime();
            
            // Calculate current speed from GPS data
            double currentSpeed = 0.0;
            if (location.hasSpeed() && location.getSpeed() >= 0) {
                currentSpeed = location.getSpeed() * 3.6; // Convert m/s to km/h
            } else if (timeDiff > 0) {
                // Calculate speed from distance and time if GPS speed not available
                double timeInHours = timeDiff / (1000.0 * 60 * 60);
                currentSpeed = (distance / 1000.0) / timeInHours;
            }
            
            // Apply speed filters
            boolean speedFilter = currentSpeed >= MIN_SPEED_THRESHOLD && currentSpeed <= MAX_REASONABLE_SPEED;
            
            // Filter out inaccurate readings with enhanced conditions
            if (location.getAccuracy() <= 20 && distance >= 2.0f && speedFilter && timeDiff > 1000) {
                totalDistance += distance / 1000.0; // Convert to kilometers
                Log.d(TAG, "Distance updated: +" + (distance/1000.0) + "km, Total: " + totalDistance + "km, Speed: " + currentSpeed + "km/h");
                
                // Update max speed with time restriction (only every 3 seconds)
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastMaxSpeedUpdateTime >= MAX_SPEED_UPDATE_INTERVAL) {
                    if (currentSpeed > maxSpeed) {
                        maxSpeed = currentSpeed;
                        lastMaxSpeedUpdateTime = currentTime;
                        Log.d(TAG, "New max speed: " + maxSpeed + " km/h");
                    }
                }
            } else {
                Log.d(TAG, "Location update filtered out - Accuracy: " + location.getAccuracy() + 
                        ", Distance: " + distance + "m, Speed: " + currentSpeed + "km/h, TimeDiff: " + timeDiff + "ms");
            }
        } else {
            // First location, just store it
            Log.d(TAG, "First location recorded");
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
            stopCycling();
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
                .setContentTitle("Đang theo dõi đạp xe")
                .setContentText("Khoảng cách: " + String.format("%.2f km", totalDistance))
                .setSmallIcon(R.drawable.ic_cycling) // Make sure this icon exists
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Cycling Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Theo dõi hoạt động đạp xe");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
} 