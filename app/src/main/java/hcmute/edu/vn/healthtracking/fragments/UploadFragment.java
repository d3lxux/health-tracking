package hcmute.edu.vn.healthtracking.fragments;

import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    FloatingActionButton addMediaFloatingActionButton;
    RecyclerView mediaRecyclerView;
    ProgressBar progressBar;
    TextView textViewNoMedia;
    MediaAdapter mediaAdapter;
    List<Media> mediaList = new ArrayList<>();
    private ValueEventListener mediaListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        // Initialize components
        addMediaFloatingActionButton = view.findViewById(R.id.addMediaFloatingButton);
        mediaRecyclerView = view.findViewById(R.id.mediaRecyclerView);
        progressBar = view.findViewById(R.id.progressBarMedia);
        textViewNoMedia = view.findViewById(R.id.textViewNoMediaFound);

        mediaRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mediaAdapter = new MediaAdapter(getContext(), mediaList);
        mediaRecyclerView.setAdapter(mediaAdapter);

        // Events
        addMediaFloatingActionButton.setOnClickListener(v -> {
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
                progressBar.setVisibility(View.GONE);
                if (mediaList.isEmpty() && getContext() != null) {
                    Toast.makeText(getContext(), "No media found", Toast.LENGTH_SHORT).show();
                    textViewNoMedia.setVisibility(VISIBLE);
                }
                Log.d("Firebase", "Fetched " + mediaList.size() + " media items for ownerId: " + ownerId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Listener cancelled: ", error.toException());
                progressBar.setVisibility(View.GONE);
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