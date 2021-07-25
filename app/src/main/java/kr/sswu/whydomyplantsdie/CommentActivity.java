package kr.sswu.whydomyplantsdie;

import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import kr.sswu.whydomyplantsdie.Model.ContentDTO;

public class CommentActivity extends AppCompatActivity {

    private ImageView writerImage;
    private TextView writerId;
    private TextView writerExplain;

    private ImageView send;
    private EditText message;
    private ImageView close;
    private RecyclerView recyclerView;
    private FirebaseUser user;
    private String destinationUid;
    private String imageUid;
    private String intentWirterId;
    private String intentWirterExplain;
    private String intentWirterImage;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        user = FirebaseAuth.getInstance().getCurrentUser();

        destinationUid = getIntent().getStringExtra("destinationUid");
        imageUid = getIntent().getStringExtra("imageUid");
        intentWirterId = getIntent().getStringExtra("writerId");
        intentWirterExplain = getIntent().getStringExtra("writerExplain");
        //intentWirterImage = getIntent().getStringExtra("writerImage");

        writerImage = (ImageView)findViewById(R.id.writer_image);
        writerId = (TextView)findViewById(R.id.writer_id);
        //writerImage = (ImageView)findViewById(R.id.writer_image);
        writerExplain = (TextView)findViewById(R.id.writer_explain);
        writerExplain = (TextView)findViewById(R.id.writer_explain);

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

        public recyclerViewAdapter(){
            comments = new ArrayList<>();
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
                            for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                comments.add(snapshot1.getValue(ContentDTO.Comment.class));
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
                        ImageView profileImageView = ((CustomViewHolder) holder).userImage;
                        Glide.with(holder.itemView.getContext())
                                .load(url)
                                .apply(new RequestOptions().circleCrop()).into(profileImageView);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            String name[] = comments.get(position).userId.split("@");
            ((CustomViewHolder)holder).userId.setText(name[0]);
            ((CustomViewHolder)holder).comment.setText(comments.get(position).comment);

        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder{
            public ImageView userImage;
            public TextView userId;
            public TextView comment;

            public CustomViewHolder(@NonNull View itemView) {
                super(itemView);

                userImage = (ImageView) itemView.findViewById(R.id.itemcomment_user_image);
                userId = (TextView) itemView.findViewById(R.id.itemcomment_user_id);
                comment = (TextView)itemView.findViewById(R.id.itemcomment_comment);
            }
        }
    }
}
