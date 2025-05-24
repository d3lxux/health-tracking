package hcmute.edu.vn.healthtracking.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.healthtracking.R;
import hcmute.edu.vn.healthtracking.adapters.WorkoutHistoryAdapter;
import hcmute.edu.vn.healthtracking.database.DatabaseHelper;
import hcmute.edu.vn.healthtracking.models.Exercise;
import hcmute.edu.vn.healthtracking.models.WorkoutHistory;
import hcmute.edu.vn.healthtracking.services.CyclingTrackingService;
import hcmute.edu.vn.healthtracking.utils.ExerciseUtils;

public class CyclingFragment extends Fragment {

    private static final String TAG = "CyclingFragment";

    // UI Elements
    private RecyclerView recentRidesRecycler;
    private WorkoutHistoryAdapter adapter;
    private List<WorkoutHistory> cyclingHistory;
    private MaterialButton startCyclingButton;
    
    // Stats TextViews
    private TextView totalDistanceText;
    private TextView totalTimeText;
    private TextView totalCaloriesText;
    private TextView avgSpeedText;
    private TextView maxSpeedText;

    // Database
    private DatabaseHelper dbHelper;
    
    // Tracking state
    private boolean isTracking = false;

    // Broadcast receiver for real-time updates
    private BroadcastReceiver cyclingUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received broadcast: " + intent.getAction());
            
            if (CyclingTrackingService.ACTION_UPDATE_CYCLING_UI.equals(intent.getAction())) {
                double distance = intent.getDoubleExtra(CyclingTrackingService.EXTRA_DISTANCE, 0.0);
                long duration = intent.getLongExtra(CyclingTrackingService.EXTRA_DURATION, 0);
                int calories = intent.getIntExtra(CyclingTrackingService.EXTRA_CALORIES, 0);
                double avgSpeed = intent.getDoubleExtra(CyclingTrackingService.EXTRA_AVG_SPEED, 0.0);
                double maxSpeed = intent.getDoubleExtra(CyclingTrackingService.EXTRA_MAX_SPEED, 0.0);
                boolean isCycling = intent.getBooleanExtra(CyclingTrackingService.EXTRA_IS_CYCLING, false);
                
                Log.d(TAG, "Update received - Distance: " + distance + ", Duration: " + duration + 
                        ", Calories: " + calories + ", AvgSpeed: " + avgSpeed + ", MaxSpeed: " + maxSpeed + 
                        ", isTracking: " + isCycling);
                
                updateUI(distance, duration, calories, avgSpeed, maxSpeed, isCycling);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cycling, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize database
        dbHelper = new DatabaseHelper(getContext());

        // Initialize UI elements
        initializeViews(view);
        
        // Load actual cycling history from database
        loadCyclingHistoryFromDatabase();
        
        // Initialize RecyclerView for recent rides
        setupRecyclerView();
        
        // Setup click listener for start cycling button
        setupStartButton();
    }

    private void initializeViews(View view) {
        // Stats TextViews
        totalDistanceText = view.findViewById(R.id.total_distance);
        totalTimeText = view.findViewById(R.id.total_time);
        totalCaloriesText = view.findViewById(R.id.total_calories);
        avgSpeedText = view.findViewById(R.id.avg_speed);
        maxSpeedText = view.findViewById(R.id.max_speed);
        
        // Button
        startCyclingButton = view.findViewById(R.id.start_cycling_button);
        
        // RecyclerView
        recentRidesRecycler = view.findViewById(R.id.recent_rides_recycler);
    }

    private void setupRecyclerView() {
        recentRidesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WorkoutHistoryAdapter(getContext(), cyclingHistory, false);
        recentRidesRecycler.setAdapter(adapter);
    }

    private void setupStartButton() {
        startCyclingButton.setOnClickListener(v -> {
            if (!isTracking) {
                startCyclingTracking();
            } else {
                stopCyclingTracking();
            }
        });
    }

