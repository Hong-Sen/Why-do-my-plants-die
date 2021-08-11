package kr.sswu.whydomyplantsdie.Fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

import kr.sswu.whydomyplantsdie.Login.LoginActivity;
import kr.sswu.whydomyplantsdie.Model.ContentDTO;
import kr.sswu.whydomyplantsdie.R;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.facebook.FacebookSdk.getApplicationContext;

public class SettingFragment extends Fragment {

    private final String TAG = "Setting";
    private final int PICK_IMAGE_FROM_ALBUM = 1;
    private ImageView setting;
    private ImageView profileImage;
    private ProgressBar progressBar;
    private TextView userId;
    private String photoUrl;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private String profileImagePath;

    private boolean isDisableRefreshingProfile = false;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_setting, container, false);

        profileImage = (ImageView) rootView.findViewById(R.id.iv_profile);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        userId = (TextView) rootView.findViewById(R.id.tv_userId);
        setting = (ImageView) rootView.findViewById(R.id.iv_setting);
        firebaseStorage = FirebaseStorage.getInstance(); //Firebase storage
        firebaseDatabase = FirebaseDatabase.getInstance(); //Firebase Database
        firebaseAuth = FirebaseAuth.getInstance(); //Firebase Auth
        user = FirebaseAuth.getInstance().getCurrentUser();

        String arr[] = user.getEmail().split("@");
        userId.setText(arr[0]);

        LayoutInflater layoutInflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.bottomsheet_setting, null, false);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(view);

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.show();

                view.findViewById(R.id.tv_edit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityForResult(intent, PICK_IMAGE_FROM_ALBUM);

                        bottomSheetDialog.dismiss();
                    }
                });

                view.findViewById(R.id.tv_logout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        firebaseAuth.signOut();
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivity(intent);

                        bottomSheetDialog.dismiss();
                    }
                });

                view.findViewById(R.id.tv_help).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 앱 소개 넣기

                        bottomSheetDialog.dismiss();
                    }
                });
            }
        });

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.grid_recyclerview);
        recyclerView.setAdapter(new GridFragmentRecyclerViewAdatper());
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        return rootView;
    }

    private String getRealPathFromUri(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(getApplicationContext(), uri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String url = cursor.getString(columnIndex);
        cursor.close();
        return url;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 앨범에서 이미지 가져오는 경우
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if (resultCode == RESULT_OK) {
                try {
                    photoUrl = getRealPathFromUri(data.getData());
                    uploadProfileToFirebase();

                } catch (Exception e) {

                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "사진 선택 취소", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshProfile();
    }

    private void refreshProfile() {
        if (isDisableRefreshingProfile) {
            isDisableRefreshingProfile = false;
            return;
        }
        firebaseDatabase.getReference().child("users").child(user.getUid()).child("profileImage").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                profileImagePath = dataSnapshot.getValue(String.class);

                if (profileImagePath == null) {
                    Glide.with(getContext())
                            .load(R.drawable.icon_profile)
                            .apply(new RequestOptions().circleCrop())
                            .into(profileImage);
                } else {
                    Glide.with(getContext())
                            .load(profileImagePath)
                            .addListener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .apply(new RequestOptions().circleCrop())
                            .into(profileImage);
                }

            }
        });
    }

    private void uploadProfileToFirebase() {

        isDisableRefreshingProfile = true;
        progressBar.setVisibility(View.VISIBLE);

        File file = new File(photoUrl);
        Uri contentUri = Uri.fromFile(file);

        String timeStamp = String.valueOf(System.nanoTime());
        StorageReference storageRef =
                firebaseStorage.getReferenceFromUrl("gs://why-do-my-plants-die.appspot.com/").child("profileImage").child(user.getUid()).child(timeStamp);
        UploadTask uploadTask = storageRef.putFile(contentUri);

        String imagePath = "https://firebasestorage.googleapis.com/v0/b/" + "why-do-my-plants-die.appspot.com"
                + "/o/" + "profileImage%2F" + user.getUid() + "%2F" + timeStamp + "?alt=media";

        Log.d(TAG, "imagepath: " + imagePath);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                firebaseDatabase.getReference().child("users").child(user.getUid()).child("profileImage").setValue(imagePath);
                refreshProfile();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "프로필 사진 업데이트 실패",
                                Toast.LENGTH_SHORT).show();
                    }
                });


    }

    // Recycler View Adapter
    class GridFragmentRecyclerViewAdatper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<ContentDTO> contentDTOs;

        public GridFragmentRecyclerViewAdatper() {

            contentDTOs = new ArrayList<>();

            FirebaseDatabase.getInstance().getReference().child("feed").orderByChild("uid").equalTo(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    contentDTOs.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        contentDTOs.add(snapshot.getValue(ContentDTO.class));
                    }

                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            //현재 사이즈 뷰 화면 크기의 가로 크기의 1/3값을 가지고 오기
            int width = getResources().getDisplayMetrics().widthPixels / 3;

            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new LinearLayoutCompat.LayoutParams(width, width));

            return new CustomViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            Glide.with(holder.itemView.getContext())
                    .load(contentDTOs.get(position).imageUrl)
                    .apply(new RequestOptions().centerCrop())
                    .into(((CustomViewHolder) holder).imageView);
        }

        @Override
        public int getItemCount() {
            return contentDTOs.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;

            public CustomViewHolder(ImageView imageView) {
                super(imageView);
                this.imageView = imageView;
            }
        }
    }
}
