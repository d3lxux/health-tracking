package hcmute.edu.vn.healthtracking.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.healthtracking.R;
import hcmute.edu.vn.healthtracking.adapters.WorkoutHistoryAdapter;
import hcmute.edu.vn.healthtracking.database.DatabaseHelper;
import hcmute.edu.vn.healthtracking.models.Exercise;
import hcmute.edu.vn.healthtracking.models.WorkoutHistory;
import hcmute.edu.vn.healthtracking.services.RunningTrackingService;
import hcmute.edu.vn.healthtracking.utils.ExerciseUtils;

public class RunningFragment extends Fragment {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    // UI Components
    private RecyclerView recentRunsRecycler;
    private WorkoutHistoryAdapter adapter;
    private List<WorkoutHistory> runningHistory;
    
    // Realtime stats UI
    private TextView totalDistanceText;
    private TextView totalTimeText;
    private TextView totalCaloriesText;
    private MaterialButton startRunButton;
    
    // Data
    private DatabaseHelper dbHelper;
    private boolean isTracking = false;
    
    // BroadcastReceiver for realtime updates
    private BroadcastReceiver runningUpdateReceiver;
    
    // Local timer for testing - remove this later when broadcast works
    private Handler localTimerHandler;
    private Runnable localTimerRunnable;
    private long localStartTime;
    private boolean receivingBroadcasts = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_running, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize database
        dbHelper = new DatabaseHelper(requireContext());
        
        // Initialize UI components
        initializeUI(view);
        
        // Initialize data
        runningHistory = getRunningHistoryData();
        
        // Set up RecyclerView for recent runs
        setupRecyclerView();
        
        // Note: BroadcastReceiver will be setup in onResume()
        
