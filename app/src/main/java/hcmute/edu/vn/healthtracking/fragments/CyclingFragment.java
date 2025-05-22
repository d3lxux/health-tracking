package hcmute.edu.vn.healthtracking.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.healthtracking.R;
import hcmute.edu.vn.healthtracking.adapters.WorkoutHistoryAdapter;
import hcmute.edu.vn.healthtracking.models.WorkoutHistory;

public class CyclingFragment extends Fragment {

    private RecyclerView recentRidesRecycler;
    private WorkoutHistoryAdapter adapter;
    private List<WorkoutHistory> cyclingHistory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cycling, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize data
        cyclingHistory = getCyclingHistoryData();
        
        // Initialize RecyclerView for recent rides
        recentRidesRecycler = view.findViewById(R.id.recent_rides_recycler);
        recentRidesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Set up adapter
        adapter = new WorkoutHistoryAdapter(getContext(), cyclingHistory, false);
        recentRidesRecycler.setAdapter(adapter);
        
        // Setup click listener for start cycling button
        view.findViewById(R.id.start_cycling_button).setOnClickListener(v -> {
            // Start cycling tracking functionality
            startCyclingTracking();
        });
    }

    private void startCyclingTracking() {
        // Implement cycling tracking functionality here
        // This could launch a new activity or update UI
    }
    
    // Sample data for demonstration purposes
    private List<WorkoutHistory> getCyclingHistoryData() {
        List<WorkoutHistory> history = new ArrayList<>();
        
        // Add sample cycling sessions
        history.add(new WorkoutHistory("Đạp xe buổi sáng", "24/08/2023, 07:15", 12.5, "00:45:30", 450));
        history.add(new WorkoutHistory("Đạp xe đường dài", "22/08/2023, 16:00", 18.3, "01:10:20", 680));
        history.add(new WorkoutHistory("Đạp xe quanh thành phố", "20/08/2023, 08:30", 15.7, "00:58:45", 590));
        history.add(new WorkoutHistory("Đạp xe ngoại ô", "18/08/2023, 17:30", 20.2, "01:25:15", 780));
        
        return history;
    }
} 