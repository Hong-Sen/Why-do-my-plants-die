package kr.sswu.whydomyplantsdie.Fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import kr.sswu.whydomyplantsdie.CommentActivity;
import kr.sswu.whydomyplantsdie.Model.ContentDTO;
import kr.sswu.whydomyplantsdie.R;
import kr.sswu.whydomyplantsdie.WritePostActivity;
import kr.sswu.whydomyplantsdie.databinding.ItemDetailPostBinding;

import static android.widget.Toast.LENGTH_SHORT;

public class FeedFragment extends Fragment {

    private FirebaseUser user;
    private FloatingActionButton btn_addPost;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private Dialog postDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_feed, container, false);

        btn_addPost = rootView.findViewById(R.id.btn_createPost);
        btn_addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WritePostActivity.class);
                startActivity(intent);
            }
        });

        postDialog = new Dialog(rootView.getContext());
        postDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        postDialog.setContentView(R.layout.item_post_dialog);

        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.feed_recyclerview);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(rootView.getContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(new DetailRecyclerViewAdapter());

        SwipeRefreshLayout swipeRefreshLayout = rootView.findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return rootView;
    }

    private class DetailRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final ArrayList<ContentDTO> contentDTOs;
        private final ArrayList<String> contentUidList;

        DetailRecyclerViewAdapter() {
            contentDTOs = new ArrayList<>();
            contentUidList = new ArrayList<>();

            FirebaseDatabase.getInstance().getReference().child("feed").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    contentDTOs.clear();
                    contentUidList.clear();

                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        contentDTOs.add(snapshot1.getValue(ContentDTO.class));
                        contentUidList.add(snapshot1.getKey());
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detail_post, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final int finalPosition = position;
            final ItemDetailPostBinding binding = ((CustomViewHolder) holder).getBinding();

            // 식물 이미지
            Glide.with(holder.itemView.getContext())
                    .load(contentDTOs.get(position).imageUrl)
                    .fitCenter()
                    .placeholder(R.drawable.icon_loading)
                    .error(R.drawable.icon_close)
                    .into(binding.itemdetailpostPlantImage);

            // 식물 이미지 클릭시 다이얼로그 띄우기
            binding.itemdetailpostPlantImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog(contentDTOs.get(position).uid,contentDTOs.get(position).userId,contentDTOs.get(position).imageUrl,contentDTOs.get(position).plantKind,contentDTOs.get(position).timestamp,contentDTOs.get(position).explain);
                }
            });

            //좋아요 이미지
            binding.itemdetailpostLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    likeEvent(finalPosition);
                }
            });
            if (contentDTOs.get(position).LIKES.containsKey(user.getUid())) {
                binding.itemdetailpostLike.setImageResource(R.drawable.icon_green_heart);
            } else {
                binding.itemdetailpostLike.setImageResource(R.drawable.icon_before_like);
            }

            //좋아요 개수
            binding.itemdetailpostLikeCnt.setText(contentDTOs.get(position).likeCount + " ");

            // 삭제 bottomsheet
            LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.bottomsheet_delete_post, null, false);
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
            bottomSheetDialog.setContentView(view);

            // more 버튼
            binding.itemdetailpostMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (user.getEmail().equals(contentDTOs.get(position).userId)) {
                        bottomSheetDialog.show();

                        view.findViewById(R.id.txt_delete).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteContent(position);
                                bottomSheetDialog.dismiss();

                            }
                        });
                    }
                }
            });

            // 댓글
            binding.itemdetailpostComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), CommentActivity.class);
                    intent.putExtra("imageUid", contentUidList.get(position));
                    intent.putExtra("destinationUid", contentDTOs.get(finalPosition).uid);

                    intent.putExtra("writerShortId", contentDTOs.get(position).userShortId);
                    intent.putExtra("writerExplain", contentDTOs.get(position).explain);

                    startActivity(intent);
                }
            });

           // 댓글 개수
            firebaseDatabase.getReference().child("feed").child(contentUidList.get(position)).child("comments").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long commentCnt = snapshot.getChildrenCount();
                    binding.itemdetailpostCommentCnt.setText(commentCnt+"");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        private void deleteContent(int position) {
            firebaseStorage.getReference().child("feed").child(contentDTOs.get(position).imageName).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

            firebaseDatabase.getReference().child("feed").child(contentUidList.get(position)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getContext(), "삭제 완료", LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "삭제 실패", LENGTH_SHORT).show();
                }
            });


        }

        @Override
        public int getItemCount() {
            return contentDTOs.size();
        }

        private void likeEvent(int position) {
            final int finalPosition = position;
            FirebaseDatabase.getInstance().getReference("feed").child(contentUidList.get(position))
                    .runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                            ContentDTO contentDTO = currentData.getValue(ContentDTO.class);
                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            if (contentDTO == null) {
                                return Transaction.success(currentData);
                            }
                            if (contentDTO.LIKES.containsKey(uid)) {
                                contentDTO.likeCount = contentDTO.likeCount - 1;
                                contentDTO.LIKES.remove(uid);
                            } else {
                                contentDTO.likeCount = contentDTO.likeCount + 1;
                                contentDTO.LIKES.put(uid, true);

                            }
                            currentData.setValue(contentDTO);
                            return Transaction.success(currentData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

                        }
                    });
        }



        private class CustomViewHolder extends RecyclerView.ViewHolder {

            //data binding
            private ItemDetailPostBinding binding;  //error 발생시 Invalidate Cashes/Restart 실행

            CustomViewHolder(View itemView) {
                super(itemView);
                binding = DataBindingUtil.bind(itemView);
            }

            ItemDetailPostBinding getBinding() {
                return binding;
            }
        }
    }

    public void showDialog(String userUid, String userID, String plantImagePath, String plantKind, String date, String explain){
        postDialog.show();

        ImageView iv_close = postDialog.findViewById(R.id.itempostdialog_close);
        ImageView iv_userImage = postDialog.findViewById(R.id.itempostdialog_user_image);
        TextView tv_userId = postDialog.findViewById(R.id.itempostdialog_user_id);
        ImageView iv_plantImage = postDialog.findViewById(R.id.itempostdialog_plant_image);
        TextView tv_plantKind = postDialog.findViewById(R.id.itempostdialog_plant_kind);
        TextView tv_date = postDialog.findViewById(R.id.itempostdialog_date);
        TextView tv_explain = postDialog.findViewById(R.id.itempostdialog_explain);

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postDialog.dismiss();
            }
        });


        // 유저 이미지
        firebaseDatabase.getReference().child("users").child(userUid).child("profileImage").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String img = snapshot.getValue(String.class);
                if(img == null){
                    try {
                        Glide.with(getContext())
                                .load(R.drawable.icon_profile)
                                .apply(new RequestOptions().circleCrop()).into(iv_userImage);
                    } catch (Exception e) {}
                }
                else{
                    try {
                        Glide.with(getContext())
                                .load(img)
                                .apply(new RequestOptions().circleCrop()).into(iv_userImage);
                    } catch (Exception e) {}
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // 유저 아이디
        tv_userId.setText(userID);

        // 식물 이미지
        Glide.with(getContext())
                .load(plantImagePath)
                .fitCenter()
                .placeholder(R.drawable.icon_loading)
                .error(R.drawable.icon_close)
                .into(iv_plantImage);

        // 식물 종류
        tv_plantKind.setText(plantKind);

        // 게시 날짜
        tv_date.setText(date);

        // 설명
        tv_explain.setText(explain);

    }
}

