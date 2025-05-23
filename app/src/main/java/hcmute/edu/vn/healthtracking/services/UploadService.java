package hcmute.edu.vn.healthtracking.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.UUID;

import hcmute.edu.vn.healthtracking.R;
import hcmute.edu.vn.healthtracking.models.Media;

public class UploadService extends Service {
    private static final String CHANNEL_ID = "upload_channel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Uri mediaUri = intent.getParcelableExtra("media_uri");
        String description = intent.getStringExtra("description");
        String ownerId = intent.getStringExtra("ownerId");
        boolean isVideo = intent.getBooleanExtra("isVideo", false);

        if (mediaUri == null || description == null || ownerId == null) {
            notifyFailedUpload(new IllegalArgumentException("Missing required upload info"));
            stopSelf();
            return START_NOT_STICKY;
        }

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());

        uploadToFirebase(mediaUri, description, ownerId, isVideo);

        return START_NOT_STICKY;
    }

    private void uploadToFirebase(Uri mediaUri, String description, String ownerId, boolean isVideo) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("uploads");
        String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(mediaUri));
        String id = UUID.randomUUID().toString();
        String filename = id + (ext != null ? "." + ext : "");

        StorageReference fileRef = storageRef.child(filename);
        UploadTask uploadTask = fileRef.putFile(mediaUri);

        uploadTask
                .addOnProgressListener(snapshot -> {
                    int progress = (int) (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    updateNotificationProgress(progress);
                })
                .addOnSuccessListener(
                        taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(
                                uri -> {
                                    String fileUrl = uri.toString();
                                    Media media = new Media(id, filename, fileUrl, description, ownerId, new Date(), isVideo);
                                    uploadMediaInformationToFirebase(media);
                                }))
                .addOnFailureListener(
                        this::notifyFailedUpload
                ).addOnCompleteListener(task -> stopSelf());
    }

    private void uploadMediaInformationToFirebase(Media media) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("medias");
        ref.child(media.getId()).setValue(media);
        notifySuccessUpload();
    }

    private void notifySuccessUpload() {
        Notification success = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Upload complete")
                .setContentText("Media uploaded successfully.")
                .setSmallIcon(R.drawable.ic_cloud)
                .setAutoCancel(true)
                .build();
        getSystemService(NotificationManager.class).notify(3, success);
    }

    private void notifyFailedUpload(Exception e) {
        Notification failNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Upload failed")
                .setContentText(e.getMessage())
                .setSmallIcon(R.drawable.ic_cloud)
                .setAutoCancel(true)
                .build();
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(2, failNotification);
    }

    private void updateNotificationProgress(int progress) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Uploading media")
                .setContentText("Progress: " + progress + "%")
                .setSmallIcon(R.drawable.ic_cloud)
                .setProgress(100, progress, false)
                .setOngoing(true)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Uploading media")
                .setContentText("Progress: 0%")
                .setSmallIcon(R.drawable.ic_cloud)
                .setOngoing(true).build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Upload Channel", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not used
    }
}
