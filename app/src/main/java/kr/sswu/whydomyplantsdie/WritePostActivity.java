package kr.sswu.whydomyplantsdie;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.sswu.whydomyplantsdie.Model.ContentDTO;

public class WritePostActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_FROM_ALBUM = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    final private static String TAG = "WritePost";
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
    Uri imgUri;
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
        ActivityCompat.requestPermissions
                (this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(WritePostActivity.this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottomsheet_add_photo, (ViewGroup) findViewById(R.id.bottomsheet));
        bottomSheetDialog.setContentView(view);

        plantList = new ArrayList<String>();
        autoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.autoCompletetv);
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
        Log.d(TAG, "dispatchTakePicture");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Log.d(TAG, "intent");
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            Log.d(TAG, "result");
        }
    }

        @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 카메라로 사진을 찍어 이미지 가져오는 경우
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imgAddPhoto.setImageBitmap(imageBitmap);
            }

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
                firebaseStorage.getReferenceFromUrl("gs://why-do-my-plants-die.appspot.com/").child("feed").child(contentUri.getLastPathSegment());
        UploadTask uploadTask = storageRef.putFile(contentUri);

        String imagePath = "https://firebasestorage.googleapis.com/v0/b/" + "why-do-my-plants-die.appspot.com"
                + "/o/" + "feed%2F" + contentUri.getLastPathSegment().toString() + "?alt=media";

        uploadTask
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(WritePostActivity.this, "업로드 성공",
                                Toast.LENGTH_SHORT).show();

                        //디비에 바인딩 할 위치 생성 및 컬렉션(테이블)에 데이터 집합 생성
                        DatabaseReference images = firebaseDatabase.getReference().child("feed").push();

                        //시간 생성
                        Date date = new Date();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        ContentDTO contentDTO = new ContentDTO();

                        //이미지 이름
                        contentDTO.imageName = contentUri.getLastPathSegment().toString();
                        //이미지 주소
                        contentDTO.imageUrl = imagePath;
                        //유저의 UID
                        contentDTO.uid = firebaseAuth.getCurrentUser().getUid();
                        //게시물의 설명
                        contentDTO.explain = edtContent.getText().toString();
                        //유저의 아이디
                        contentDTO.userId = firebaseAuth.getCurrentUser().getEmail();
                        //유저의 아이디 앞부분
                        String shortId[] = firebaseAuth.getCurrentUser().getEmail().split("@");
                        contentDTO.userShortId = shortId[0];
                        //게시물 업로드 시간
                        contentDTO.timestamp = simpleDateFormat.format(date);
                        //식물 종류
                        contentDTO.plantKind = autoCompleteTextView.getText().toString();

                        //게시물을 데이터를 생성 및 엑티비티 종료
                        images.setValue(contentDTO);

                        setResult(RESULT_OK);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(WritePostActivity.this, "업로드 실패",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void settingPlantList(){
        plantList.add("몬스테라 델리시오사");
        plantList.add("올리브");
        plantList.add("몬스테라 아단소");
        plantList.add("블루스타 고사리");
        plantList.add("몬스테라 알보 바리에가타");
        plantList.add("피쉬본 선인장");
        plantList.add("스투키");
        plantList.add("테이블 야자");
        plantList.add("칼라디움");
        plantList.add("은엽 아카시아");
        plantList.add("산세베리아 문샤인");
        plantList.add("보스턴 고사리");
        plantList.add("뱅갈 고무나무");
        plantList.add("스파티필름");
        plantList.add("로즈마리");
        plantList.add("아레카야자");
        plantList.add("유칼립투스 폴리안");
        plantList.add("마오리 소포라");
        plantList.add("스킨답서스");
        plantList.add("필로덴드론 버킨");
        plantList.add("스위트 바질");
        plantList.add("금전수");
        plantList.add("칼라데아 오르비폴리아");
        plantList.add("헤데라(아이비)");
        plantList.add("필로덴드론 콩고");
        plantList.add("멜라니 고무나무");
        plantList.add("아스파라거스 나누스");
        plantList.add("세네시오 칸디칸스(엔젤윙)");
        plantList.add("홍콩 야자");
        plantList.add("알로카시아 오도라");
        plantList.add("유칼립투스 구니");
        plantList.add("목마가렛");
        plantList.add("알로카시아 프라이덱");
        plantList.add("호주매화(마누카)");
        plantList.add("수박 페페로미아");
        plantList.add("호야 카르노사(무늬 호야)");
        plantList.add("여인초");
        plantList.add("싱고니움");
        plantList.add("유카");
        plantList.add("켄차 야자");
        plantList.add("극락조화");
        plantList.add("율마(월마)");
        plantList.add("원숭이꼬리 선인장");
        plantList.add("필로덴드론 셀로움");
        plantList.add("필레아 페페로미오데스");
        plantList.add("해피트리");
        plantList.add("개운죽");
        plantList.add("애니시다");
        plantList.add("사계귤(유주나무)");
        plantList.add("코로키아");
        plantList.add("떡갈잎 고무나무");
        plantList.add("브레이니아 니보사(소코라코)");
        plantList.add("박쥐란");
        plantList.add("알로카시아 아마조니카");
        plantList.add("파키라");
        plantList.add("드라세나 드라코(용혈수)");
        plantList.add("오렌지 자스민");
        plantList.add("백묘국");
        plantList.add("동백나무");
        plantList.add("접란(클로로피텀)");
        plantList.add("산세베리아");
        plantList.add("용신목");
        plantList.add("드라세나 마지나타");
        plantList.add("라벤더");
        plantList.add("립살리스 트리고나");
        plantList.add("다바나 고사리");
        plantList.add("인도 고무나무");
        plantList.add("관음죽");
        plantList.add("아라우카리아");
        plantList.add("마란타 레우코네우라");
        plantList.add("페퍼민트");
        plantList.add("아비스");
        plantList.add("장미허브");
        plantList.add("알로에 베라");
        plantList.add("마삭줄");
        plantList.add("장미");
        plantList.add("티트리");
        plantList.add("아이비");
        plantList.add("석송");
        plantList.add("선인장");
        plantList.add("마리모");
        plantList.add("프리지아");
        plantList.add("카네이션");
        plantList.add("체리 세이지");
        plantList.add("녹탑");







    }
}


