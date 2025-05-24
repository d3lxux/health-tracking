package hcmute.edu.vn.healthtracking.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.healthtracking.database.DatabaseHelper;
import hcmute.edu.vn.healthtracking.models.Exercise;
import hcmute.edu.vn.healthtracking.models.UserProfile;

public class ExerciseUtils {

    // const value
    private static final double WALKING_MET = 3.5;
    private static final double RUNNING_MET = 8.0;
    private static final double CYCLING_MET = 6.0;
    private static final double DEFAULT_MET = 4.0;

    public static long calculateDuration(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return endTime.getTime() - startTime.getTime();
    }

    // Calculate steps based on exercise type
    public static int calculateSteps(Exercise exercise) {
        if (exercise == null) {
            return 0;
        }

        // For walking, return the stored step count
        if ("WALKING".equalsIgnoreCase(exercise.getExerciseType())) {
            int steps = exercise.getSteps();
            Log.d("ExerciseUtils", "Calculated steps for WALKING: " + steps);
            return steps;
        }

        // For running, calculate from distance
        if ("RUNNING".equalsIgnoreCase(exercise.getExerciseType()) && exercise.getDistance() > 0) {
            int steps = (int)(exercise.getDistance() * 1100);
            Log.d("ExerciseUtils", "Calculated steps for RUNNING: " + steps + " (distance: " + exercise.getDistance() + " km)");
            return steps;
        }

        Log.d("ExerciseUtils", "No steps calculated for exercise type: " + exercise.getExerciseType());
        return 0;
    }

    // Calculate total steps for a given date
    public static int getTotalSteps(Date date, DatabaseHelper dbHelper) {
        if (date == null || dbHelper == null) {
            Log.e("ExerciseUtils", "Invalid date or dbHelper for getTotalSteps");
            return 0;
        }

        // Get walking and running exercises for the date
        String dateStr = formatDate(date, "yyyyMMdd");
        List<Exercise> exercises = dbHelper.getExercisesByDate(dateStr);

        int totalSteps = 0;
        for (Exercise exercise : exercises) {
            if ("WALKING".equalsIgnoreCase(exercise.getExerciseType()) ||
                    "RUNNING".equalsIgnoreCase(exercise.getExerciseType())) {
                totalSteps += calculateSteps(exercise);
            }
        }

        Log.d("ExerciseUtils", "Total steps for date " + dateStr + ": " + totalSteps);
        return totalSteps;
    }

    // Tính lượng calories tiêu thụ dựa trên loại bài tập, cân nặng và thời gian
    public static int calculateCalories(Exercise exercise, float weightKg) {
        String type = exercise.getExerciseType();
        long duration = exercise.getDuration();

        if (type == null || duration <= 0) {
            Log.d("ExerciseUtils", "Invalid exercise type or duration for calorie calculation");
            return 0;
        }

        // Chọn giá trị MET dựa trên loại hoạt động
        double metValue;
        switch (type.toUpperCase()) {
            case "WALKING":
                metValue = WALKING_MET;
                break;
            case "RUNNING":
                metValue = RUNNING_MET;
                break;
            case "CYCLING":
                metValue = CYCLING_MET;
                break;
            default:
                metValue = DEFAULT_MET;
                break;
        }

        // Công thức: Calories = MET × Trọng lượng (kg) × Thời gian (giờ)
        double durationInHours = duration / (1000.0 * 60 * 60);
        int calories = (int) (metValue * weightKg * durationInHours);
        Log.d("ExerciseUtils", "Calculated calories: " + calories + " for " + type + ", duration: " + duration + "ms, weight: " + weightKg + "kg");
        return calories;
    }

    // Định dạng thời gian duration sang chuỗi "HH:MM:SS"
    public static String formatDuration(long durationMillis) {
        if (durationMillis <= 0) {
            return "00:00:00";
        }

        long seconds = durationMillis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        String formatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, remainingSeconds);
        Log.d("ExerciseUtils", "Formatted duration: " + formatted + " from " + durationMillis + "ms");
        return formatted;
    }

    // Định dạng ngày tháng sang chuỗi
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        String formatted = sdf.format(date);
        Log.d("ExerciseUtils", "Formatted date: " + formatted + " with pattern: " + pattern);
        return formatted;
    }

    // Tính toán calories dựa trên thông tin người dùng đầy đủ
    public static int calculateCaloriesWithUserProfile(Exercise exercise, UserProfile userProfile) {
        if (exercise == null || userProfile == null) {
            Log.e("ExerciseUtils", "Invalid exercise or userProfile for calorie calculation");
            return 0;
        }
        return calculateCalories(exercise, userProfile.getWeight());
    }
}