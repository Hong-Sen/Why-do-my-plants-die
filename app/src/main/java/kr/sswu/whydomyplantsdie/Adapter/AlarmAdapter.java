package kr.sswu.whydomyplantsdie.Adapter;
/*
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import kr.sswu.whydomyplantsdie.Model.AlarmModel;
import kr.sswu.whydomyplantsdie.R;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private Context ctx;
    private List<AlarmModel> alarmList;
    private String mUid;

    private DatabaseReference alarmRef;

    public AlarmAdapter(Context ctx, List<AlarmModel> alarmList) {
        this.ctx = ctx;
        this.alarmList = alarmList;

        mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //alarmRef = FirebaseDatabase.getInstance().getReference().child("Alarms");
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.alarm_item, parent, false);

        return new AlarmViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        AlarmModel tmp = alarmList.get(position);

        String fid = tmp.getFid();
        String uName = tmp.getName();
        String fImg = tmp.getImage();

        try {
            Picasso.get().load(fImg).into(holder.aImage);
        } catch (Exception e) {

        }


    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    class AlarmViewHolder extends RecyclerView.ViewHolder {
        ImageView aImage;

        public AlarmViewHolder(View itemView) {
            super(itemView);

            aImage = itemView.findViewById(R.id.imageView);
        }
    }
}

 */