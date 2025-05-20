package hcmute.edu.vn.healthtracking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import hcmute.edu.vn.healthtracking.R;

public class MainActivity extends AppCompatActivity {
    private TextView tvCalories, tvSteps, tvActiveMinutes;
    private ProgressBar progressBar;
    private ImageButton btnAdd;
    private RecyclerView rvHealthTiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        tvCalories = findViewById(R.id.tv_calories);
        tvSteps = findViewById(R.id.tv_steps);
        tvActiveMinutes = findViewById(R.id.tv_active_minutes);
        progressBar = findViewById(R.id.progress_bar);
        btnAdd = findViewById(R.id.btn_add);

        // Set up RecyclerView

        // Set up BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // Highlight Home by default
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on MainActivity (Home), no action needed
                return true;
            } else if (itemId == R.id.nav_exercise) {
                startActivity(new Intent(this, WorkoutActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_stats) {
                startActivity(new Intent(this, WorkoutActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, WorkoutActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        // Sample data (to be replaced with real data from a health tracker)
        updateMetrics(0, 6, 0, 25);

        // Add button click listener (placeholder for adding data)
        btnAdd.setOnClickListener(v -> {
            // Implement add data functionality here
        });

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void updateMetrics(int calories, int steps, int activeMinutes, int progress) {
        tvCalories.setText(String.valueOf(calories));
        tvSteps.setText(String.valueOf(steps));
        tvActiveMinutes.setText(String.valueOf(activeMinutes));
        progressBar.setProgress(progress);
    }
}