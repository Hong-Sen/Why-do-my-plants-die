package kr.sswu.whydomyplantsdie;

import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import kr.sswu.whydomyplantsdie.Model.ContentDTO;
import kr.sswu.whydomyplantsdie.databinding.ItemCommentBinding;

import static android.widget.Toast.LENGTH_SHORT;

public class CommentActivity extends AppCompatActivity {

    private ImageView writerImage;
    private TextView writerId;
    private TextView writerExplain;

    private ImageView send;
    private EditText message;
    private ImageView close;
    private RecyclerView recyclerView;
    private String user;
    private String destinationUid;
    private String imageUid;
    private String intentWirterId;
    private String intentWirterExplain;
    private String intentWirterImage;
    private FirebaseDatabase firebaseDatabase;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        firebaseDatabase = FirebaseDatabase.getInstance();

        destinationUid = getIntent().getStringExtra("destinationUid");
        imageUid = getIntent().getStringExtra("imageUid");
        intentWirterId = getIntent().getStringExtra("writerShortId");
        intentWirterExplain = getIntent().getStringExtra("writerExplain");
        //intentWirterImage = getIntent().getStringExtra("writerImage");

        writerImage = (ImageView)findViewById(R.id.writer_image);
        writerId = (TextView)findViewById(R.id.writer_id);
        writerExplain = (TextView)findViewById(R.id.writer_explain);
        //writerImage = (ImageView)findViewById(R.id.writer_image);

        writerId.setText(intentWirterId);
        writerExplain.setText(intentWirterExplain);

        message = (EditText)findViewById(R.id.comment_message_edt);

        close = (ImageView)findViewById(R.id.btn_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        send = (ImageView)findViewById(R.id.comment_send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentDTO.Comment comment = new ContentDTO.Comment();
                comment.userId = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                comment.comment = message.getText().toString();
                comment.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                FirebaseDatabase.getInstance()
                        .getReference("feed")
                        .child(imageUid)
                        .child("comments")
                        .push()
                        .setValue(comment);
                message.setText("");
            }
        });

        recyclerView = (RecyclerView)findViewById(R.id.comment_recyclerview);
        recyclerView.setAdapter(new recyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    class recyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private ArrayList<ContentDTO.Comment> comments;
        private  ArrayList<String> contentUidList;

        public recyclerViewAdapter(){
            comments = new ArrayList<>();
            contentUidList = new ArrayList<>();
            FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child("feed")
                    .child(imageUid)
                    .child("comments")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            comments.clear();
                            contentUidList.clear();
                            for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                comments.add(snapshot1.getValue(ContentDTO.Comment.class));
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment,parent,false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final ItemCommentBinding binding = ((CustomViewHolder)holder).getBinding();

            FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child("profileImages")
                    .child(comments.get(position).uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {

                        @SuppressWarnings("VisibleForTests")
                        String url = snapshot.getValue().toString();

                        Glide.with(holder.itemView.getContext())
                                .load(url)
                                .apply(new RequestOptions().circleCrop()).into(binding.itemcommentUserImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            // 유저 short 아이디
            String name[] = comments.get(position).userId.split("@");
            binding.itemcommentUserId.setText(name[0]);

            //유저 댓글
            binding.itemcommentComment.setText(comments.get(position).comment);

            // 삭제 bottomsheet
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(CommentActivity.this);
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottomsheet_delete_post, (ViewGroup) findViewById(R.id.bottomsheet));
            bottomSheetDialog.setContentView(view);

            // 댓글 layout
            binding.itemcommentLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(user.equals(comments.get(position).userId)){
                        bottomSheetDialog.show();

                        view.findViewById(R.id.txt_delete).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteContent(position);
                                bottomSheetDialog.dismiss();

                            }
                        });
                    }
                    return true;
                }
            });
        }

        private void deleteContent(int position){
            firebaseDatabase.getReference().child("feed").child(imageUid).child("comments").child(contentUidList.get(position)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("CommentActivity", "삭제완료");
                    Toast.makeText(getApplicationContext(), "삭제 완료", LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("CommentActivity", "삭제실패");
                    Toast.makeText(getApplicationContext(), "삭제 실패", LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder{
            
            private ItemCommentBinding binding;

            public CustomViewHolder(@NonNull View itemView) {
                super(itemView);
                binding = DataBindingUtil.bind(itemView);
            }
            ItemCommentBinding getBinding() {return binding;}
        }
    }
}
