package kr.sswu.whydomyplantsdie;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.sswu.whydomyplantsdie.Model.ContentDTO;

public class WritePostActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_FROM_ALBUM = 101;
    final private static String TAG = "WritePost";
    private String photoUrl;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private ImageView imgAddPhoto, btnClose, imgCamera;
    private EditText edtContent;
    private Button btnAddFeed;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        imgAddPhoto = findViewById(R.id.img_addPhoto);
        edtContent = findViewById(R.id.edt_content);
        btnAddFeed = findViewById(R.id.btn_add_feed);
        btnClose = findViewById(R.id.btn_close);
        imgCamera = findViewById(R.id.img_camera);

        firebaseStorage = FirebaseStorage.getInstance(); //Firebase storage
        firebaseDatabase = FirebaseDatabase.getInstance(); //Firebase Database
        firebaseAuth = FirebaseAuth.getInstance(); //Firebase Auth
        ActivityCompat.requestPermissions
                (this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(WritePostActivity.this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottomsheet_add_photo, (ViewGroup) findViewById(R.id.bottomsheet));
        bottomSheetDialog.setContentView(view);


        imgAddPhoto.bringToFront();
        imgAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.show();

                view.findViewById(R.id.goCamera).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //
                    }
                });

                view.findViewById(R.id.goGallery).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(intent, PICK_IMAGE_FROM_ALBUM);

                        imgCamera.setVisibility(View.INVISIBLE);
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

        btnAddFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFirebase();
                finish();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if (resultCode == RESULT_OK) {
                try {
                    photoUrl = getRealPathFromUri(data.getData());
                    Glide.with(getApplicationContext())
                            .load(photoUrl)
                            .centerCrop()
                            .into(imgAddPhoto);

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
        File file = new File(photoUrl);
        Uri contentUri = Uri.fromFile(file);
        StorageReference storageRef =
                firebaseStorage.getReferenceFromUrl("gs://why-do-my-plants-die.appspot.com/").child("images").child(contentUri.getLastPathSegment());
        UploadTask uploadTask = storageRef.putFile(contentUri);
        uploadTask
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(WritePostActivity.this, "upload success",
                                Toast.LENGTH_SHORT).show();

                        @SuppressWarnings("VisibleForTests")
                        String uri = taskSnapshot.getStorage().getDownloadUrl().toString();
                        //디비에 바인딩 할 위치 생성 및 컬렉션(테이블)에 데이터 집합 생성
                        DatabaseReference images = firebaseDatabase.getReference().child("images").push();

                        //시간 생성
                        Date date = new Date();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        ContentDTO contentDTO = new ContentDTO();

                        //이미지 주소
                        contentDTO.imageUrl = uri.toString();
                        //유저의 UID
                        contentDTO.uid = firebaseAuth.getCurrentUser().getUid();
                        //게시물의 설명
                        contentDTO.explain = edtContent.getText().toString();
                        //유저의 아이디
                        contentDTO.userId = firebaseAuth.getCurrentUser().getEmail();
                        //게시물 업로드 시간
                        contentDTO.timestamp = simpleDateFormat.format(date);

                        //게시물을 데이터를 생성 및 엑티비티 종료
                        images.setValue(contentDTO);

                        setResult(RESULT_OK);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(WritePostActivity.this, "upload fail",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

