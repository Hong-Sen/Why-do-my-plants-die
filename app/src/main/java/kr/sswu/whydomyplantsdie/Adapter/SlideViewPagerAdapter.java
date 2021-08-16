package kr.sswu.whydomyplantsdie.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import kr.sswu.whydomyplantsdie.Login.LoginActivity;
import kr.sswu.whydomyplantsdie.R;
import kr.sswu.whydomyplantsdie.SlideActivity;

public class SlideViewPagerAdapter extends PagerAdapter {
    Context ctx;

    public SlideViewPagerAdapter(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        return 6;
    }

    @Override
    public boolean isViewFromObject(@NonNull  View view, @NonNull Object object) {
        return view==object;
    }

    @NonNull

    @Override
    public Object instantiateItem(@NonNull  ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) ctx.getSystemService(ctx.LAYOUT_INFLATER_SERVICE);
        View view= layoutInflater.inflate(R.layout.slide_screen,container,false);

        ImageView background=view.findViewById(R.id.background);

        ImageView tutorialimage=view.findViewById(R.id.tutorialimage);


        ImageView next=view.findViewById(R.id.ic_next);


        Button btnGetStarted=view.findViewById(R.id.btnGetStarted);
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ctx, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(intent);
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SlideActivity.viewPager.setCurrentItem(position+1);
            }
        });



        switch (position)
        {
            case 0:

                background.setImageResource(R.drawable.backgreen);
                tutorialimage.setImageResource(R.drawable.tutorial_image1_3);

                next.setVisibility(View.VISIBLE);
                break;


            case 1:

                background.setImageResource(R.drawable.backgreen);
                tutorialimage.setImageResource(R.drawable.tutorial_image_2);

                next.setVisibility(View.VISIBLE);
                break;


            case 2:
                background.setImageResource(R.drawable.backdeepgreen);
                tutorialimage.setImageResource(R.drawable.tutorial_alarm);

                next.setVisibility(View.VISIBLE);
                break;

            case 3:
                background.setImageResource(R.drawable.backdeepgreen);
                tutorialimage.setImageResource(R.drawable.tutorial_search);

                next.setVisibility(View.VISIBLE);
                break;


            case 4:
                background.setImageResource(R.drawable.backdeepgreen);
                tutorialimage.setImageResource(R.drawable.tutorial_feed);

                next.setVisibility(View.VISIBLE);
                break;


            case 5:
                background.setImageResource(R.drawable.backdeepgreen);
                tutorialimage.setImageResource(R.drawable.tutorial_mbti);

                next.setVisibility(View.VISIBLE);
                break;



        }
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
