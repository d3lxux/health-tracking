package hcmute.edu.vn.healthtracking.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import hcmute.edu.vn.healthtracking.R;
import hcmute.edu.vn.healthtracking.fragments.ChatbotFragment;
import hcmute.edu.vn.healthtracking.fragments.ExerciseFragment;
import hcmute.edu.vn.healthtracking.fragments.HomeFragment;
import hcmute.edu.vn.healthtracking.fragments.ProfileFragment;
import hcmute.edu.vn.healthtracking.fragments.ScheduleFragment;
import hcmute.edu.vn.healthtracking.fragments.UploadFragment;
import hcmute.edu.vn.healthtracking.fragments.ScheduleFragment;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Set up BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // Highlight Home by default
        // Load HomeFragment as the default view
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

        // Set event listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;
            switch (Objects.requireNonNull(item.getTitle()).toString()) {
                case "Home":
                    selectedFragment = new HomeFragment();
                    break;
                case "Workout":
                    selectedFragment = new ExerciseFragment();
                    break;
                case "Chatbot":
                    selectedFragment = new ChatbotFragment();
                    break;
                case "Upload":
                    selectedFragment = new UploadFragment();
                    break;
                case "Schedule":
                    selectedFragment = new ScheduleFragment();
                    break;
                case "Profile":
                    selectedFragment = new ProfileFragment();
                    break;
                default:
                    return false;
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
            return true;
        });

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}