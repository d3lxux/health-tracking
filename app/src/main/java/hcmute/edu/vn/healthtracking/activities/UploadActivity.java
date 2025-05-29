package hcmute.edu.vn.healthtracking.activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;

import java.io.IOException;

import hcmute.edu.vn.healthtracking.R;
import hcmute.edu.vn.healthtracking.services.UploadService;

public class UploadActivity extends AppCompatActivity {
    private TextInputEditText descriptionInput;
    private ImageView imageMediaPreview;
    private VideoView videoMediaPreview;
    private ExtendedFloatingActionButton uploadButton;
    private TextView textSelectMedia;

    private Uri mediaUri;
    private String mimeType;

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        FirebaseApp.initializeApp(this);

        // Initialize views
        descriptionInput = findViewById(R.id.editTextUploadMediaDescription);
        imageMediaPreview = findViewById(R.id.imageMediaPreview);
        videoMediaPreview = findViewById(R.id.videoMediaPreview);
        uploadButton = findViewById(R.id.uploadButton);
        textSelectMedia = findViewById(R.id.textSelectMedia);

        // Setup click listeners
        View.OnClickListener mediaClickListener = v -> selectMedia();
        imageMediaPreview.setOnClickListener(mediaClickListener);
        videoMediaPreview.setOnClickListener(mediaClickListener);
        textSelectMedia.setOnClickListener(mediaClickListener);

        uploadButton.setOnClickListener(v -> {
            if (mediaUri == null) {
                Toast.makeText(this, "Please select a media file first", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadFile();
        });

        checkAndRequestPermissions();
    }

    private final ActivityResultLauncher<Intent> mediaPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    mediaUri = result.getData().getData();
                    mimeType = getContentResolver().getType(mediaUri);
                    previewMedia(mediaUri, mimeType);
                }
            });

    private void selectMedia() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        mediaPickerLauncher.launch(Intent.createChooser(intent, "Select image or video"));
    }

    private void previewMedia(Uri uri, String type) {
        if (type == null) {
            Toast.makeText(this, "Unknown file type", Toast.LENGTH_SHORT).show();
            return;
        }

        imageMediaPreview.setVisibility(GONE);
        videoMediaPreview.setVisibility(GONE);
        textSelectMedia.setVisibility(GONE);

        if (type.startsWith("image/")) {
            try {
                Bitmap bitmap;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                    bitmap = ImageDecoder.decodeBitmap(source);
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                }
                imageMediaPreview.setImageBitmap(bitmap);
                imageMediaPreview.setVisibility(VISIBLE);
            } catch (IOException e) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                textSelectMedia.setVisibility(VISIBLE);
            }
        } else if (type.startsWith("video/")) {
            videoMediaPreview.setVideoURI(uri);
            videoMediaPreview.setVisibility(VISIBLE);
            videoMediaPreview.setBackground(null);
            videoMediaPreview.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                videoMediaPreview.start();
            });
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoMediaPreview);
            videoMediaPreview.setMediaController(mediaController);
        } else {
            Toast.makeText(this, "Unsupported media type", Toast.LENGTH_SHORT).show();
            textSelectMedia.setVisibility(VISIBLE);
        }

        // Show upload button with animation
        uploadButton.extend();
    }

    private void uploadFile() {
        String description = descriptionInput.getText() != null ? descriptionInput.getText().toString().trim() : "";

        Intent serviceIntent = new Intent(this, UploadService.class);
        serviceIntent.putExtra("media_uri", mediaUri);
        serviceIntent.putExtra("description", description);
        serviceIntent.putExtra("ownerId", "1");
        serviceIntent.putExtra("isVideo", mimeType.startsWith("video/"));
        ContextCompat.startForegroundService(this, serviceIntent);

        Toast.makeText(this, "Upload started", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean imagePermission = ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
            boolean videoPermission = ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
            boolean notificationPermission = ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;

            if (!imagePermission || !videoPermission || !notificationPermission) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                android.Manifest.permission.READ_MEDIA_IMAGES,
                                android.Manifest.permission.READ_MEDIA_VIDEO,
                                android.Manifest.permission.POST_NOTIFICATIONS
                        },
                        PERMISSION_REQUEST_CODE);
            }
        } else {
            boolean storagePermission = ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

            if (!storagePermission) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && !allPermissionsGranted(grantResults)) {
                Toast.makeText(this, "Permission denied. Cannot access media files.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted(int[] results) {
        for (int res : results) {
            if (res != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }
}
