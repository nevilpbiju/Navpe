package com.example.navpe.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.navpe.Activities.MapLocation;
import com.example.navpe.Activities.QRCode;
import com.example.navpe.Adapters.HistoryAdapter;
import com.example.navpe.Models.GetHistory;
import com.example.navpe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class FragmentHistory extends Fragment {

    RecyclerView recyclerView;
    ArrayList<GetHistory> getHistories = new ArrayList<>();
    View view;
    TextView nameView;
    ImageView imgNothing;
    List<String> historyData;
    String days = "",time, imageUrl = "",uid;
    FirebaseUser user;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    LinearLayout l1, l2, l3, l4;
    public FragmentHistory(){
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_history, container, false);
//        getHistories = functionList();
        recyclerView = view.findViewById(R.id.historyRecycler);
        l1 = view.findViewById(R.id.user_layout);
        l2 = view.findViewById(R.id.qr_layout1);
        l3 = view.findViewById(R.id.address_layout1);
        l4 = view.findViewById(R.id.history_gone);
        imgNothing = view.findViewById(R.id.image_nothing);
        historyData = new ArrayList<>();
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
        nameView = view.findViewById(R.id.nameView2);
        user = FirebaseAuth.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();user = auth.getCurrentUser();
        assert user != null;
        uid = user.getUid();
        if(user.getPhoneNumber() != null)
            nameView.setText(user.getPhoneNumber());
        if(user.getPhotoUrl()!= null) {
            imageUrl = user.getPhotoUrl().toString();
        }
        databaseReference= FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);

        RetrieveData();
        return view;
    }
    public void RetrieveData() {
        databaseReference.child("History").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot snapshot : postSnapshot.getChildren()) {
                        historyData.clear();
                        String t1 = snapshot.getKey();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            historyData.add(Objects.requireNonNull(snapshot1.getValue()).toString());
    //                        Log.w("E", "Value retrieved" + snapshot.getValue().toString());
                        }
                        time = historyData.get(4);
                        SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
                        try {
                            Date date = sdf1.parse(time);
                            String currentDateAndTime = sdf1.format(new Date());
                            Date now = sdf1.parse(currentDateAndTime);
                            long diff = Math.abs(Objects.requireNonNull(now).getTime() - Objects.requireNonNull(date).getTime());
                            days = String.valueOf(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
                            if (days.equals("0")) {
                                days = String.valueOf(TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS));
                                if (days.equals("0")) {
                                    days = String.valueOf(TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS));
                                    days += " min ago";
                                } else {
                                    days += " hours ago";
                                }
                            } else {
                                days += " days ago";
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        String mobile;
                        mobile= historyData.get(3);
                        if (t1 != null && !Objects.requireNonNull(user.getPhoneNumber()).contains(t1)){
                            mobile = Objects.requireNonNull(t1).substring(3);
                            Log.e("Mobile",mobile);
                        }
//                            Log.e("List", historyData.toString());
                        String imageValue = historyData.get(1);
                        if(imageValue.length() == 0)
                            imageValue = "https://png.pngtree.com/png-vector/20190215/ourmid/pngtree-vector-valid-user-icon-png-image_516298.jpg";
                        getHistories.add(new GetHistory(historyData.get(0), imageValue, historyData.get(2), mobile, days , historyData.get(6), historyData.get(5)));
                    }
                }
                if(getHistories.size() == 0){
                    l4.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    String ull = "https://firebasestorage.googleapis.com/v0/b/favfood-99554.appspot.com/o/no%20history%20found.png?alt=media&token=a2272056-2fc2-4101-b29b-a9f685d9b969";
                    Picasso.get().load(ull).into(imgNothing);
                }
                HistoryAdapter historyAdapter = new HistoryAdapter(getHistories);
                recyclerView.setAdapter(historyAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("", "Failed to read value.", error.toException());
            }
        });
    }
}