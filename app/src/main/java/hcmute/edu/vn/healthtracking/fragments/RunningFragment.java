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

public class RunningFragment extends Fragment {

    private RecyclerView recentRunsRecycler;
    private WorkoutHistoryAdapter adapter;
    private List<WorkoutHistory> runningHistory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_running, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize data
        runningHistory = getRunningHistoryData();
        
        // Initialize RecyclerView for recent runs
        recentRunsRecycler = view.findViewById(R.id.recent_runs_recycler);
        recentRunsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Set up adapter
        adapter = new WorkoutHistoryAdapter(getContext(), runningHistory, true);
        recentRunsRecycler.setAdapter(adapter);
        
        // Setup click listener for start run button
        view.findViewById(R.id.start_run_button).setOnClickListener(v -> {
            // Start run tracking functionality
            startRunTracking();
        });
    }

    private void startRunTracking() {
        // Implement run tracking functionality here
        // This could launch a new activity or update UI
    }
    
    // Sample data for demonstration purposes
    private List<WorkoutHistory> getRunningHistoryData() {
        List<WorkoutHistory> history = new ArrayList<>();
        
        // Add sample running sessions
        history.add(new WorkoutHistory("Chạy buổi sáng", "25/08/2023, 06:30", 5.2, "00:32:45", 320));
        history.add(new WorkoutHistory("Chạy trong công viên", "23/08/2023, 17:15", 3.7, "00:23:12", 240));
        history.add(new WorkoutHistory("Chạy quanh hồ", "21/08/2023, 18:00", 4.5, "00:28:35", 280));
        history.add(new WorkoutHistory("Chạy buổi tối", "19/08/2023, 19:45", 6.1, "00:38:20", 390));
        
        return history;
    }
} 