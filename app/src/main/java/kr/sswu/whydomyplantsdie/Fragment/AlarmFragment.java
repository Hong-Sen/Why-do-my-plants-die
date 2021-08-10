package kr.sswu.whydomyplantsdie.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import kr.sswu.whydomyplantsdie.AlarmUploadActivity;
import kr.sswu.whydomyplantsdie.Model.AlarmModel;
import kr.sswu.whydomyplantsdie.R;
import kr.sswu.whydomyplantsdie.databinding.ItemAlarmBinding;

public class AlarmFragment extends Fragment {

    public static final String ex = "btnOnOff";
    SharedPreferences sharedPreferences;
    private String curUid;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<AlarmModel> alarmList;
    private FirebaseDatabase database;
    private FirebaseStorage firebaseStorage;
    private DatabaseReference databaseReference;
    //feed upload btn
    private FloatingActionButton btnaddAlarm;
    //alarm on-off
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch btnOnOff;
    private View view;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_alarm, container, false);

        curUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        btnaddAlarm = view.findViewById(R.id.btn_addAlarm);
        btnOnOff = (Switch) view.findViewById(R.id.itemAlarm_btn_onoff);

        recyclerView = (RecyclerView) view.findViewById(R.id.alarm_recyclerview);
        recyclerView.setHasFixedSize(true); // 리사이클러뷰 기존성능 강화
        layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new AlarmAdapter()); // 리사이클러뷰에 어댑터 연결

        loadAlarm();

        alarmList = new ArrayList<>(); // User 객체를 담을 어레이 리스트 (어댑터쪽으로)

        database = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
        firebaseStorage = FirebaseStorage.getInstance();
//        databaseReference = database.getReference("alarmList"); // DB 테이블 연결
//        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                // 파이어베이스 데이터베이스의 데이터를 받아오는 곳
//                alarmList.clear(); // 기존 배열리스트가 존재하지않게 초기화
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) { // 반복문으로 데이터 List를 추출해냄
//                    AlarmModel alarmModel = snapshot.getValue(AlarmModel.class); // 만들어뒀던 User 객체에 데이터를 담는다.
//                    alarmList.add(alarmModel); // 담은 데이터들을 배열리스트에 넣고 리사이클러뷰로 보낼 준비
//                }
//                adapter.notifyDataSetChanged(); // 리스트 저장 및 새로고침해야 반영이 됨
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                // 디비를 가져오던중 에러 발생 시
//                Log.e("AlarmFragment", String.valueOf(databaseError.toException())); // 에러문 출력
//            }
//        });

        //alarm upload activity로 이동
        btnaddAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AlarmUploadActivity.class);
                startActivity(intent);
            }
        });

        //fcm cloud messaging token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FCM Log", "getInstanceId failed", task.getException());
                            return;
                        }
                        String token = task.getResult();

                        Log.d("FCM Log", "FCM 토큰: " + token);
                    }
                });

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadAlarm() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Alarm");
        ref.orderByChild("uid").equalTo(curUid).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                alarmList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String uid = ds.child("uid").getValue().toString();
                    String userid = ds.child("userid").getValue().toString();
                    String imageUrl = ds.child("imageUrl").getValue().toString();
                    String plantName = ds.child("plantName").getValue().toString();
                    String water = ds.child("water").getValue().toString();
                    String cycle = ds.child("cycle").getValue().toString();

                    AlarmModel item = new AlarmModel();

                    alarmList.add(item);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class AlarmAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final ArrayList<AlarmModel> alarmList;
        private final ArrayList<String> uidList;

        AlarmAdapter() {
            alarmList = new ArrayList<>();
            uidList = new ArrayList<>();

            FirebaseDatabase.getInstance().getReference().child("alarm").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    alarmList.clear();
                    uidList.clear();

                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        alarmList.add(snapshot1.getValue(AlarmModel.class));
                        uidList.add(snapshot1.getKey());
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final ItemAlarmBinding binding = ((CustomViewHolder) holder).getBinding();

            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));

            Glide.with(holder.itemView.getContext())
                    .load(alarmList.get(position).getImageUrl())
                    .centerCrop()
                    .apply(requestOptions).into((binding.itemAlarmImage));
            binding.itemAlarmPlantName.setText(alarmList.get(position).getPlantName());
            binding.itemAlarmWater.setText(alarmList.get(position).getWater());
            binding.itemAlarmCycle.setText(alarmList.get(position).getCycle() + "일 주기");
            //(CustomViewHolder)holder).btnOnOff.setOnCheckedChangeListener(new View.) { };
        }

        @Override
        public int getItemCount() {
            return (alarmList != null ? alarmList.size() : 0);
        }

        public class CustomViewHolder extends RecyclerView.ViewHolder {

            private ItemAlarmBinding binding;

            public CustomViewHolder(@NonNull View itemView) {
                super(itemView);
                binding = DataBindingUtil.bind(itemView);
            }

            ItemAlarmBinding getBinding() {
                return binding;
            }
        }
    }
}