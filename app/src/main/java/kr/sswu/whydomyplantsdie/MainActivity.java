package kr.sswu.whydomyplantsdie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import kr.sswu.whydomyplantsdie.Fragment.AlarmFragment;
import kr.sswu.whydomyplantsdie.Fragment.FeedFragment;
import kr.sswu.whydomyplantsdie.Fragment.MbtiFragment;
import kr.sswu.whydomyplantsdie.Fragment.SearchFragment;
import kr.sswu.whydomyplantsdie.Fragment.SettingFragment;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager = getSupportFragmentManager();
     BottomNavigationView bottomNavigationView;

    private AlarmFragment alarmFragment = new AlarmFragment();
    private SearchFragment searchFragment = new SearchFragment();
    private FeedFragment feedFragment = new FeedFragment();
    private MbtiFragment mbtiFragment = new MbtiFragment();
    private SettingFragment settingFragment = new SettingFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavi);

        //첫 화면 지정
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frameLayout, alarmFragment).commitAllowingStateLoss();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                FragmentTransaction transaction = fragmentManager.beginTransaction();

                switch (item.getItemId()) {
                    case R.id.navigation_alarm:
                        transaction.replace(R.id.frameLayout, alarmFragment).commitAllowingStateLoss();
                        break;
                    case R.id.navigation_search:
                        transaction.replace(R.id.frameLayout, searchFragment).commitAllowingStateLoss();
                        break;
                    case R.id.navigation_feed:
                        transaction.replace(R.id.frameLayout, feedFragment).commitAllowingStateLoss();
                        break;
                    case R.id.navigation_mbti:
                        transaction.replace(R.id.frameLayout, mbtiFragment).commitAllowingStateLoss();
                        break;
                    case R.id.navigation_settings:
                        transaction.replace(R.id.frameLayout, settingFragment).commitAllowingStateLoss();
                        break;
                }
                return true;
            }
        });
    }
}