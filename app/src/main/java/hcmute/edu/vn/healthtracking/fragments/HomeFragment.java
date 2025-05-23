package hcmute.edu.vn.healthtracking.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.healthtracking.R;
import hcmute.edu.vn.healthtracking.database.DatabaseHelper;
import hcmute.edu.vn.healthtracking.models.Exercise;
import hcmute.edu.vn.healthtracking.models.UserProfile;
import hcmute.edu.vn.healthtracking.utils.ExerciseUtils;

public class HomeFragment extends Fragment {

    private TextView stepsTextView, caloriesTextView, activeMinutesTextView;
    private ProgressBar progressBar;
    private DatabaseHelper dbHelper;
    private UserProfile userProfile;
    private static final int STEP_GOAL = 6000; // Mục tiêu 6000 bước
    private static final int CALORIE_GOAL = 300; // Mục tiêu 300 kcal
    private static final int ACTIVE_MINUTES_GOAL = 30; // Mục tiêu 30 phút

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo views
        stepsTextView = view.findViewById(R.id.tv_steps);
        caloriesTextView = view.findViewById(R.id.tv_calories);
        activeMinutesTextView = view.findViewById(R.id.tv_active_minutes);
        progressBar = view.findViewById(R.id.progress_bar);

        // Khởi tạo database
        dbHelper = new DatabaseHelper(requireContext());

        // Lấy thông tin người dùng
        userProfile = dbHelper.getUserProfile();
        if (userProfile == null) {
            // Giả lập thông tin người dùng nếu chưa có
            userProfile = new UserProfile("Default User", 30, 170.0f, 70.0f, null);
            dbHelper.saveUserProfile(userProfile.getName(), userProfile.getAge(),
                    userProfile.getHeight(), userProfile.getWeight(), null);
        }

        // Cập nhật giao diện
        updateUI();

        // Giả lập thêm dữ liệu bước chân (thay bằng StepServiceProvider sau này)
        simulateStepTracking();

        return view;
    }

    private void updateUI() {
        // Lấy ngày hiện tại
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        // Lấy danh sách bài tập hôm nay
        List<Exercise> todayExercises = dbHelper.getExercisesByDate(today);

        int totalSteps = 0;
        int totalCalories = 0;
        long totalDuration = 0; // milliseconds

        // Tính tổng số bước, calories, và thời gian vận động
        for (Exercise exercise : todayExercises) {
            totalSteps += ExerciseUtils.calculateSteps(exercise);
            totalCalories += exercise.getCaloriesBurned();
            totalDuration += exercise.getDuration();
        }

        // Chuyển đổi duration sang phút
        int totalActiveMinutes = (int) (totalDuration / (1000 * 60));

        // Cập nhật giao diện
        stepsTextView.setText(String.format(Locale.getDefault(), "%d", totalSteps));
        caloriesTextView.setText(String.format(Locale.getDefault(), "%d", totalCalories));
        activeMinutesTextView.setText(String.format(Locale.getDefault(), "%d", totalActiveMinutes));

        // Cập nhật ProgressBar dựa trên số bước
        int progress = (int) ((totalSteps / (float) STEP_GOAL) * 100);
        progressBar.setProgress(Math.min(progress, 100));
    }

    private void simulateStepTracking() {
        // Giả lập dữ liệu bước chân
        int simulatedSteps = 5000; // Giả lập 5000 bước
        double distance = simulatedSteps / 1100.0; // Giả lập khoảng cách (dựa trên công thức trong ExerciseUtils)
        long duration = 3600 * 1000; // Giả lập 1 giờ hoạt động

        // Tạo bài tập mới
        Exercise exercise = new Exercise(
                "user1", // Giả lập userId
                "WALKING",
                new Date(System.currentTimeMillis() - duration), // 1 giờ trước
                new Date(),
                new Date(),
                distance
        );
        exercise.setDuration(duration);
        exercise.setCaloriesBurned(ExerciseUtils.calculateCaloriesWithUserProfile(exercise, userProfile));

        // Lưu vào database
        dbHelper.addExercise(exercise);

        // Cập nhật lại giao diện
        updateUI();
    }
}