        // Check permissions
        checkLocationPermission();
    }

    @Override
    public void onResume() {
        super.onResume();
        android.util.Log.d("RunningFragment", "onResume() called");
        
        // Setup broadcast receiver every time fragment becomes active
        setupBroadcastReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        android.util.Log.d("RunningFragment", "onPause() called");
        
        // Unregister receiver when fragment becomes inactive
        if (runningUpdateReceiver != null) {
            try {
                // Unregister from global broadcasts
                requireContext().unregisterReceiver(runningUpdateReceiver);
                android.util.Log.d("RunningFragment", "Global BroadcastReceiver unregistered in onPause");
                
                // Unregister from local broadcasts
                androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(requireContext())
                    .unregisterReceiver(runningUpdateReceiver);
                android.util.Log.d("RunningFragment", "Local BroadcastReceiver unregistered in onPause");
                
            } catch (Exception e) {
                android.util.Log.e("RunningFragment", "Error unregistering receiver in onPause", e);
            }
            runningUpdateReceiver = null;
        }
    }

    private void initializeUI(View view) {
        // Stats UI
        totalDistanceText = view.findViewById(R.id.total_distance);
        totalTimeText = view.findViewById(R.id.total_time);
        totalCaloriesText = view.findViewById(R.id.total_calories);
        startRunButton = view.findViewById(R.id.start_run_button);
        
        // RecyclerView
        recentRunsRecycler = view.findViewById(R.id.recent_runs_recycler);
        
        // Button click listener
        startRunButton.setOnClickListener(v -> toggleRunTracking());
        
        // Reset UI to initial state
        resetUI();
    }

    private void setupRecyclerView() {
        recentRunsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WorkoutHistoryAdapter(getContext(), runningHistory, true);
        recentRunsRecycler.setAdapter(adapter);
    }

    private void setupBroadcastReceiver() {
        // Don't register if already registered
        if (runningUpdateReceiver != null) {
            android.util.Log.d("RunningFragment", "BroadcastReceiver already registered, skipping");
            return;
        }
        
        runningUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                android.util.Log.d("RunningFragment", "Broadcast received: " + intent.getAction());
                updateRealtimeUI(intent);
            }
        };
        
        IntentFilter filter = new IntentFilter(RunningTrackingService.ACTION_UPDATE_RUNNING_UI);
        android.util.Log.d("RunningFragment", "Registering broadcast receiver for: " + RunningTrackingService.ACTION_UPDATE_RUNNING_UI);
        
        try {
            // Register for global broadcasts
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireContext().registerReceiver(runningUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                requireContext().registerReceiver(runningUpdateReceiver, filter);
            }
            
            // Also register for local broadcasts
            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(runningUpdateReceiver, filter);
            
            android.util.Log.d("RunningFragment", "BroadcastReceiver registered successfully (both global and local)");
        } catch (Exception e) {
            android.util.Log.e("RunningFragment", "Error registering BroadcastReceiver", e);
            runningUpdateReceiver = null;
        }
    }

    private void toggleRunTracking() {
        if (!isTracking) {
            startRunTracking();
        } else {
            stopRunTracking();
        }
    }

    private void startRunTracking() {
        if (!hasLocationPermission()) {
            requestLocationPermission();
            return;
        }

        // Start the running tracking service
        Intent serviceIntent = new Intent(requireContext(), RunningTrackingService.class);
        serviceIntent.setAction(RunningTrackingService.ACTION_START_RUNNING);
        requireContext().startService(serviceIntent);
        
        // Update UI state
        isTracking = true;
        startRunButton.setText("Dừng chạy");
        startRunButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), android.R.color.holo_red_dark));
        
        Toast.makeText(getContext(), "Bắt đầu theo dõi chạy bộ", Toast.LENGTH_SHORT).show();
        
        // Start local timer for testing (fallback if broadcast doesn't work)
        startLocalTimer();
    }

    private void stopRunTracking() {
        // Stop the running tracking service
        Intent serviceIntent = new Intent(requireContext(), RunningTrackingService.class);
        serviceIntent.setAction(RunningTrackingService.ACTION_STOP_RUNNING);
        requireContext().startService(serviceIntent);
        
        // Update UI state
        isTracking = false;
        receivingBroadcasts = false; // Reset broadcast flag
        startRunButton.setText("Bắt đầu chạy");
        startRunButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.green_primary));
        
        Toast.makeText(getContext(), "Đã dừng theo dõi chạy bộ", Toast.LENGTH_SHORT).show();
        
        // Stop local timer
        stopLocalTimer();
        
        // Refresh running history
        refreshRunningHistory();
        
        // Reset UI after a short delay
        resetUI();
    }

    private void updateRealtimeUI(Intent intent) {
        android.util.Log.d("RunningFragment", "updateRealtimeUI called - BROADCAST RECEIVED!");
        
        // Mark that we're receiving broadcasts from service
        receivingBroadcasts = true;
        
        double distance = intent.getDoubleExtra(RunningTrackingService.EXTRA_DISTANCE, 0.0);
        long duration = intent.getLongExtra(RunningTrackingService.EXTRA_DURATION, 0);
        int calories = intent.getIntExtra(RunningTrackingService.EXTRA_CALORIES, 0);
        boolean isCurrentlyTracking = intent.getBooleanExtra(RunningTrackingService.EXTRA_IS_RUNNING, false);
        
        android.util.Log.d("RunningFragment", "Received data - Distance: " + distance + 
                ", Duration: " + duration + ", Calories: " + calories + ", isTracking: " + isCurrentlyTracking);
        
        // Update UI components with service data
        if (totalDistanceText != null) {
            String distanceStr = String.format(Locale.getDefault(), "%.2f", distance);
            totalDistanceText.setText(distanceStr);
            android.util.Log.d("RunningFragment", "Updated distance text: " + distanceStr);
        }
        
        if (totalTimeText != null) {
            String formattedTime = formatDuration(duration);
            totalTimeText.setText(formattedTime);
            android.util.Log.d("RunningFragment", "Updated time text: " + formattedTime);
        }
        
        if (totalCaloriesText != null) {
            String caloriesStr = String.valueOf(calories);
            totalCaloriesText.setText(caloriesStr);
            android.util.Log.d("RunningFragment", "Updated calories text: " + caloriesStr);
        }
        
        // Update tracking state
        isTracking = isCurrentlyTracking;
        
        if (!isTracking) {
            // Service stopped, reset button and flag
            receivingBroadcasts = false;
            startRunButton.setText("Bắt đầu chạy");
            startRunButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.green_primary));
        }
    }

    private void resetUI() {
        totalDistanceText.setText("0.0");
        totalTimeText.setText("00:00");
        totalCaloriesText.setText("0");
    }

    private String formatDuration(long durationMillis) {
        if (durationMillis <= 0) {
            return "00:00";
        }
        
        long seconds = durationMillis / 1000;
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        
        if (minutes >= 60) {
            long hours = minutes / 60;
            minutes = minutes % 60;
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, remainingSeconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds);
        }
    }

    private void refreshRunningHistory() {
        // Get fresh data from database
        runningHistory.clear();
        runningHistory.addAll(getRunningHistoryData());
        adapter.notifyDataSetChanged();
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), 
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION);
    }

    private void checkLocationPermission() {
        if (!hasLocationPermission()) {
            requestLocationPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Quyền truy cập vị trí đã được cấp", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Cần quyền truy cập vị trí để theo dõi chạy bộ", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (runningUpdateReceiver != null) {
            requireContext().unregisterReceiver(runningUpdateReceiver);
            runningUpdateReceiver = null;
        }
    }

    // Sample data for demonstration purposes (updated with more sessions)
    private List<WorkoutHistory> getRunningHistoryData() {
        List<WorkoutHistory> history = new ArrayList<>();

        // Get running exercises from database
        List<Exercise> runningExercises = dbHelper.getAllExercises();
        
        // Filter and convert running exercises to WorkoutHistory
        for (Exercise exercise : runningExercises) {
            if ("RUNNING".equalsIgnoreCase(exercise.getExerciseType())) {
                WorkoutHistory workoutHistory = convertExerciseToWorkoutHistory(exercise);
                if (workoutHistory != null) {
                    history.add(workoutHistory);
                }
            }
        }

        // If no real data, add sample data
        if (history.isEmpty()) {
            history.add(new WorkoutHistory("Chạy thử data", "25/08/2023, 06:30", 5.2, "00:32:45", 320));
        }
        
        return history;
    }
    
    // Helper method to convert Exercise to WorkoutHistory
    private WorkoutHistory convertExerciseToWorkoutHistory(Exercise exercise) {
        if (exercise == null) {
            return null;
        }
        
        // Format title based on time of day
        String title = "Chạy bộ";
        if (exercise.getStartTime() != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(exercise.getStartTime());
            int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
            
            if (hour >= 5 && hour < 12) {
                title = "Chạy buổi sáng";
            } else if (hour >= 12 && hour < 17) {
                title = "Chạy buổi chiều";
            } else {
                title = "Chạy buổi tối";
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
        
        // Format duration
        String duration = formatDuration(exercise.getDuration());
        
        return new WorkoutHistory(
                title,
                dateTime,
                exercise.getDistance(),
                duration,
                exercise.getCaloriesBurned()
        );
    }
    
    // Local timer for testing - remove this later when broadcast works
    private void startLocalTimer() {
        localStartTime = System.currentTimeMillis();
        localTimerHandler = new Handler();
        localTimerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isTracking) {
                    long duration = System.currentTimeMillis() - localStartTime;
                    android.util.Log.d("RunningFragment", "Local timer - Duration: " + duration + "ms, receivingBroadcasts: " + receivingBroadcasts);
                    
                    // Only update UI if NOT receiving broadcasts from service
                    if (!receivingBroadcasts) {
                        android.util.Log.d("RunningFragment", "Updating UI via local timer (no broadcasts received)");
                        totalTimeText.setText(formatDuration(duration));
                        // Keep distance and calories as set by service broadcasts or 0
                        if (totalDistanceText.getText().toString().equals("0.0")) {
                            totalDistanceText.setText("0.00");
                        }
                        if (totalCaloriesText.getText().toString().equals("0")) {
                            totalCaloriesText.setText("0");
                        }
                    }
                    
                    localTimerHandler.postDelayed(this, 1000); // Update every second
                }
            }
        };
        localTimerHandler.post(localTimerRunnable);
    }
    
    private void stopLocalTimer() {
        if (localTimerHandler != null && localTimerRunnable != null) {
            localTimerHandler.removeCallbacks(localTimerRunnable);
        }
    }
} 