    private void startCyclingTracking() {
        Log.d(TAG, "Starting cycling tracking");
        
        Intent serviceIntent = new Intent(getContext(), CyclingTrackingService.class);
        serviceIntent.setAction(CyclingTrackingService.ACTION_START_CYCLING);
        
        if (getContext() != null) {
            getContext().startService(serviceIntent);
            isTracking = true;
            startCyclingButton.setText("Dừng đạp xe");
            startCyclingButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }
    
    private void stopCyclingTracking() {
        Log.d(TAG, "Stopping cycling tracking");
        
        Intent serviceIntent = new Intent(getContext(), CyclingTrackingService.class);
        serviceIntent.setAction(CyclingTrackingService.ACTION_STOP_CYCLING);
        
        if (getContext() != null) {
            getContext().startService(serviceIntent);
            isTracking = false;
            startCyclingButton.setText("Bắt đầu đạp xe");
            startCyclingButton.setBackgroundColor(getResources().getColor(R.color.primary_blue));
            
            // Reload history after session ends
            loadCyclingHistoryFromDatabase();
        }
    }

    private void updateUI(double distance, long duration, int calories, double avgSpeed, double maxSpeed, boolean isCycling) {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            // Update distance
            totalDistanceText.setText(String.format(Locale.getDefault(), "%.2f", distance));
            
            // Update time
            totalTimeText.setText(ExerciseUtils.formatDuration(duration));
            
            // Update calories
            totalCaloriesText.setText(String.valueOf(calories));
            
            // Update speeds
            avgSpeedText.setText(String.format(Locale.getDefault(), "%.1f", avgSpeed));
            maxSpeedText.setText(String.format(Locale.getDefault(), "%.1f", maxSpeed));
            
            // Update button state
            isTracking = isCycling;
            if (isTracking) {
                startCyclingButton.setText("Dừng đạp xe");
                startCyclingButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                startCyclingButton.setText("Bắt đầu đạp xe");
                startCyclingButton.setBackgroundColor(getResources().getColor(R.color.primary_blue));
            }
            
            Log.d(TAG, "UI updated - Distance: " + distance + "km, Duration: " + 
                    ExerciseUtils.formatDuration(duration) + ", Calories: " + calories);
        });
    }

    private void loadCyclingHistoryFromDatabase() {
        // Initialize data using the same pattern as RunningFragment
        cyclingHistory = getCyclingHistoryData();
        
        // Update adapter if it exists
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // Get cycling history data (similar to getRunningHistoryData())
    private List<WorkoutHistory> getCyclingHistoryData() {
        List<WorkoutHistory> history = new ArrayList<>();

        // Get cycling exercises from database
        List<Exercise> cyclingExercises = dbHelper.getAllExercises();
        
        // Filter and convert cycling exercises to WorkoutHistory
        for (Exercise exercise : cyclingExercises) {
            if ("CYCLING".equalsIgnoreCase(exercise.getExerciseType())) {
                WorkoutHistory workoutHistory = convertExerciseToWorkoutHistory(exercise);
                if (workoutHistory != null) {
                    history.add(workoutHistory);
                }
            }
        }

        Log.d(TAG, "Loaded " + history.size() + " cycling sessions from database");

        // If no real data, add sample data
        if (history.isEmpty()) {
            history.addAll(getSampleCyclingData());
        }
        
        return history;
    }
    
    // Helper method to convert Exercise to WorkoutHistory (similar to RunningFragment)
    private WorkoutHistory convertExerciseToWorkoutHistory(Exercise exercise) {
        if (exercise == null) {
            return null;
        }
        
        // Format title based on time of day
        String title = "Đạp xe";
        if (exercise.getStartTime() != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(exercise.getStartTime());
            int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
            
            if (hour >= 5 && hour < 12) {
                title = "Đạp xe buổi sáng";
            } else if (hour >= 12 && hour < 17) {
                title = "Đạp xe buổi chiều";
            } else {
                title = "Đạp xe buổi tối";
            }
        }
        
        // Format date and time
        String dateTime = "";
        if (exercise.getStartTime() != null) {
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault());
            dateTime = dateFormat.format(exercise.getStartTime());
        } else if (exercise.getDate() != null) {
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dateTime = dateFormat.format(exercise.getDate());
        }
        
        // Format duration using ExerciseUtils
        String duration = ExerciseUtils.formatDuration(exercise.getDuration());
        
        return new WorkoutHistory(
                title,
                dateTime,
                exercise.getDistance(),
                duration,
                exercise.getCaloriesBurned()
        );
    }

    // Keep sample data for demo purposes
    private List<WorkoutHistory> getSampleCyclingData() {
        List<WorkoutHistory> history = new ArrayList<>();
        
        history.add(new WorkoutHistory("Đạp xe buổi sáng", "24/08/2023, 07:15", 12.5, "00:45:30", 450));
        history.add(new WorkoutHistory("Đạp xe đường dài", "22/08/2023, 16:00", 18.3, "01:10:20", 680));
        history.add(new WorkoutHistory("Đạp xe quanh thành phố", "20/08/2023, 08:30", 15.7, "00:58:45", 590));
        history.add(new WorkoutHistory("Đạp xe ngoại ô", "18/08/2023, 17:30", 20.2, "01:25:15", 780));
        
        return history;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - Registering broadcast receiver");
        
        // Register broadcast receiver for real-time updates
        IntentFilter filter = new IntentFilter(CyclingTrackingService.ACTION_UPDATE_CYCLING_UI);
        
        // Register both global and local broadcast receivers
        if (getContext() != null) {
            ContextCompat.registerReceiver(getContext(), cyclingUpdateReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(cyclingUpdateReceiver, filter);
        }
        
        // Reload data when resuming
        loadCyclingHistoryFromDatabase();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause - Unregistering broadcast receiver");
        
        // Unregister broadcast receiver
        if (getContext() != null) {
            try {
                getContext().unregisterReceiver(cyclingUpdateReceiver);
                LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(cyclingUpdateReceiver);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Receiver not registered", e);
            }
        }
    }
} 