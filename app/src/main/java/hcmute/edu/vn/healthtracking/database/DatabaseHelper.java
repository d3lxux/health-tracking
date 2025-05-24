package hcmute.edu.vn.healthtracking.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.healthtracking.models.Task;
import hcmute.edu.vn.healthtracking.models.UserProfile;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tasks.db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_TASKS = "tasks";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME = "time";

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

        // Tạo bảng user profile (chỉ 1 dòng)
        String createUserProfileTable = "CREATE TABLE user_profile (" +
                "id INTEGER PRIMARY KEY, " +
                "name TEXT, " +
                "age INTEGER, " +
                "height REAL, " +
                "weight REAL, " +
                "avatar_uri TEXT,gender TEXT)";
        db.execSQL(createUserProfileTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Đã xử lý
            db.execSQL("ALTER TABLE user_profile ADD COLUMN gender TEXT");
        }

        if (oldVersion < 3) {
            // Future migrations
        }
    }




    // Thêm Task
    // Thêm Task và trả về ID của Task vừa tạo
    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_DESCRIPTION, task.getDescription());
        values.put(COLUMN_DATE, task.getDate());
        values.put(COLUMN_TIME, task.getTime());

        // Chèn dữ liệu vào database và lấy ID vừa tạo
        long taskId = db.insert(TABLE_TASKS, null, values);
        db.close();
        return taskId; // Trả về ID của task mới
    }


    // Cập nhật Task
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


    // Xóa Task theo ID
    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Lấy tất cả Task
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
                        cursor.getString(3), // Ngày
                        cursor.getString(4)  // Giờ
                );
                taskList.add(task);
                Log.d("DatabaseCheck", "Task Loaded: " + task.getTitle() + " | Date: " + task.getDate());
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }


    // Lấy Task theo ID
    public Task getTaskById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_DESCRIPTION, COLUMN_DATE, COLUMN_TIME},
                COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
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

    // Lấy danh sách Task theo ngày
    public List<Task> getTasksByDate(String date) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TASKS,
                new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_DESCRIPTION, COLUMN_DATE, COLUMN_TIME},
                COLUMN_DATE + " = ?",
                new String[]{date}, null, null, null);

        Log.d("DatabaseQuery", "Querying tasks for date: " + date);

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
                Log.d("DatabaseQuery", "Task Found: " + task.getTitle() + " | Date: " + task.getDate());
            } while (cursor.moveToNext());
        } else {
            Log.d("DatabaseQuery", "No task found for date: " + date);
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



    // Lấy thông tin người dùng :
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



}
