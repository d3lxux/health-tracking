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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hcmute.edu.vn.healthtracking.R;
import hcmute.edu.vn.healthtracking.database.DatabaseHelper;
import hcmute.edu.vn.healthtracking.models.Exercise;
import hcmute.edu.vn.healthtracking.models.UserProfile;
import hcmute.edu.vn.healthtracking.services.StepTrackingService;
import hcmute.edu.vn.healthtracking.utils.ExerciseUtils;

public class HomeFragment extends Fragment {

    private TextView stepsTextView, caloriesTextView, activeMinutesTextView, distanceTextView;
    private TextView goalStepsTextView, minCaloriesTextView, minDurationTextView; // New TextViews
    private ProgressBar progressBar;
    private DatabaseHelper dbHelper;
    private UserProfile userProfile;
    private static final int STEP_GOAL = 6000; // Goal: 6000 steps
    private static final int CALORIE_GOAL = 300; // Goal: 300 kcal
    private static final int ACTIVE_MINUTES_GOAL = 30; // Goal: 30 minutes
    private static final String ACTION_UPDATE_UI = "hcmute.edu.vn.healthtracking.ACTION_UPDATE_UI";
    private static final String TAG = "HomeFragment";
    private BroadcastReceiver uiUpdateReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views with null checks
        stepsTextView = view.findViewById(R.id.tv_steps);
        if (stepsTextView == null) Log.e(TAG, "stepsTextView is null");
        caloriesTextView = view.findViewById(R.id.tv_calories);
        if (caloriesTextView == null) Log.e(TAG, "caloriesTextView is null");
        activeMinutesTextView = view.findViewById(R.id.tv_active_minutes);
        if (activeMinutesTextView == null) Log.e(TAG, "activeMinutesTextView is null");
        distanceTextView = view.findViewById(R.id.tv_distance);
        if (distanceTextView == null) Log.e(TAG, "distanceTextView is null");
        progressBar = view.findViewById(R.id.progress_bar);
        if (progressBar == null) Log.e(TAG, "progressBar is null");
        // Initialize new TextViews
        goalStepsTextView = view.findViewById(R.id.tv_goal_steps);
        if (goalStepsTextView == null) Log.e(TAG, "minStepsTextView is null");
        minCaloriesTextView = view.findViewById(R.id.minCalories);
        if (minCaloriesTextView == null) Log.e(TAG, "minCaloriesTextView is null");
        minDurationTextView = view.findViewById(R.id.minDuration);
        if (minDurationTextView == null) Log.e(TAG, "minDurationTextView is null");

        // Initialize database
        dbHelper = new DatabaseHelper(requireContext());

        // Get user profile
        userProfile = dbHelper.getUserProfile();
        if (userProfile == null) {
            userProfile = new UserProfile("Default User", 30, 170.0f, 70.0f, null);
            dbHelper.saveUserProfile(userProfile.getName(), userProfile.getAge(),
                    userProfile.getHeight(), userProfile.getWeight(), null);
        }

        // Start StepTrackingService
        Intent serviceIntent = new Intent(requireContext(), StepTrackingService.class);
        requireContext().startService(serviceIntent);

        // Register BroadcastReceiver for UI updates
        uiUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUI();
            }
        };
        IntentFilter filter = new IntentFilter(ACTION_UPDATE_UI);
        requireContext().registerReceiver(uiUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        // Update UI
        updateUI();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (uiUpdateReceiver != null) {
            requireContext().unregisterReceiver(uiUpdateReceiver);
            uiUpdateReceiver = null;
        }
    }

    private void updateUI() {
        // Get current date (e.g., 20250524 for May 24, 2025)
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        // Get today's Walking exercise
        Exercise walkingExercise = dbHelper.getWalkingExerciseByDate(today);

        // Calculate total steps (Walking + Running)
        int totalSteps = ExerciseUtils.getTotalSteps(new Date(), dbHelper);

        int totalCalories = 0;
        long totalDuration = 0; // milliseconds
        double totalDistance = 0.0;

        if (walkingExercise != null) {
            totalCalories = walkingExercise.getCaloriesBurned();
            totalDuration = walkingExercise.getDuration();
            totalDistance = walkingExercise.getDistance();
        }

        // Convert duration to minutes
//        int totalActiveMinutes = (int) (totalDuration / (1000 * 60));
        int totalActiveMinutes = 0;

        // Update existing UI with null checks
        if (stepsTextView != null)
            stepsTextView.setText(String.format(Locale.getDefault(), "%d", totalSteps));
        if (caloriesTextView != null)
            caloriesTextView.setText(String.format(Locale.getDefault(), "%d", totalCalories));
        if (activeMinutesTextView != null)
            activeMinutesTextView.setText(String.format(Locale.getDefault(), "%d", totalActiveMinutes));
        if (distanceTextView != null)
            distanceTextView.setText(String.format(Locale.getDefault(), "%.2f", totalDistance));

        // Update ProgressBar based on steps
        if (progressBar != null) {
            int progress = (int) ((totalSteps / (float) STEP_GOAL) * 2000);
            progressBar.setProgress(Math.min(progress, 100));
        }

        // Calculate minimum metrics (assuming Male gender for simplicity)
        ExerciseUtils.Gender gender = ExerciseUtils.Gender.MALE; // Default assumption
        float weight = userProfile.getWeight();
        int height = (int) userProfile.getHeight(); // Convert to int (cm)
        int age = userProfile.getAge();

        int minCalories = ExerciseUtils.calculateTDEE(weight, height, age, gender);
        int minSteps = ExerciseUtils.calculateMinStepsPerDay(minCalories, gender);
        int minActiveMinutes = ExerciseUtils.getMinActiveMinutesPerDay();

        // Update new TextViews with minimum values
        if (goalStepsTextView != null)
            goalStepsTextView.setText(String.format(Locale.getDefault(), "Goal: %d steps", minSteps));
        if (minCaloriesTextView != null)
            minCaloriesTextView.setText(String.format(Locale.getDefault(), "Goal: %d kcal", minCalories));
        if (minDurationTextView != null)
            minDurationTextView.setText(String.format(Locale.getDefault(), "Goal: %d min", minActiveMinutes));
    }
}