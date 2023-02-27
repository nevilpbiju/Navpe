package com.example.navpe.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

import com.example.navpe.Adapters.SlideshowAdapter;
import com.example.navpe.R;

import java.util.Timer;
import java.util.TimerTask;

public class WalletBalance extends AppCompatActivity {
    ViewPager2 viewPager;
    SlideshowAdapter slideshowAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_balance);

        findViewById(R.id.back5).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.faq_help5).setOnClickListener(v -> {
            Intent helpIntent = new Intent(WalletBalance.this, FAQ.class);
            startActivity(helpIntent);
        });

        viewPager = findViewById(R.id.viewpager2);
        slideshowAdapter = new SlideshowAdapter(this);
        viewPager.setAdapter(slideshowAdapter);
        viewPager.setBackgroundColor(Color.TRANSPARENT);
        automateViewPagerSwiping();
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }
    // For View Pager automatic slider
    private void automateViewPagerSwiping() {
        final long DELAY_MS = 1500;
        final long PERIOD_MS = 6000;
        final Handler handler = new Handler();
        final Runnable update = () -> {
            if (viewPager.getCurrentItem() == slideshowAdapter.getItemCount() - 1) {
                viewPager.setCurrentItem(0);
            } else {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            }
        };
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(update);
            }
        }, DELAY_MS, PERIOD_MS);
    }
}