package hcmute.edu.vn.healthtracking.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hcmute.edu.vn.healthtracking.R;
import hcmute.edu.vn.healthtracking.activities.UploadActivity;
import hcmute.edu.vn.healthtracking.adapters.MediaAdapter;
import hcmute.edu.vn.healthtracking.models.Media;

public class UploadFragment extends Fragment {
    private ExtendedFloatingActionButton addMediaButton;
    private RecyclerView mediaRecyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MediaAdapter mediaAdapter;
    private List<Media> mediaList = new ArrayList<>();
    private ValueEventListener mediaListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        // Initialize components
        addMediaButton = view.findViewById(R.id.addMediaFloatingButton);
        mediaRecyclerView = view.findViewById(R.id.mediaRecyclerView);
        progressBar = view.findViewById(R.id.progressBarMedia);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Setup RecyclerView
        mediaRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mediaAdapter = new MediaAdapter(getContext(), mediaList);
        mediaRecyclerView.setAdapter(mediaAdapter);

        // Setup scroll listener for FAB animation
        mediaRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    addMediaButton.shrink();
                } else if (dy < 0) {
                    addMediaButton.extend();
                }
            }
        });

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchMediaFromCloud("1"); // Replace with dynamic ownerId if needed
        });

        // Events
        addMediaButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), UploadActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void fetchMediaFromCloud(String ownerId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("medias");
        mediaListener = ref.orderByChild("ownerId").equalTo(ownerId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mediaList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Media media = child.getValue(Media.class);
                    if (media != null) {
                        mediaList.add(media);
                    } else {
                        Log.w("Firebase", "Failed to parse media for key: " + child.getKey());
                    }
                }
                Collections.sort(mediaList, (m1, m2) -> {
                    if (m1.getDateCreated() == null || m2.getDateCreated() == null) return 0;
                    return m2.getDateCreated().compareTo(m1.getDateCreated());
                });
                
                mediaAdapter.notifyDataSetChanged();
                progressBar.setVisibility(GONE);
                swipeRefreshLayout.setRefreshing(false);
                
                // Update empty state visibility
                if (mediaList.isEmpty()) {
                    mediaRecyclerView.setVisibility(GONE);
                    emptyStateLayout.setVisibility(VISIBLE);
                } else {
                    mediaRecyclerView.setVisibility(VISIBLE);
                    emptyStateLayout.setVisibility(GONE);
                }
                
                Log.d("Firebase", "Fetched " + mediaList.size() + " media items for ownerId: " + ownerId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Listener cancelled: ", error.toException());
                progressBar.setVisibility(GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading media: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        progressBar.setVisibility(VISIBLE);
        fetchMediaFromCloud("1"); // Replace with dynamic ownerId if needed
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mediaListener != null) {
            FirebaseDatabase.getInstance().getReference("medias").removeEventListener(mediaListener);
            mediaListener = null;
        }
    }
}