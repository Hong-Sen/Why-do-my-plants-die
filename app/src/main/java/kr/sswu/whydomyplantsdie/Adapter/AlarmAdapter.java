package kr.sswu.whydomyplantsdie.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import kr.sswu.whydomyplantsdie.Model.AlarmModel;
import kr.sswu.whydomyplantsdie.R;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private ArrayList<AlarmModel> alarmList;
    private Context context;

    public AlarmAdapter(ArrayList<AlarmModel> alarmList, Context context) {
        this.alarmList = alarmList;
        this.context = context;
    }

    @NonNull
    @Override
    public AlarmAdapter.AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmAdapter.AlarmViewHolder holder, int position) {
        Glide.with(holder.itemView.getContext())
                .load(alarmList.get(position).getImageUrl())
                .centerCrop()
                .into(holder.itemAlarmImage);

        holder.itemAlarmName.setText(alarmList.get(position).getPlantName());
        //holder.btnOnOff.setOnCheckedChangeListener(new View.) { };

    }

    @Override
    public int getItemCount() {
        return (alarmList != null ? alarmList.size() : 0);
    }

    public class AlarmViewHolder extends RecyclerView.ViewHolder {
        ImageView itemAlarmImage;
        TextView itemAlarmName;
        Switch btnOnOff;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemAlarmImage = itemView.findViewById(R.id.alarm_img_photo);
            this.itemAlarmName = itemView.findViewById(R.id.alarm_edit_name);
            this.btnOnOff = itemView.findViewById(R.id.alarm_btn_onoff);
        }
    }
}