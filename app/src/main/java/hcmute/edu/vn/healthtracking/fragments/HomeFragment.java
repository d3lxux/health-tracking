package hcmute.edu.vn.healthtracking.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
    private ProgressBar progressBar;
    private DatabaseHelper dbHelper;
    private UserProfile userProfile;
    private static final int STEP_GOAL = 6000; // Mục tiêu 6000 bước
    private static final int CALORIE_GOAL = 300; // Mục tiêu 300 kcal
    private static final int ACTIVE_MINUTES_GOAL = 30; // Mục tiêu 30 phút
    private static final String ACTION_UPDATE_UI = "hcmute.edu.vn.healthtracking.ACTION_UPDATE_UI";
    private BroadcastReceiver uiUpdateReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo views
        stepsTextView = view.findViewById(R.id.tv_steps);
        caloriesTextView = view.findViewById(R.id.tv_calories);
        activeMinutesTextView = view.findViewById(R.id.tv_active_minutes);
        distanceTextView = view.findViewById(R.id.tv_distance);
        progressBar = view.findViewById(R.id.progress_bar);

        // Khởi tạo database
        dbHelper = new DatabaseHelper(requireContext());

        // Lấy thông tin người dùng
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

        // Cập nhật giao diện
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
        // Lấy ngày hiện tại
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        // Lấy bài tập Walking hôm nay
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

        // Chuyển đổi duration sang phút
        int totalActiveMinutes = (int) (totalDuration / (1000 * 60));

        // Cập nhật giao diện
        stepsTextView.setText(String.format(Locale.getDefault(), "%d", totalSteps));
        caloriesTextView.setText(String.format(Locale.getDefault(), "%d", totalCalories));
        activeMinutesTextView.setText(String.format(Locale.getDefault(), "%d", totalActiveMinutes));
        distanceTextView.setText(String.format(Locale.getDefault(), "%.2f", totalDistance));

        // Cập nhật ProgressBar dựa trên số bước
        int progress = (int) ((totalSteps / (float) STEP_GOAL) * 100);
        progressBar.setProgress(Math.min(progress, 100));
    }
}