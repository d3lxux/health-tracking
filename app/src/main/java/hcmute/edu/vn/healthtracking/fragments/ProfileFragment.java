package hcmute.edu.vn.healthtracking.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.RadioButton;
import android.widget.RadioGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.edu.vn.healthtracking.R;
import hcmute.edu.vn.healthtracking.database.DatabaseHelper;
import hcmute.edu.vn.healthtracking.models.UserProfile;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private CircleImageView imageProfile;
    private EditText etName, etAge, etHeight, etWeight;
    private Button btnSave;

    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private String selectedAvatarUri = null;

    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Ánh xạ view
        imageProfile = view.findViewById(R.id.image_profile);
        etName = view.findViewById(R.id.et_name);
        etAge = view.findViewById(R.id.et_age);
        etHeight = view.findViewById(R.id.et_height);
        etWeight = view.findViewById(R.id.et_weight);
        btnSave = view.findViewById(R.id.btn_save_profile);
        rgGender = view.findViewById(R.id.rg_gender);
        rbMale = view.findViewById(R.id.rb_male);
        rbFemale = view.findViewById(R.id.rb_female);

        databaseHelper = new DatabaseHelper(requireContext());

        // Tải dữ liệu hồ sơ nếu đã lưu
        loadUserProfile();

        // Chọn ảnh từ thư viện
        imageProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Nút lưu thông tin
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String ageStr = etAge.getText().toString().trim();
            String heightStr = etHeight.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim();
            String gender = rbMale.isChecked() ? "Nam" : rbFemale.isChecked() ? "Nữ" : "";

            // Kiểm tra dữ liệu
            if (name.isEmpty() || ageStr.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty() || gender.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin và chọn giới tính!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int age = Integer.parseInt(ageStr);
                float height = Float.parseFloat(heightStr);
                float weight = Float.parseFloat(weightStr);

                databaseHelper.saveUserProfile(name, age, height, weight, selectedAvatarUri, gender);
                Toast.makeText(getContext(), "Đã lưu thông tin cá nhân", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Vui lòng nhập đúng định dạng số cho tuổi, chiều cao và cân nặng!", Toast.LENGTH_SHORT).show();
            }
        });




        return view;
    }

    // Xử lý kết quả chọn ảnh từ thư viện
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            selectedAvatarUri = saveImageToInternalStorage(uri); // lưu ảnh vào bộ nhớ nội bộ và lấy đường dẫn
            if (selectedAvatarUri != null) {
                imageProfile.setImageURI(Uri.fromFile(new File(selectedAvatarUri)));
            }
        }
    }


    private String saveImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            File file = new File(requireContext().getFilesDir(), "avatar.jpg"); // ảnh được lưu trong thư mục riêng của app
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return file.getAbsolutePath(); // đường dẫn ảnh lưu để ghi vào database
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    // Tải dữ liệu hồ sơ đã lưu
    private void loadUserProfile() {
        UserProfile profile = databaseHelper.getUserProfile();
        if (profile != null) {
            etName.setText(profile.getName());
            etAge.setText(String.valueOf(profile.getAge()));
            etHeight.setText(String.valueOf(profile.getHeight()));
            etWeight.setText(String.valueOf(profile.getWeight()));

            if (profile.getAvatarUri() != null) {
                selectedAvatarUri = profile.getAvatarUri();
                File file = new File(selectedAvatarUri);
                if (file.exists()) {
                    imageProfile.setImageURI(Uri.fromFile(file));
                }
            }


            if (profile.getGender() != null) {
                if (profile.getGender().equalsIgnoreCase("Nam")) {
                    rbMale.setChecked(true);
                } else if (profile.getGender().equalsIgnoreCase("Nữ")) {
                    rbFemale.setChecked(true);
                }
            }

        }
    }
}
