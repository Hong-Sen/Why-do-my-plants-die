package kr.sswu.whydomyplantsdie.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import kr.sswu.whydomyplantsdie.AlarmUploadActivity;
import kr.sswu.whydomyplantsdie.Model.AlarmModel;
import kr.sswu.whydomyplantsdie.R;
import kr.sswu.whydomyplantsdie.databinding.ItemAlarmBinding;

import static android.content.Context.MODE_PRIVATE;
import static android.widget.Toast.LENGTH_SHORT;

public class AlarmFragment extends Fragment {

    public static final String ex = "btnOnOff";
    SharedPreferences sharedPreferences;
    private String uid;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<AlarmModel> alarmModels;
    private FirebaseDatabase database;
    private FirebaseStorage firebaseStorage;
    private FirebaseUser user;
    private FloatingActionButton btnAddAlarm;
    private Switch btnOnOff;
    private View view;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_alarm, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        btnAddAlarm = view.findViewById(R.id.btn_addAlarm);
        recyclerView = view.findViewById(R.id.alarm_recyclerview);
        recyclerView.setHasFixedSize(true); // 리사이클러뷰 기존성능 강화
        layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new AlarmAdapter(getActivity(), alarmModels)); // 리사이클러뷰에 어댑터 연결

        alarmModels = new ArrayList<>(); // User 객체를 담을 어레이 리스트 (어댑터쪽으로)
        database = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
        firebaseStorage = FirebaseStorage.getInstance();

        //alarm upload activity로 이동
        btnAddAlarm.setOnClickListener(new View.OnClickListener() {
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

    public class AlarmAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final ArrayList<AlarmModel> alarmList;
        private final ArrayList<String> uidList;

        public AlarmAdapter(FragmentActivity activity, ArrayList<AlarmModel> alarmModels) {
            alarmList = new ArrayList<>();
            uidList = new ArrayList<>();

            FirebaseDatabase.getInstance().getReference().child("alarm").orderByChild("uid").equalTo(user.getUid()).addValueEventListener(new ValueEventListener() {
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
            btnOnOff = view.findViewById(R.id.itemAlarm_btnOnoff);
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
            binding.itemAlarmHeart.setText("입양  " + alarmList.get(position).getHeart());
            binding.itemAlarmCycle.setText(alarmList.get(position).getCycle() + " 주기");


            //fcm 토픽 제어
            sharedPreferences = getActivity().getSharedPreferences(" ", MODE_PRIVATE);
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            btnOnOff.setChecked(sharedPreferences.getBoolean(ex, true));
            FirebaseMessaging.getInstance().subscribeToTopic("90");
            btnOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        switch (alarmList.get(position).getCycle()) {
                            case "물주기":
                                break;
                            case "1일":
                                editor.putBoolean(ex, true); // value to store
                                FirebaseMessaging.getInstance().subscribeToTopic("1");
                                break;
                            case "2일":
                                editor.putBoolean(ex, true); // value to store
                                FirebaseMessaging.getInstance().subscribeToTopic("2");
                                break;
                            case "3일":
                                editor.putBoolean(ex, true); // value to store
                                FirebaseMessaging.getInstance().subscribeToTopic("3");
                                break;
                            case "5일":
                                editor.putBoolean(ex, true); // value to store
                                FirebaseMessaging.getInstance().subscribeToTopic("5");
                                break;
                            case "10일":
                                editor.putBoolean(ex, true); // value to store
                                FirebaseMessaging.getInstance().subscribeToTopic("10");
                                break;
                            case "15일":
                                editor.putBoolean(ex, true); // value to store
                                FirebaseMessaging.getInstance().subscribeToTopic("15");
                                break;
                            case "20일":
                                editor.putBoolean(ex, true); // value to store
                                FirebaseMessaging.getInstance().subscribeToTopic("20");
                                break;
                            case "한 달":
                                editor.putBoolean(ex, true); // value to store
                                FirebaseMessaging.getInstance().subscribeToTopic("30");
                                break;
                            case "두 달":
                                editor.putBoolean(ex, true); // value to store
                                FirebaseMessaging.getInstance().subscribeToTopic("60");
                                break;
                            case "세 달":
                                editor.putBoolean(ex, true); // value to store
                                FirebaseMessaging.getInstance().subscribeToTopic("90");
                                break;
                        }
                    } else {
                        switch (alarmList.get(position).getCycle()) {
                            case "물주기":
                                break;
                            case "1일":
                                editor.putBoolean(ex, false); // value to store
                                FirebaseMessaging.getInstance().unsubscribeFromTopic("1");
                                break;
                            case "2일":
                                editor.putBoolean(ex, false); // value to store
                                FirebaseMessaging.getInstance().unsubscribeFromTopic("2");
                                break;
                            case "3일":
                                editor.putBoolean(ex, false); // value to store
                                FirebaseMessaging.getInstance().unsubscribeFromTopic("3");
                                break;
                            case "5일":
                                editor.putBoolean(ex, false); // value to store
                                FirebaseMessaging.getInstance().unsubscribeFromTopic("5");
                                break;
                            case "10일":
                                editor.putBoolean(ex, false); // value to store
                                FirebaseMessaging.getInstance().unsubscribeFromTopic("10");
                                break;
                            case "15일":
                                editor.putBoolean(ex, false); // value to store
                                FirebaseMessaging.getInstance().unsubscribeFromTopic("15");
                                break;
                            case "20일":
                                editor.putBoolean(ex, false); // value to store
                                FirebaseMessaging.getInstance().unsubscribeFromTopic("20");
                                break;
                            case "한 달":
                                editor.putBoolean(ex, false); // value to store
                                FirebaseMessaging.getInstance().unsubscribeFromTopic("30");
                                break;
                            case "두 달":
                                editor.putBoolean(ex, false); // value to store
                                FirebaseMessaging.getInstance().unsubscribeFromTopic("60");
                                break;
                            case "세 달":
                                editor.putBoolean(ex, false); // value to store
                                FirebaseMessaging.getInstance().unsubscribeFromTopic("90");
                                break;
                        }
                    }
                    editor.apply();
                }
            });

            // 삭제 bottomsheet
            LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.bottomsheet_delete_post, null, false);
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
            bottomSheetDialog.setContentView(view);

            // 알람 아이템 삭제
            binding.itemAlarmLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                        bottomSheetDialog.show();

                        view.findViewById(R.id.txt_delete).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteAlarm(position);
                                bottomSheetDialog.dismiss();

                            }
                        });
                    return true;
                }
            });
        }

        private void deleteAlarm(int position) {
            firebaseStorage.getReference().child("alarm").child(alarmList.get(position).image).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

            database.getReference().child("alarm").child(uidList.get(position)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
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