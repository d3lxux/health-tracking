package hcmute.edu.vn.healthtracking.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import com.bumptech.glide.Glide;

import hcmute.edu.vn.healthtracking.R;
import hcmute.edu.vn.healthtracking.models.Media;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.PlaybackException;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {
    private final Context context;
    private final List<Media> mediaList;

    public MediaAdapter(Context context, List<Media> mediaList) {
        this.context = context;
        this.mediaList = mediaList;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Media media = mediaList.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        holder.mediaDateTextView.setText("Uploaded: " + sdf.format(media.getDateCreated()));
        holder.mediaDescriptionTextView.setText(media.getDescription());

        if (media.isVideo()) {
            holder.mediaImageView.setVisibility(View.GONE);
            holder.mediaVideoView.setVisibility(View.VISIBLE);
            try {
                holder.player.clearMediaItems();
                MediaItem mediaItem = MediaItem.fromUri(media.getUrl());
                holder.player.setMediaItem(mediaItem);
                holder.player.setPlayWhenReady(false); // Keep manual playback
                holder.player.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);
                holder.player.prepare();
                holder.player.addListener(new com.google.android.exoplayer2.Player.Listener() {
                    @Override
                    public void onPlayerError(PlaybackException error) {
                        Log.e("MediaAdapter", "ExoPlayer error: " + error.getMessage());
                        holder.mediaVideoView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onPlaybackStateChanged(int state) {
                        Log.d("MediaAdapter", "ExoPlayer state: " + state);
                    }
                });
            } catch (Exception e) {
                holder.mediaVideoView.setVisibility(View.GONE);
                Toast.makeText(context, "Video error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            holder.mediaVideoView.setVisibility(View.GONE);
            holder.mediaImageView.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(media.getUrl())
                    .placeholder(R.drawable.bg_media_placeholder)
                    .into(holder.mediaImageView);
        }
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    @Override
    public void onViewRecycled(@NonNull MediaViewHolder holder) {
        if (holder.player != null) {
            holder.player.stop();
            holder.player.clearMediaItems();
        }
        super.onViewRecycled(holder);
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        TextView mediaDateTextView, mediaDescriptionTextView;
        ImageView mediaImageView;
        PlayerView mediaVideoView;
        ExoPlayer player; // Store ExoPlayer instance

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            mediaDateTextView = itemView.findViewById(R.id.mediaDateTextView);
            mediaDescriptionTextView = itemView.findViewById(R.id.mediaDescriptionTextView);
            mediaImageView = itemView.findViewById(R.id.mediaImageView);
            mediaVideoView = itemView.findViewById(R.id.mediaVideoView);
            // Initialize ExoPlayer once
            player = new ExoPlayer.Builder(itemView.getContext()).build();
            mediaVideoView.setPlayer(player);
        }
    }
}