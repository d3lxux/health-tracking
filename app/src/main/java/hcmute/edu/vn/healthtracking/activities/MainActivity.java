package hcmute.edu.vn.healthtracking.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

import hcmute.edu.vn.healthtracking.R;
import hcmute.edu.vn.healthtracking.fragments.ChatbotFragment;
import hcmute.edu.vn.healthtracking.fragments.ExerciseFragment;
import hcmute.edu.vn.healthtracking.fragments.HomeFragment;
import hcmute.edu.vn.healthtracking.fragments.ProfileFragment;
import hcmute.edu.vn.healthtracking.fragments.UploadFragment;

public class MainActivity extends AppCompatActivity {
    // Variables to handle touch case
    private float dX, dY;
    private boolean isClick;
    private FloatingActionButton chatFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize FAB
        chatFab = findViewById(R.id.chatFab);
        setupFloatingActionButton();

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

    @SuppressLint("ClickableViewAccessibility")
    private void setupFloatingActionButton() {
        chatFab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        isClick = true; // Assume it's a click initially
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // If the finger moves, it's no longer a pure click
                        isClick = false;

                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        // Ensure the FAB stays within screen bounds
                        // Need to cast view.getParent() to View
                        View parentView = (View) view.getParent();
                        if (parentView != null) {
                            newX = Math.max(0, Math.min(newX, parentView.getWidth() - view.getWidth()));
                            newY = Math.max(0, Math.min(newY, parentView.getHeight() - view.getHeight()));
                        } else {
                            // Fallback if parent is null
                            newX = Math.max(0, newX);
                            newY = Math.max(0, newY);
                        }


                        view.setX(newX);
                        view.setY(newY);
                        break;

                    case MotionEvent.ACTION_UP:
                        if (isClick) {
                            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                            startActivity(intent);
                        }
                        break;

                    case MotionEvent.ACTION_CANCEL:
                        isClick = false;
                        break;

                    default:
                        return false;
                }

                return true;
            }
        });
    }
}