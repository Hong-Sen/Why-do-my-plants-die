package kr.sswu.whydomyplantsdie;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.sswu.whydomyplantsdie.Model.AlarmModel;

import static android.widget.Toast.LENGTH_SHORT;

public class AlarmUploadActivity extends AppCompatActivity {

    final private static String TAG = "AlarmUpload";
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int PICK_IMAGE_FROM_ALBUM = 2;
    private boolean camera = false;
    private String photoUrl;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private ImageView imgPhoto, btnClose, imgCamera;
    private EditText editName, editWater, editCycle, editTime;
    private Switch editBtnOnOff;
    private Button btnUpload;

    String mCurrentPhotoPath;
    Uri photoURI;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        imgPhoto = findViewById(R.id.alarm_img_photo);
        imgCamera = findViewById(R.id.img_camera);
        editName = findViewById(R.id.alarm_edit_name);
        editWater = findViewById(R.id.alarm_edit_water);
        editCycle = findViewById(R.id.alarm_edit_cycle);
        //editTime = findViewById(R.id.edit_time);
        btnClose = findViewById(R.id.btn_close);
        editBtnOnOff = findViewById(R.id.alarm_btn_onoff);
        btnUpload = findViewById(R.id.btn_upload);

        firebaseStorage = FirebaseStorage.getInstance(); //Firebase storage
        firebaseDatabase = FirebaseDatabase.getInstance(); //Firebase Database
        firebaseAuth = FirebaseAuth.getInstance(); //Firebase Auth
        ActivityCompat.requestPermissions
                (this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(AlarmUploadActivity.this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottomsheet_add_photo, (ViewGroup) findViewById(R.id.bottomsheet));
        bottomSheetDialog.setContentView(view);


        imgPhoto.bringToFront();
        imgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.show();

                view.findViewById(R.id.goCamera).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dispatchTakePictureIntent();
                        camera = true;

                        bottomSheetDialog.dismiss();
                    }
                });

                view.findViewById(R.id.goGallery).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityForResult(intent, PICK_IMAGE_FROM_ALBUM);

                        bottomSheetDialog.dismiss();
                    }
                });

            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFirebase();
                finish();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();

            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "kr.sswu.whydomyplantsdie.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 카메라로 사진을 찍어 이미지 가져오는 경우
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if(resultCode == RESULT_OK){
                imgPhoto.setImageURI(photoURI);
            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
            }
        }
        // 앨범에서 이미지 가져오는 경우
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if (resultCode == RESULT_OK) {
                try {
                    photoUrl = getRealPathFromUri(data.getData());

                    Glide.with(getApplicationContext())
                            .load(photoUrl)
                            .centerCrop()
                            .into(imgPhoto);

                    imgCamera.setVisibility(View.INVISIBLE);
                } catch (Exception e) {

                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String getRealPathFromUri(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String url = cursor.getString(columnIndex);
        cursor.close();
        return url;
    }

    private void uploadFirebase() {
        Uri contentUri;
        if(camera){
            contentUri = Uri.fromFile(new File(mCurrentPhotoPath));
        }
        else {
            File file = new File(photoUrl);
            contentUri = Uri.fromFile(file);
        }

        StorageReference storageRef =
                firebaseStorage.getReferenceFromUrl("gs://why-do-my-plants-die.appspot.com/").child("alarm").child(contentUri.getLastPathSegment());
        UploadTask uploadTask = storageRef.putFile(contentUri);

        String imagePath = "https://firebasestorage.googleapis.com/v0/b/" + "why-do-my-plants-die.appspot.com"
                + "/o/" + "alarm%2F" + contentUri.getLastPathSegment().toString() + "?alt=media";

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(AlarmUploadActivity.this, "업로드 성공",
                                Toast.LENGTH_SHORT).show();

                        //디비에 바인딩 할 위치 생성 및 컬렉션(테이블)에 데이터 집합 생성
                        DatabaseReference images = firebaseDatabase.getReference().child("alarm").push();

                        AlarmModel alarmModel = new AlarmModel();

                        //이미지 이름
                        alarmModel.image = contentUri.getLastPathSegment().toString();
                        //이미지 주소
                        alarmModel.imageUrl = imagePath;
                        //유저의 UID
                        alarmModel.uid = firebaseAuth.getCurrentUser().getUid();
                        //알람 설명
                        alarmModel.plantName = editName.getText().toString();
                        alarmModel.water = editWater.getText().toString();
                        alarmModel.cycle = editCycle.getText().toString();
                        //alarmModel.time = editTime.getText().toString();
                        //알람 온오프
                        //alarmModel.btnOnoff = editBtnOnOff.;
                        //유저의 아이디
                        alarmModel.userid = firebaseAuth.getCurrentUser().getEmail();

                        //알람 데이터 생성 및 엑티비티 종료
                        images.setValue(alarmModel);

                        setResult(RESULT_OK);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(AlarmUploadActivity.this, "업로드 실패",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteAlarm(int position) {
        firebaseDatabase.getReference().child("alarm").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("AlarmUploadActivity", "삭제완료");
                Toast.makeText(getApplicationContext(), "삭제 완료", LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("AlarmUploadActivity", "삭제실패");
                Toast.makeText(getApplicationContext(), "삭제 실패", LENGTH_SHORT).show();
            }
        });
    }

}