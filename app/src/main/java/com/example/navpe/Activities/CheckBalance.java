package com.example.navpe.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.navpe.Adapters.SlideshowAdapter;
import com.example.navpe.Adapters.UPIAdapter;
import com.example.navpe.Models.GetAccount;
import com.example.navpe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class CheckBalance extends AppCompatActivity {

    ArrayList<GetAccount> getAccountList = new ArrayList<>();
    FirebaseUser user;
    FirebaseAuth auth;
    String uid;
    Timer timer;
    ImageView back;
    TextView t1;
    ViewPager2 viewPager;
    SlideshowAdapter slideshowAdapter;
    RecyclerView UPIRecycler;
    LinearLayout l12;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_balance);
        t1 = findViewById(R.id.textView6);
        UPIRecycler = findViewById(R.id.UPIRecycler);
        l12 = findViewById(R.id.linearLayout12);
        back = findViewById(R.id.back4);
        back.setOnClickListener(v -> onBackPressed());
        findViewById(R.id.faq_help1).setOnClickListener(v -> {
            Intent helpIntent = new Intent(getApplicationContext(), FAQ.class);
            startActivity(helpIntent);
        });
        l12.setOnClickListener(v -> {
            Intent in = new Intent(CheckBalance.this, WalletBalance.class);
            in.putExtra("Function", "Balance");
            startActivity(in);
        });
        user = FirebaseAuth.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();user = auth.getCurrentUser();
        assert user != null;
        uid = user.getUid();
        RetrieveAccountData();
        viewPager = findViewById(R.id.viewpager1);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(timer != null) {
            timer.cancel();
        }
    }
    // For View Pager automatic slider
    private void automateViewPagerSwiping() {
        final long DELAY_MS = 1500;
        final long PERIOD_MS = 4500;
        final Handler handler = new Handler();
        final Runnable update = () -> {
            if (viewPager.getCurrentItem() == slideshowAdapter.getItemCount() - 1) {
                viewPager.setCurrentItem(0);
            } else {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            }
        };
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(update);
            }
        }, DELAY_MS, PERIOD_MS);
    }
    public void RetrieveAccountData() {
        FirebaseDatabase.getInstance().getReference().child("Accounts").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    getAccountList.add(postSnapshot.getValue(GetAccount.class));
                }
                if(getAccountList.isEmpty()){t1.setVisibility(View.VISIBLE);}
                UPIAdapter UPIAdapter = new UPIAdapter(getAccountList);
                UPIRecycler.setAdapter(UPIAdapter);
                UPIRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL,false));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Profile", "Failed to read value.", error.toException());
            }
        });
    }
}