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

    // MET values
    private static final double WALKING_MET = 3.5;
    public static final double RUNNING_MET = 8.0;
    private static final double CYCLING_MET = 6.0;
    private static final double DEFAULT_MET = 4.0;

    // Constants for BMR, TDEE, steps, and active time
    private static final double DEFAULT_ACTIVITY_MULTIPLIER = 1.375;
    private static final double CALORIES_PER_STEP_MALE = 0.04;
    private static final double CALORIES_PER_STEP_FEMALE = 0.035;
    private static final int MIN_ACTIVE_MINUTES_PER_DAY = 30;

    // Enum để phân biệt giới tính
    public enum Gender {
        MALE, FEMALE
    }

    public static long calculateDuration(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return endTime.getTime() - startTime.getTime();
    }

    public static int calculateSteps(Exercise exercise) {
        if (exercise == null) {
            return 0;
        }

        if ("WALKING".equalsIgnoreCase(exercise.getExerciseType())) {
            int steps = exercise.getSteps();
            Log.d("ExerciseUtils", "Calculated steps for WALKING: " + steps);
            return steps;
        }

        if ("RUNNING".equalsIgnoreCase(exercise.getExerciseType()) && exercise.getDistance() > 0) {
            int steps = (int)(exercise.getDistance() * 1100);
            Log.d("ExerciseUtils", "Calculated steps for RUNNING: " + steps + " (distance: " + exercise.getDistance() + " km)");
            return steps;
        }

        Log.d("ExerciseUtils", "No steps calculated for exercise type: " + exercise.getExerciseType());
        return 0;
    }

    public static int getTotalSteps(Date date, DatabaseHelper dbHelper) {
        if (date == null || dbHelper == null) {
            Log.e("ExerciseUtils", "Invalid date or dbHelper for getTotalSteps");
            return 0;
        }

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

    public static int calculateCalories(Exercise exercise, float weightKg) {
        String type = exercise.getExerciseType();
        long duration = exercise.getDuration();

        if (type == null || duration <= 0) {
            Log.d("ExerciseUtils", "Invalid exercise type or duration for calorie calculation");
            return 0;
        }

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

        double durationInHours = duration / (1000.0 * 60 * 60);
        int calories = (int) (metValue * weightKg * durationInHours);
        Log.d("ExerciseUtils", "Calculated calories: " + calories + " for " + type + ", duration: " + duration + "ms, weight: " + weightKg + "kg");
        return calories;
    }

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

    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        String formatted = sdf.format(date);
        Log.d("ExerciseUtils", "Formatted date: " + formatted + " with pattern: " + pattern);
        return formatted;
    }

    public static int calculateCaloriesWithUserProfile(Exercise exercise, UserProfile userProfile) {
        if (exercise == null || userProfile == null) {
            Log.e("ExerciseUtils", "Invalid exercise or userProfile for calorie calculation");
            return 0;
        }
        return calculateCalories(exercise, userProfile.getWeight());
    }

    // ============================ NEW METHODS BELOW ============================

    // Tính BMR (Basal Metabolic Rate)
    public static double calculateBMR(float weightKg, int heightCm, int age, Gender gender) {
        if (gender == Gender.MALE) {
            return 10 * weightKg + 6.25 * heightCm - 5 * age + 5;
        } else {
            return 10 * weightKg + 6.25 * heightCm - 5 * age - 161;
        }
    }

    // Tính TDEE (Total Daily Energy Expenditure)
    public static int calculateTDEE(float weightKg, int heightCm, int age, Gender gender) {
        double bmr = calculateBMR(weightKg, heightCm, age, gender);
        return (int) (bmr * DEFAULT_ACTIVITY_MULTIPLIER);
    }

    // Tính số bước chân tối thiểu mỗi ngày dựa vào TDEE
    public static int calculateMinStepsPerDay(int tdee, Gender gender) {
        double caloriesPerStep = (gender == Gender.MALE) ? CALORIES_PER_STEP_MALE : CALORIES_PER_STEP_FEMALE;
        return (int) (tdee * 0.1 / caloriesPerStep); // Giả sử 15% TDEE đến từ bước đi
    }

    // Trả về thời gian vận động tối thiểu (phút)
    public static int getMinActiveMinutesPerDay() {
        return MIN_ACTIVE_MINUTES_PER_DAY;
    }
}
