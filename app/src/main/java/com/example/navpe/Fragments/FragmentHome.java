package com.example.navpe.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.example.navpe.Activities.CheckBalance;
import com.example.navpe.Activities.MapLocation;
import com.example.navpe.Activities.MobilePay;
import com.example.navpe.Activities.QRCode;
import com.example.navpe.Adapters.SlideshowAdapter;
import com.example.navpe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class FragmentHome extends Fragment {

    ViewPager2 viewPager;
    Timer timer;
    View view;
    LinearLayout l1,l2,l3, checkBal;
    SlideshowAdapter slideshowAdapter;
    List<String> userData;
    FirebaseUser user;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    String uid, upi;
    TextView upiView,nameView;

    public FragmentHome() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        databaseReference.keepSynced(true);
        uid = user.getUid();
        String z = "/topics/notification_Chat" +  uid.substring(0,4);
        FirebaseMessaging.getInstance().subscribeToTopic(z);
        userData = new ArrayList<>();
        RetrieveData();
        view = inflater.inflate(R.layout.fragment_home, container, false);
        l1 = view.findViewById(R.id.linearLayout4);
        l2 = view.findViewById(R.id.qr_layout);
        l3 = view.findViewById(R.id.address_layout);
        checkBal = view.findViewById(R.id.linearLayout11);
        l1.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_global_profileFragment));
        l2.setOnClickListener(v -> {
            Intent mapIntent = new Intent(view.getContext(), QRCode.class);
            startActivity(mapIntent);
        });
        l3.setOnClickListener(v -> {
            Intent mapIntent = new Intent(view.getContext(), MapLocation.class);
//            mapIntent.putExtra("Location",location.getText().toString());
            startActivity(mapIntent);
        });
        checkBal.setOnClickListener(v -> {
            Intent inw = new Intent(view.getContext(), CheckBalance.class);
            startActivity(inw);
        });

        upiView = view.findViewById(R.id.textView11);
        nameView = view.findViewById(R.id.nameView);
        new Handler().postDelayed(() -> upiView.setText(String.format("My NAVPE Id: %s", upi)), 200);

        //View Pager
        viewPager = view.findViewById(R.id.viewpager);
        slideshowAdapter = new SlideshowAdapter(view.getContext());
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
        view.findViewById(R.id.linearLayout8).setOnClickListener(v -> {
            Intent in = new Intent(view.getContext(), MobilePay.class);
            startActivity(in);
        });
        return view;
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

    public void RetrieveData() {
        databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    userData.add(Objects.requireNonNull(postSnapshot.getValue()).toString());
                }
                if(userData.isEmpty()){
                    l2.setClickable(false);
                    Toast.makeText(getContext(), "Enter your details for the payment features..", Toast.LENGTH_LONG).show();
                }
                if (userData.size() == 6) {
                    if(!userData.get(4).equals("")){
                        nameView.setText(userData.get(4));
                    }else if(!Objects.equals(user.getDisplayName(), "")){
                        nameView.setText(user.getDisplayName());
                    }else{
                        String value = "No name";
                        nameView.setText(value);
                    }
                    // For Email or Phone Number
                    if(!Objects.equals(user.getPhoneNumber(), "")){
                        upi = Objects.requireNonNull(user.getPhoneNumber()).substring(3) + "@navpe";
//                        phNo.setText(user.getPhoneNumber());
                    } else if (!userData.get(5).equals("")){
                        upi = userData.get(5).substring(3) + "@navpe";
//                        phNo.setText(userData.get(5));
                    } else {
                        upi=user.getPhoneNumber()+"@navpe";
//                        phNo.setText(user.getPhoneNumber());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Profile", "Failed to read value.", error.toException());
            }
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(timer != null) {
            timer.cancel();

        }
    }
}