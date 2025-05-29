package hcmute.edu.vn.healthtracking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.healthtracking.R;
import hcmute.edu.vn.healthtracking.models.WorkoutHistory;

public class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.ViewHolder> {
    
    private final List<WorkoutHistory> workoutHistoryList;
    private final Context context;
    private final boolean isRunningWorkout;
    
    public WorkoutHistoryAdapter(Context context, List<WorkoutHistory> workoutHistoryList, boolean isRunningWorkout) {
        this.context = context;
        this.workoutHistoryList = workoutHistoryList;
        this.isRunningWorkout = isRunningWorkout;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.workout_history_item, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutHistory workout = workoutHistoryList.get(position);
        
        holder.titleTextView.setText(workout.getTitle());
        holder.dateTextView.setText(workout.getDate());
        holder.distanceTextView.setText(String.format(Locale.getDefault(), "%.2f km", workout.getDistance()));
        holder.timeTextView.setText(workout.getDuration());
        
        // Set appropriate icon based on workout type
        if (isRunningWorkout) {
            holder.iconImageView.setImageResource(R.drawable.ic_running);
        } else {
            holder.iconImageView.setImageResource(R.drawable.ic_cycling);
        }
        
        // Set click listener for the item
        holder.itemView.setOnClickListener(v -> {
            // Handle click on workout history item
            // Could navigate to details screen
        });
    }
    
    @Override
    public int getItemCount() {
        return workoutHistoryList.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView titleTextView;
        TextView dateTextView;
        TextView distanceTextView;
        TextView timeTextView;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            
            iconImageView = itemView.findViewById(R.id.workout_icon);
            titleTextView = itemView.findViewById(R.id.workout_title);
            dateTextView = itemView.findViewById(R.id.workout_date);
            distanceTextView = itemView.findViewById(R.id.workout_distance);
            timeTextView = itemView.findViewById(R.id.workout_time);
        }
    }
} 