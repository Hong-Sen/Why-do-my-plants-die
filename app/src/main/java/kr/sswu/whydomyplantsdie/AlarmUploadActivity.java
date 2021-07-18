package kr.sswu.whydomyplantsdie;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import kr.sswu.whydomyplantsdie.Login.LoginActivity;

public class AlarmUploadActivity extends AppCompatActivity {

    final private static String TAG = "";

    private static final int GALLERY_ACTION_CODE = 1;
    private static final int CAMERA_ACTION_CODE = 2;

    private static final int REF_GALLERY_ACTION_CODE = 3;
    private static final int REF_CAMERA_ACTION_CODE = 4;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;

    private String name, email, uid;

    private BottomSheetDialog bottomSheetDialog, bottomCheckDialog;

    private String mCurrentPhotoPath;
    private Uri imageUri, imageRefUri;

    private TextView btn_upload;
    private ImageView btn_close, imageView, add;

    private ProgressDialog progressDialog;

    //Edit
    private String editDate, editImage;
    String isEditKey, editAlarmId;
    private boolean isEditRef = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_acticity);

        //get user info
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        //checkUser();

        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference("Users");
        Query query = reference.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    name = ds.child("name").getValue().toString();
                    email = ds.child("email").getValue().toString();
                    uid = ds.child("uid").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        btn_close = (ImageView) findViewById(R.id.btn_close);
        btn_upload = (TextView) findViewById(R.id.btn_upload);
        imageView = (ImageView) findViewById(R.id.imageView);
        add = (ImageView) findViewById(R.id.img_add);

        progressDialog = new ProgressDialog(this);

        //edited
        Intent intent = getIntent();
        if (intent != null) {
            isEditKey = intent.getStringExtra("key");
            editAlarmId = intent.getStringExtra("editAlarmId");
        }

        if (isEditKey == null) {
            btn_upload.setText("Upload");
        } else if (isEditKey.equals("edit")) {
            btn_upload.setText("Update");
            imageView.setEnabled(false);
            //loadAlarmData(editAlarmId);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "권한 설정 완료");
            } else {
                Log.d(TAG, "권한 설정 요청");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}

        /*
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEditKey == null) {
                    if (imageUri != null) {
                        if (imageRefUri == null)
                            uploadToFirebase(imageUri);
                        else
                            uploadToFirebase(imageUri, imageRefUri);
                    } else
                        Toast.makeText(getApplicationContext(), "Select image", Toast.LENGTH_SHORT).show();
                } else if (isEditKey.equals("edit"))
                    if (isEditRef == true)
                        if (imageRefUri == null) {
                            updateToFirebase(edit_title,"", editAlarmId);
                        } else {
                            if (editAlarmRef.equals(""))
                                updateToFirebaseNullRef(edit_title, imageRefUri, editAlarmId);
                            else
                                updateToFirebaseWithRef(edit_title, imageRefUri, editAlarmId);
                        }
                    else
                        updateToFirebase(edit_title, editAlarmRef, editAlarmId);
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bottomSheetDialog = new BottomSheetDialog(AlarmUploadActivity.this, R.style.BottomSheetTheme);

                View sheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottom_sheet_photo_layout,
                        (ViewGroup) findViewById(R.id.bottom_sheet));

                sheetView.findViewById(R.id.getCamera).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //카메라로 사진 가져오기
                        dispatchTakePhotoIntent(CAMERA_ACTION_CODE);
                    }
                });

                sheetView.findViewById(R.id.getGallery).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 앨범에서 사진 가져오기
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, GALLERY_ACTION_CODE);

                        bottomSheetDialog.dismiss();
                    }
                });
                bottomSheetDialog.setContentView(sheetView);
                bottomSheetDialog.show();
            }
        });

        imageView_ref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEditKey == null) {
                    showBottomSheet();
                } else {
                    bottomCheckDialog = new BottomSheetDialog(AlarmUploadActivity.this, R.style.BottomSheetTheme);
                    View checkView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottom_sheet_ref_layout,
                            (ViewGroup) findViewById(R.id.bottom_sheet));

                    TextView replace = checkView.findViewById(R.id.replace);
                    if (editAlarmRef.equals(""))
                        replace.setText("Add Photo");
                    else
                        replace.setText("Replace Photo");

                    checkView.findViewById(R.id.replace).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showBottomSheet();
                            bottomCheckDialog.dismiss();
                        }
                    });

                    checkView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            isEditRef = true;

                            imageRefUri = null;
                            ;
                            imageView_ref.setImageResource(R.drawable.image_border);
                            add_ref.setVisibility(View.VISIBLE);

                            bottomCheckDialog.dismiss();
                        }
                    });

                    bottomCheckDialog.setContentView(checkView);
                    bottomCheckDialog.show();
                }
            }
        });
    }

    private void showBottomSheet() {
        bottomSheetDialog = new BottomSheetDialog(AlarmUploadActivity.this, R.style.BottomSheetTheme);

        View sheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottom_sheet_photo_layout,
                (ViewGroup) findViewById(R.id.bottom_sheet));

        sheetView.findViewById(R.id.getCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEditKey != null)
                    isEditRef = true;
                //카메라로 사진 가져오기
                dispatchTakePhotoIntent(REF_CAMERA_ACTION_CODE);
            }
        });

        sheetView.findViewById(R.id.getGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEditKey != null)
                    isEditRef = true;
                // 앨범에서 사진 가져오기
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REF_GALLERY_ACTION_CODE);

                bottomSheetDialog.dismiss();

            }
        });
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    private void updateToFirebaseNullRef(EditText edit_title, Uri refUri, String editAlarmId) {
        String timeStamp = String.valueOf(System.currentTimeMillis());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        String filePathName = "Alarms/" + "src_" + timeStamp;

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathName);

        ref.putFile(refUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;

                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()) {
                            HashMap<String, Object> feed = new HashMap<>();

                            feed.put("title", edit_title.getText().toString());

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Feeds");
                            ref.child(editAlarmId)
                                    .updateChildren(feed)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            Toast.makeText(AlarmUploadActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(AlarmUploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AlarmUploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        finish();
    }

    private void updateToFirebaseWithRef(EditText edit_title, Uri refUri, String editAlarmId) {
        progressDialog.setMessage("Update..");
        progressDialog.show();

        StorageReference feedRef = FirebaseStorage.getInstance().getReferenceFromUrl(editAlarmRef);
        feedRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        String timeStamp = String.valueOf(System.currentTimeMillis());

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());

                        String filePathName = "Alarms/" + "src_" + timeStamp;

                        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathName);

                        ref.putFile(refUri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful()) ;

                                        String downloadUri = uriTask.getResult().toString();

                                        if (uriTask.isSuccessful()) {
                                            HashMap<String, Object> alarm = new HashMap<>();

                                            alarm.put("title", edit_title.getText().toString());

                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Alarms");
                                            ref.child(editAlarmId)
                                                    .updateChildren(alarm)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(AlarmUploadActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(AlarmUploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(AlarmUploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AlarmUploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        progressDialog.dismiss();
        finish();
    }

    private void updateToFirebase(EditText edit_title, String refUri, String editAlarmId) {
        progressDialog.setMessage("Update..");
        progressDialog.show();

        HashMap<String, Object> alarm = new HashMap<>();

        alarm.put("title", edit_title.getText().toString());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Alarms");
        ref.child(editAlarmId)
                .updateChildren(alarm)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(AlarmUploadActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AlarmUploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        finish();
    }

    private void loadAlarmData(String editAlarmId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Alarms");
        DownloadManager.Query query = ref.orderByChild("fid").equalTo(editAlarmId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    editTitle = ds.child("title").getValue().toString();
                    editImage = ds.child("image").getValue().toString();

                    edit_title.setText(editTitle);

                    add.setVisibility(View.INVISIBLE);
                    if (!editRef.equals(""))
                        add_ref.setVisibility(View.INVISIBLE);

                    try {
                        Picasso.get().load(editImage).into(imageView);
                    } catch (Exception e) {

                    }

                    if (!editRef.equals(""))
                        try {
                            Picasso.get().load(editRef).into(imageView_ref);
                        } catch (Exception e) {

                        }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUser();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUser();
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            email = user.getEmail();
            uid = user.getUid();
        } else {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionResult");
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //앨범에서 이미지 가져오는 경우 //image
        if (requestCode == GALLERY_ACTION_CODE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }

        // 카메라로 사진을 찍어 이미지 가져오는 경우 // image
        if (requestCode == CAMERA_ACTION_CODE && resultCode == RESULT_OK) {
            try {
                File file = new File(mCurrentPhotoPath);
                Bitmap bitmap;
                if (Build.VERSION.SDK_INT >= 29) {
                    ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), Uri.fromFile(file));

                    try {
                        bitmap = ImageDecoder.decodeBitmap(source);
                        if (bitmap != null) {
                            CropImage.activity(imageUri)
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .start(this);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));
                        if (bitmap != null) {
                            CropImage.activity(imageUri)
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .start(this);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception error) {
                error.printStackTrace();
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                imageView.setBackgroundColor(Color.WHITE);
                imageView.setImageURI(imageUri);
                add.setVisibility(View.GONE);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void dispatchTakePhotoIntent(int code) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;

        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "이미지 처리 오류 발생. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            ex.printStackTrace();
        }
        if (photoFile != null) {
            if (code == CAMERA_ACTION_CODE) {
                imageUri = FileProvider.getUriForFile(this, "kr.sswu.whydomyplantsdie.fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, code);
                bottomSheetDialog.dismiss();
            } else if (code == REF_CAMERA_ACTION_CODE) {
                imageRefUri = FileProvider.getUriForFile(this, "kr.sswu.whydomyplantsdie.fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageRefUri);
                startActivityForResult(intent, code);
                bottomSheetDialog.dismiss();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void uploadToFirebase(Uri uri) {
        String timeStamp = String.valueOf(System.currentTimeMillis());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        String fTime = DateFormat.format("yyyy-MM-dd hh:mm aa", calendar).toString();

        String filePathName = "Alarms/" + "alarms_" + timeStamp;

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathName);

        ref.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;

                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()) {
                            HashMap<String, Object> alarm = new HashMap<>();
                            alarm.put("uid", uid);
                            alarm.put("fid", timeStamp);
                            alarm.put("uName", name);
                            alarm.put("email", email);
                            alarm.put("image", downloadUri);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Alarms");
                            ref.child(timeStamp).setValue(alarm)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            Toast.makeText(AlarmUploadActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                        }
                                    });
                        }
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        progressDialog.setMessage("Upload..");
                        progressDialog.show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AlarmUploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        //alarm upload 후 Alarm 메인 회면으로 전환
        startActivity(new Intent(getApplicationContext(), MainActivity.class));

    }

    private void uploadToFirebase(Uri uri, Uri srcUri) {
        String timeStamp = String.valueOf(System.currentTimeMillis());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        String fTime = DateFormat.format("yyyy-MM-dd hh:mm aa", calendar).toString();

        String filePathName = "Alarms/" + "alarms_" + timeStamp;
        String fileSrcPathName = "Alarms/" + "src_" + timeStamp;

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathName);
        StorageReference refSrc = FirebaseStorage.getInstance().getReference().child(fileSrcPathName);

        refSrc.putFile(srcUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;

                        String downloadSrcUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()) {
                            ref.putFile(uri)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                            while (!uriTask.isSuccessful()) ;

                                            String downloadUri = uriTask.getResult().toString();

                                            if (uriTask.isSuccessful()) {
                                                HashMap<String, Object> alarm = new HashMap<>();
                                                alarm.put("uid", uid);
                                                alarm.put("fid", timeStamp);
                                                alarm.put("uName", name);
                                                alarm.put("email", email);
                                                alarm.put("image", downloadUri);

                                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Alarms");
                                                ref.child(timeStamp).setValue(alarm)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(AlarmUploadActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                progressDialog.dismiss();
                                                            }
                                                        });
                                            }
                                        }
                                    })
                                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                            progressDialog.setMessage("Upload..");
                                            progressDialog.show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(AlarmUploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(getApplicationContext(), "src UriTask failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AlarmUploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        //alarm upload 후 Alarm 메인 회면으로 전환
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }
}
*/