package kr.sswu.whydomyplantsdie.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

//import kr.sswu.whydomyplantsdie.Adapter.AlarmAdapter;
import kr.sswu.whydomyplantsdie.AlarmUploadActivity;
import kr.sswu.whydomyplantsdie.Model.AlarmModel;
import kr.sswu.whydomyplantsdie.R;

public class AlarmFragment extends Fragment {

    //feed upload btn
    private FloatingActionButton fab;

    //feed view
    private RecyclerView recyclerView;
    private ArrayList<AlarmModel> alarmList;

    //alarm on-off
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch sw;
    SharedPreferences sharedPreferences;
    public static final String ex = "sw";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);

        fab = view.findViewById(R.id.fab);

        recyclerView = view.findViewById(R.id.alarm_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setHasFixedSize(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        alarmList = new ArrayList<>();
        //loadAlarm();

        //alarm upload activity로 이동
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AlarmUploadActivity.class);
                startActivity(intent);
            }
        });
/*
        // 전체 알람
        private void loadAlarm() {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Alarms");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (tabLayout.getSelectedTabPosition() == 0) {
                        alarmList.clear();

                        for (DataSnapshot d : snapshot.getChildren()) {
                            String fid = d.child("fid").getValue().toString();
                            String img = d.child("image").getValue().toString();
                            String name = d.child("uName").getValue().toString();
                            String email = d.child("email").getValue().toString();
                            String title = d.child("title").getValue().toString();

                            AlarmModel feedModel = new AlarmModel(fid, img, name, email, title);

                            alarmList.add(feedModel);
                            adapter = new AlarmAdapter(getActivity(), alarmList);
                            recyclerView.setAdapter(adapter);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        return inflater.inflate(R.layout.fragment_alarm, container, false);
        
    }

    //alarm on-off
    sharedPreferences = getActivity().getSharedPreferences(" ", MODE_PRIVATE);
    final SharedPreferences.Editor editor = sharedPreferences.edit();
        sw.setChecked(sharedPreferences.getBoolean(ex, true));
        FirebaseMessaging.getInstance().subscribeToTopic("1");
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                editor.putBoolean(ex, true); // value to store
                FirebaseMessaging.getInstance().subscribeToTopic("1");
            } else {
                editor.putBoolean(ex, false); // value to store
                FirebaseMessaging.getInstance().unsubscribeFromTopic("1");
            }
            editor.commit();
        }
    });

    //fcm cloudmessage token
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
        @Override
        public void onComplete(@NonNull Task<InstanceIdResult> task) {
            if (!task.isSuccessful()) {
                Log.w("FCM Log", "getInstanceId failed", task.getException());
                return;
            }
            String token = task.getResult().getToken();

            Log.d("FCM Log", "FCM 토큰: " + token);
            //Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();

        }
        */
        return view;
    }

}