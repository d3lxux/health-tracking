package hcmute.edu.vn.healthtracking.models;

import java.util.Date;

import hcmute.edu.vn.healthtracking.utils.ExerciseUtils;

public class Exercise {
    private int id;
    private String userId;
    private String exerciseType;  // "WALKING", "RUNNING", "CYCLING"
    private Date startTime;
    private Date endTime;
    private Date date;
    private double distance;      // km
    private long duration;        // milliseconds
    private int caloriesBurned;
    private int steps;           // Added for Walking mode

    public Exercise() {
    }

    // Constructor cơ bản
    public Exercise(String userId, String exerciseType, Date startTime,
                    Date endTime, Date date, double distance, int steps) {
        this.userId = userId;
        this.exerciseType = exerciseType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.distance = distance;
        this.steps = steps;
        this.duration = ExerciseUtils.calculateDuration(startTime, endTime);
    }

    // Constructor đầy đủ
    public Exercise(int id, String userId, String exerciseType, Date startTime,
                    Date endTime, Date date, double distance, long duration, int caloriesBurned, int steps) {
        this.id = id;
        this.userId = userId;
        this.exerciseType = exerciseType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.distance = distance;
        this.duration = duration;
        this.caloriesBurned = caloriesBurned;
        this.steps = steps;
    }

    // Getters và Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
        if (this.endTime != null) {
            this.duration = ExerciseUtils.calculateDuration(startTime, endTime);
        }
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
        if (this.startTime != null) {
            this.duration = ExerciseUtils.calculateDuration(startTime, endTime);
        }
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(int caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }
}