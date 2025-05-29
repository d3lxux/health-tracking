package hcmute.edu.vn.healthtracking.models;

public class WorkoutHistory {
    private String title;
    private String date;
    private double distance;
    private String duration;
    private int caloriesBurned;
    
    // Constructor for a new workout history item
    public WorkoutHistory(String title, String date, double distance, String duration, int caloriesBurned) {
        this.title = title;
        this.date = date;
        this.distance = distance;
        this.duration = duration;
        this.caloriesBurned = caloriesBurned;
    }
    
    // Getters and setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public double getDistance() {
        return distance;
    }
    
    public void setDistance(double distance) {
        this.distance = distance;
    }
    
    public String getDuration() {
        return duration;
    }
    
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    public int getCaloriesBurned() {
        return caloriesBurned;
    }
    
    public void setCaloriesBurned(int caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }
} 