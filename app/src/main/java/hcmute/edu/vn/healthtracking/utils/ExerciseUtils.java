package hcmute.edu.vn.healthtracking.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    // Calculate running step base on distance
    public static int calculateSteps(Exercise exercise) {
        if (exercise == null || exercise.getDistance() <= 0) {
            return 0;
        }

        String type = exercise.getExerciseType();
        if ("RUNNING".equalsIgnoreCase(type)) {
            return (int)(exercise.getDistance() * 1100);
        }

        // Trả về 0 cho các hoạt động khác (CYCLING, WALKING)
        return 0;
    }

    /*
    PLS READ HERE!!!!!!!!!!

    Dùng này để hiển thị tổng số bước bên home nha, vì tui nghĩ là khi chạy nó cũng tính step vào step tổng nên chi bằng mình tính chung cùng lúc sau đỡ handle
    Tuy nhiên tui chưa biết ông sẽ làm cái service cho walking như nào, tui sẽ assume là ông sẽ tạo một cái service provider, tui sẽ lấy giá trị từ đó truyền vào đây
    Để sẵn làm ý tưởng thôi, có gì chỗ này mình implement sau
     */

//    public interface StepServiceProvider {
//        int getDailySteps(Date date); // Lấy số bước đi bộ từ service cho một ngày cụ thể
//    }

//    public static int getTotalSteps(Date date, StepServiceProvider stepServiceProvider, List<Exercise> exercises) {
//        // Lấy số bước từ walking service (nếu có)
//        int walkingSteps = (stepServiceProvider != null) ? stepServiceProvider.getDailySteps(date) : 0;
//
//        // Lấy số bước từ các bài tập RUNNING
//        int runningSteps = 0;
//        if (exercises != null && !exercises.isEmpty()) {
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
//            String targetDateStr = sdf.format(date);
//
//            for (Exercise exercise : exercises) {
//                if (exercise.getDate() != null && sdf.format(exercise.getDate()).equals(targetDateStr)
//                        && "RUNNING".equalsIgnoreCase(exercise.getExerciseType())) {
//                    runningSteps += calculateSteps(exercise);
//                }
//            }
//        }
//
//        // Trả về tổng số bước
//        return walkingSteps + runningSteps;
//    }

    // Tính lượng calories tiêu thụ dựa trên loại bài tập, cân nặng và thời gian
    public static int calculateCalories(Exercise exercise, float weightKg) {
        String type = exercise.getExerciseType();
        long duration = exercise.getDuration();

        if (type == null || duration <= 0) {
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
        return (int) (metValue * weightKg * durationInHours);
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

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }

    // Định dạng ngày tháng sang chuỗi
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(date);
    }

    // Tính toán calories dựa trên thông tin người dùng đầy đủ
    public static int calculateCaloriesWithUserProfile(Exercise exercise, UserProfile userProfile) {
        if (exercise == null || userProfile == null) {
            return 0;
        }
        return calculateCalories(exercise, userProfile.getWeight());
    }
}
