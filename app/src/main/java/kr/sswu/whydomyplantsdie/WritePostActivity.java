package kr.sswu.whydomyplantsdie;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.sswu.whydomyplantsdie.Model.ContentDTO;

public class WritePostActivity extends AppCompatActivity {

    final private static String TAG = "WritePost";
    private static final int REQUEST_TAKE_PHOTO = 101;
    private static final int PICK_IMAGE_FROM_ALBUM = 102;
    private boolean camera = false;
    private String photoUrl;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private ImageView imgAddPhoto, btnClose, imgCamera;
    private EditText edtContent;
    private Button btnAddFeed;
    private List<String> plantList;
    private AutoCompleteTextView autoCompleteTextView;

    String mCurrentPhotoPath;
    Uri photoURI;


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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "?????? ?????? ??????");
            } else {
                Log.d(TAG, "?????? ?????? ??????");
                ActivityCompat.requestPermissions(WritePostActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        ActivityCompat.requestPermissions
                (this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(WritePostActivity.this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottomsheet_add_photo, (ViewGroup) findViewById(R.id.bottomsheet));
        bottomSheetDialog.setContentView(view);

        plantList = new ArrayList<String>();
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompletetv);
        settingPlantList();
        autoCompleteTextView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, plantList));

        imgAddPhoto.bringToFront();
        imgAddPhoto.setOnClickListener(new View.OnClickListener() {
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

        btnAddFeed.setOnClickListener(new View.OnClickListener() {
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
        // ???????????? ????????? ?????? ????????? ???????????? ??????
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                imgAddPhoto.setImageURI(photoURI);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "?????? ?????? ??????", Toast.LENGTH_LONG).show();
            }
        }
        // ???????????? ????????? ???????????? ??????
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if (resultCode == RESULT_OK) {
                try {
                    photoUrl = getRealPathFromUri(data.getData());

                    Glide.with(getApplicationContext())
                            .load(photoUrl)
                            .centerCrop()
                            .into(imgAddPhoto);

                    imgCamera.setVisibility(View.INVISIBLE);
                } catch (Exception e) {

                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "?????? ?????? ??????", Toast.LENGTH_LONG).show();
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
        if (camera) {
            contentUri = Uri.fromFile(new File(mCurrentPhotoPath));
        } else {
            File file = new File(photoUrl);
            contentUri = Uri.fromFile(file);
        }
        StorageReference storageRef =
                firebaseStorage.getReferenceFromUrl("gs://why-do-my-plants-die.appspot.com/").child("feed").child(contentUri.getLastPathSegment());
        UploadTask uploadTask = storageRef.putFile(contentUri);

        String imagePath = "https://firebasestorage.googleapis.com/v0/b/" + "why-do-my-plants-die.appspot.com"
                + "/o/" + "feed%2F" + contentUri.getLastPathSegment().toString() + "?alt=media";

        uploadTask
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(WritePostActivity.this, "????????? ??????",
                                Toast.LENGTH_SHORT).show();

                        //????????? ????????? ??? ?????? ?????? ??? ?????????(?????????)??? ????????? ?????? ??????
                        DatabaseReference images = firebaseDatabase.getReference().child("feed").push();

                        //?????? ??????
                        Date date = new Date();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        ContentDTO contentDTO = new ContentDTO();

                        //????????? ??????
                        contentDTO.imageName = contentUri.getLastPathSegment().toString();
                        //????????? ??????
                        contentDTO.imageUrl = imagePath;
                        //????????? UID
                        contentDTO.uid = firebaseAuth.getCurrentUser().getUid();
                        //???????????? ??????
                        contentDTO.explain = edtContent.getText().toString();
                        //????????? ?????????
                        contentDTO.userId = firebaseAuth.getCurrentUser().getEmail();
                        //????????? ????????? ?????????
                        String shortId[] = firebaseAuth.getCurrentUser().getEmail().split("@");
                        contentDTO.userShortId = shortId[0];
                        //????????? ????????? ??????
                        contentDTO.timestamp = simpleDateFormat.format(date);
                        //?????? ??????
                        contentDTO.plantKind = autoCompleteTextView.getText().toString();

                        //???????????? ???????????? ?????? ??? ???????????? ??????
                        images.setValue(contentDTO);

                        setResult(RESULT_OK);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(WritePostActivity.this, "????????? ??????",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * ??????: https://www.ibric.org/species/plant/plantlist.php , https://www.fuleaf.com/plants
     * https://www.convertcsv.com/html-table-to-csv.htm ??? ???????????? ??????
     * asset ????????? ?????????
     */
    private void settingPlantList() {

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("plantdic.csv")));
            String line;
            while ((line = reader.readLine()) != null) {
                // ??????,??????,????????????,???????
                String name = line.split(",")[1].replace("\"", "");
                plantList.add(name);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {

        }
    }
}


