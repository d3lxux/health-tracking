package hcmute.edu.vn.healthtracking.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.healthtracking.models.Exercise;
import hcmute.edu.vn.healthtracking.models.Task;
import hcmute.edu.vn.healthtracking.models.UserProfile;
import hcmute.edu.vn.healthtracking.utils.ExerciseUtils;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tasks.db";
    private static final int DATABASE_VERSION = 6; // Incremented to remove UNIQUE constraint

    // Bảng tasks
    private static final String TABLE_TASKS = "tasks";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME = "time";

    // Bảng exercises
    private static final String TABLE_EXERCISES = "exercises";
    private static final String COLUMN_EXERCISE_ID = "id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_EXERCISE_TYPE = "exercise_type";
    private static final String COLUMN_START_TIME = "start_time";
    private static final String COLUMN_END_TIME = "end_time";
    private static final String COLUMN_EXERCISE_DATE = "exercise_date";
    private static final String COLUMN_DISTANCE = "distance";
    private static final String COLUMN_DURATION = "duration";
    private static final String COLUMN_CALORIES_BURNED = "calories_burned";
    private static final String COLUMN_STEPS = "steps"; // Added for Walking mode

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTaskTable = "CREATE TABLE " + TABLE_TASKS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_TIME + " TEXT)";
        db.execSQL(createTaskTable);

        String createUserProfileTable = "CREATE TABLE user_profile (" +
                "id INTEGER PRIMARY KEY, " +
                "name TEXT, " +
                "age INTEGER, " +
                "height REAL, " +
                "weight REAL, " +
                "avatar_uri TEXT,gender TEXT)";
        db.execSQL(createUserProfileTable);

        String createExerciseTable = "CREATE TABLE " + TABLE_EXERCISES + " (" +
                COLUMN_EXERCISE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " TEXT, " +
                COLUMN_EXERCISE_TYPE + " TEXT, " +
                COLUMN_START_TIME + " INTEGER, " +
                COLUMN_END_TIME + " INTEGER, " +
                COLUMN_EXERCISE_DATE + " TEXT, " +
                COLUMN_DISTANCE + " REAL, " +
                COLUMN_DURATION + " INTEGER, " +
                COLUMN_CALORIES_BURNED + " INTEGER, " +
                COLUMN_STEPS + " INTEGER)";
        db.execSQL(createExerciseTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Đã xử lý
            db.execSQL("ALTER TABLE user_profile ADD COLUMN gender TEXT");
        }
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_EXERCISES + " (" +
                    COLUMN_EXERCISE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " TEXT, " +
                    COLUMN_EXERCISE_TYPE + " TEXT, " +
                    COLUMN_START_TIME + " INTEGER, " +
                    COLUMN_END_TIME + " INTEGER, " +
                    COLUMN_EXERCISE_DATE + " TEXT, " +
                    COLUMN_DISTANCE + " REAL, " +
                    COLUMN_DURATION + " INTEGER, " +
                    COLUMN_CALORIES_BURNED + " INTEGER)");
        }
        if (oldVersion < 4) {
            db.execSQL("CREATE TABLE exercises_new (" +
                    COLUMN_EXERCISE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " TEXT, " +
                    COLUMN_EXERCISE_TYPE + " TEXT, " +
                    COLUMN_START_TIME + " INTEGER, " +
                    COLUMN_END_TIME + " INTEGER, " +
                    COLUMN_EXERCISE_DATE + " TEXT, " +
                    COLUMN_DISTANCE + " REAL, " +
                    COLUMN_DURATION + " INTEGER, " +
                    COLUMN_CALORIES_BURNED + " INTEGER, " +
                    "UNIQUE(" + COLUMN_EXERCISE_DATE + ", " + COLUMN_EXERCISE_TYPE + "))");

            // Select the record with the maximum id for each exercise_date and exercise_type
            db.execSQL("INSERT INTO exercises_new (" +
                    COLUMN_EXERCISE_ID + ", " +
                    COLUMN_USER_ID + ", " +
                    COLUMN_EXERCISE_TYPE + ", " +
                    COLUMN_START_TIME + ", " +
                    COLUMN_END_TIME + ", " +
                    COLUMN_EXERCISE_DATE + ", " +
                    COLUMN_DISTANCE + ", " +
                    COLUMN_DURATION + ", " +
                    COLUMN_CALORIES_BURNED + ") " +
                    "SELECT " +
                    COLUMN_EXERCISE_ID + ", " +
                    COLUMN_USER_ID + ", " +
                    COLUMN_EXERCISE_TYPE + ", " +
                    COLUMN_START_TIME + ", " +
                    COLUMN_END_TIME + ", " +
                    COLUMN_EXERCISE_DATE + ", " +
                    COLUMN_DISTANCE + ", " +
                    COLUMN_DURATION + ", " +
                    COLUMN_CALORIES_BURNED + " " +
                    "FROM " + TABLE_EXERCISES + " WHERE " + COLUMN_EXERCISE_ID + " IN (" +
                    "SELECT MAX(" + COLUMN_EXERCISE_ID + ") FROM " + TABLE_EXERCISES + " " +
                    "GROUP BY " + COLUMN_EXERCISE_DATE + ", " + COLUMN_EXERCISE_TYPE + ")");

            db.execSQL("DROP TABLE " + TABLE_EXERCISES);
            db.execSQL("ALTER TABLE exercises_new RENAME TO " + TABLE_EXERCISES);
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE " + TABLE_EXERCISES + " ADD COLUMN " + COLUMN_STEPS + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 6) {
            // Remove UNIQUE constraint to allow multiple sessions per day
            db.execSQL("CREATE TABLE exercises_new (" +
                    COLUMN_EXERCISE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " TEXT, " +
                    COLUMN_EXERCISE_TYPE + " TEXT, " +
                    COLUMN_START_TIME + " INTEGER, " +
                    COLUMN_END_TIME + " INTEGER, " +
                    COLUMN_EXERCISE_DATE + " TEXT, " +
                    COLUMN_DISTANCE + " REAL, " +
                    COLUMN_DURATION + " INTEGER, " +
                    COLUMN_CALORIES_BURNED + " INTEGER, " +
                    COLUMN_STEPS + " INTEGER)"); // No UNIQUE constraint

            // Copy all existing data
            db.execSQL("INSERT INTO exercises_new SELECT * FROM " + TABLE_EXERCISES);

            // Replace old table
            db.execSQL("DROP TABLE " + TABLE_EXERCISES);
            db.execSQL("ALTER TABLE exercises_new RENAME TO " + TABLE_EXERCISES);
        }
    }

    // --- Quản lý Task ---
    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_DESCRIPTION, task.getDescription());
        values.put(COLUMN_DATE, task.getDate());
        values.put(COLUMN_TIME, task.getTime());

        long taskId = db.insert(TABLE_TASKS, null, values);
        db.close();
        return taskId;
    }

    public void updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_DESCRIPTION, task.getDescription());
        values.put(COLUMN_DATE, task.getDate());
        values.put(COLUMN_TIME, task.getTime());

        db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS, null);

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4)
                );
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }

    public Task getTaskById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_DESCRIPTION, COLUMN_DATE, COLUMN_TIME},
                COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Task task = new Task(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4)
            );
            cursor.close();
            db.close();
            return task;
        }
        return null;
    }

    public List<Task> getTasksByDate(String date) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TASKS,
                new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_DESCRIPTION, COLUMN_DATE, COLUMN_TIME},
                COLUMN_DATE + " = ?",
                new String[]{date}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4)
                );
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }

    // Lưu thông tin người dùng
    public void saveUserProfile(String name, int age, float height, float weight, String avatarUri, String gender) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", 1);
        values.put("name", name);
        values.put("age", age);
        values.put("height", height);
        values.put("weight", weight);
        values.put("avatar_uri", avatarUri);
        values.put("gender", gender);

        int rows = db.update("user_profile", values, "id = ?", new String[]{"1"});
        if (rows == 0) {
            db.insert("user_profile", null, values);
        }
        db.close();
    }

    public UserProfile getUserProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("user_profile", null, "id = ?", new String[]{"1"},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            UserProfile profile = new UserProfile(
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("age")),
                    cursor.getFloat(cursor.getColumnIndexOrThrow("height")),
                    cursor.getFloat(cursor.getColumnIndexOrThrow("weight")),
                    cursor.getString(cursor.getColumnIndexOrThrow("avatar_uri")),
                    cursor.getString(cursor.getColumnIndexOrThrow("gender"))
            );
            cursor.close();
            db.close();
            return profile;
        }
        db.close();
        return null;
    }

    // --- Quản lý Exercise ---
    public long addOrUpdateWalkingExercise(Exercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, exercise.getUserId());
        values.put(COLUMN_EXERCISE_TYPE, exercise.getExerciseType());
        values.put(COLUMN_START_TIME, exercise.getStartTime() != null ? exercise.getStartTime().getTime() : null);
        values.put(COLUMN_END_TIME, exercise.getEndTime() != null ? exercise.getEndTime().getTime() : null);
        values.put(COLUMN_EXERCISE_DATE, exercise.getDate() != null ? ExerciseUtils.formatDate(exercise.getDate(), "yyyyMMdd") : null);
        values.put(COLUMN_DISTANCE, exercise.getDistance());
        values.put(COLUMN_DURATION, exercise.getDuration());
        values.put(COLUMN_CALORIES_BURNED, exercise.getCaloriesBurned());
        values.put(COLUMN_STEPS, exercise.getSteps());

        // Check if a Walking record exists for the date
        String dateStr = ExerciseUtils.formatDate(exercise.getDate(), "yyyyMMdd");
        Cursor cursor = db.query(TABLE_EXERCISES,
                new String[]{COLUMN_EXERCISE_ID},
                COLUMN_EXERCISE_DATE + " = ? AND " + COLUMN_EXERCISE_TYPE + " = ?",
                new String[]{dateStr, "WALKING"},
                null, null, null);

        long exerciseId;
        if (cursor.moveToFirst()) {
            // Update existing record
            exerciseId = cursor.getLong(0);
            db.update(TABLE_EXERCISES, values, COLUMN_EXERCISE_ID + " = ?", new String[]{String.valueOf(exerciseId)});
            Log.d("DatabaseHelper", "Updated Walking exercise for date: " + dateStr);
        } else {
            // Insert new record
            exerciseId = db.insert(TABLE_EXERCISES, null, values);
            Log.d("DatabaseHelper", "Inserted new Walking exercise for date: " + dateStr);
        }
        cursor.close();
        db.close();
        return exerciseId;
    }

    public Exercise getWalkingExerciseByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXERCISES,
                new String[]{COLUMN_EXERCISE_ID, COLUMN_USER_ID, COLUMN_EXERCISE_TYPE, COLUMN_START_TIME,
                        COLUMN_END_TIME, COLUMN_EXERCISE_DATE, COLUMN_DISTANCE, COLUMN_DURATION, COLUMN_CALORIES_BURNED, COLUMN_STEPS},
                COLUMN_EXERCISE_DATE + " = ? AND " + COLUMN_EXERCISE_TYPE + " = ?",
                new String[]{date, "WALKING"},
                null, null, null);

        if (cursor.moveToFirst()) {
            Date exerciseDate = null;
            String dateStr = cursor.getString(5);
            if (dateStr != null) {
                try {
                    exerciseDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(dateStr);
                } catch (ParseException e) {
                    Log.e("DatabaseHelper", "Error parsing date: " + dateStr, e);
                }
            }

            Exercise exercise = new Exercise(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getLong(3) != 0 ? new java.util.Date(cursor.getLong(3)) : null,
                    cursor.getLong(4) != 0 ? new java.util.Date(cursor.getLong(4)) : null,
                    exerciseDate,
                    cursor.getDouble(6),
                    cursor.getLong(7),
                    cursor.getInt(8),
                    cursor.getInt(9)
            );
            cursor.close();
            db.close();
            return exercise;
        }
        cursor.close();
        db.close();
        return null;
    }

    // Add new exercise (for running sessions - allows multiple per day)
    public long addExercise(Exercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, exercise.getUserId());
        values.put(COLUMN_EXERCISE_TYPE, exercise.getExerciseType());
        values.put(COLUMN_START_TIME, exercise.getStartTime() != null ? exercise.getStartTime().getTime() : null);
        values.put(COLUMN_END_TIME, exercise.getEndTime() != null ? exercise.getEndTime().getTime() : null);
        values.put(COLUMN_EXERCISE_DATE, exercise.getDate() != null ? ExerciseUtils.formatDate(exercise.getDate(), "yyyyMMdd") : null);
        values.put(COLUMN_DISTANCE, exercise.getDistance());
        values.put(COLUMN_DURATION, exercise.getDuration());
        values.put(COLUMN_CALORIES_BURNED, exercise.getCaloriesBurned());
        values.put(COLUMN_STEPS, exercise.getSteps());

        long exerciseId = db.insert(TABLE_EXERCISES, null, values);
        Log.d("DatabaseHelper", "Inserted new " + exercise.getExerciseType() + " exercise with ID: " + exerciseId);

        db.close();
        return exerciseId;
    }

    public List<Exercise> getExercisesByDate(String date) {
        List<Exercise> exerciseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_EXERCISES,
                new String[]{COLUMN_EXERCISE_ID, COLUMN_USER_ID, COLUMN_EXERCISE_TYPE, COLUMN_START_TIME,
                        COLUMN_END_TIME, COLUMN_EXERCISE_DATE, COLUMN_DISTANCE, COLUMN_DURATION, COLUMN_CALORIES_BURNED, COLUMN_STEPS},
                COLUMN_EXERCISE_DATE + " = ?",
                new String[]{date}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Date exerciseDate = null;
                String dateStr = cursor.getString(5);
                if (dateStr != null) {
                    try {
                        exerciseDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(dateStr);
                    } catch (ParseException e) {
                        Log.e("DatabaseHelper", "Error parsing date: " + dateStr, e);
                    }
                }

                Exercise exercise = new Exercise(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getLong(3) != 0 ? new java.util.Date(cursor.getLong(3)) : null,
                        cursor.getLong(4) != 0 ? new java.util.Date(cursor.getLong(4)) : null,
                        exerciseDate,
                        cursor.getDouble(6),
                        cursor.getLong(7),
                        cursor.getInt(8),
                        cursor.getInt(9)
                );
                exerciseList.add(exercise);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return exerciseList;
    }

    public List<Exercise> getAllExercises() {
        List<Exercise> exerciseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EXERCISES, null);

        if (cursor.moveToFirst()) {
            do {
                Date exerciseDate = null;
                String dateStr = cursor.getString(5);
                if (dateStr != null) {
                    try {
                        exerciseDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(dateStr);
                    } catch (ParseException e) {
                        Log.e("DatabaseHelper", "Error parsing date: " + dateStr, e);
                    }
                }

                Exercise exercise = new Exercise(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getLong(3) != 0 ? new java.util.Date(cursor.getLong(3)) : null,
                        cursor.getLong(4) != 0 ? new java.util.Date(cursor.getLong(4)) : null,
                        exerciseDate,
                        cursor.getDouble(6),
                        cursor.getLong(7),
                        cursor.getInt(8),
                        cursor.getInt(9)
                );
                exerciseList.add(exercise);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return exerciseList;
    }
    // Calculate total calories for all exercises on a specific date

    public int getTotalCaloriesByDate(String date) {
        List<Exercise> exercises = getExercisesByDate(date);
        int totalCalories = 0;
        for (Exercise exercise : exercises) {
            totalCalories += exercise.getCaloriesBurned();
        }
        Log.d("DatabaseHelper", "Total calories for date " + date + ": " + totalCalories);
        return totalCalories;
    }

    // Calculate total distance for all exercises on a specific date
    public double getTotalDistanceByDate(String date) {
        List<Exercise> exercises = getExercisesByDate(date);
        double totalDistance = 0.0;
        for (Exercise exercise : exercises) {
            totalDistance += exercise.getDistance();
        }
        Log.d("DatabaseHelper", "Total distance for date " + date + ": " + totalDistance + " km");
        return totalDistance;
    }

    // Calculate total duration for all exercises on a specific date
    public long getTotalDurationByDate(String date) {
        List<Exercise> exercises = getExercisesByDate(date);
        long totalDuration = 0;
        for (Exercise exercise : exercises) {
            totalDuration += exercise.getDuration();
        }
        Log.d("DatabaseHelper", "Total duration for date " + date + ": " + totalDuration + " ms");
        return totalDuration;
    }
}

