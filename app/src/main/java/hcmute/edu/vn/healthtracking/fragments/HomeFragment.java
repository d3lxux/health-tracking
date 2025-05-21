package hcmute.edu.vn.healthtracking.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import hcmute.edu.vn.healthtracking.R;

public class HomeFragment extends Fragment {
    private TextView tvCalories, tvSteps, tvActiveMinutes;
    private ProgressBar progressBar;
    private ImageButton btnAdd;
    private RecyclerView rvHealthTiles;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize UI components
        tvCalories = view.findViewById(R.id.tv_calories);
        tvSteps = view.findViewById(R.id.tv_steps);
        tvActiveMinutes = view.findViewById(R.id.tv_active_minutes);
        progressBar = view.findViewById(R.id.progress_bar);
        btnAdd = view.findViewById(R.id.btn_add);


        // Sample data (to be replaced with real data from a health tracker)
        updateMetrics(0, 6, 0, 25);

        // Add button click listener (placeholder for adding data)
        btnAdd.setOnClickListener(v -> {
            // Implement add data functionality here
        });

        return view;
    }

    private void updateMetrics(int calories, int steps, int activeMinutes, int progress) {
        tvCalories.setText(String.valueOf(calories));
        tvSteps.setText(String.valueOf(steps));
        tvActiveMinutes.setText(String.valueOf(activeMinutes));
        progressBar.setProgress(progress);
    }
}
