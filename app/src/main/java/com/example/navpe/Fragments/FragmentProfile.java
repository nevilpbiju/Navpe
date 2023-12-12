package com.example.navpe.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.navpe.Activities.AccountActivity;
import com.example.navpe.Activities.AddFaceActivity;
import com.example.navpe.Activities.EditProfile;
import com.example.navpe.Activities.SetPIN;
import com.example.navpe.Activities.FAQ;
import com.example.navpe.Activities.Login;
import com.example.navpe.Activities.ShowQR;
import com.example.navpe.Adapters.AccountAdapter;
import com.example.navpe.Models.GetAccount;
import com.example.navpe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FragmentProfile extends Fragment implements
        CompoundButton.OnCheckedChangeListener{
    TextView changePass, name, phNo,logout, qrCodeOption, enterUPI;
    ImageView backProfile, help,image, addFaceOption;
    CardView addBankAccount;
    View view;
    RecyclerView accountRecycler;
    ArrayList<GetAccount> getAccountList = new ArrayList<>();
    Button edit;
    List<String> userData;
    FirebaseUser user;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    String uid;

    public FragmentProfile() {
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       view = inflater.inflate(R.layout.fragment_profile, container, false);
                //Change Password
//        getAccountList.add(new GetAccount("State Bank of India","https://i.pinimg.com/originals/48/69/d9/4869d9a4c0d3b680ac66274dd0a88501.png","632-747xxx-xxx"));
        accountRecycler = view.findViewById(R.id.cardDetails);
        backProfile = view.findViewById(R.id.back3);
        help = view.findViewById(R.id.faq_help);
        changePass = view.findViewById(R.id.changePass);
        name = view.findViewById(R.id.name);
        phNo = view.findViewById(R.id.mobileNumber);
        image = view.findViewById(R.id.profileImage2);
        enterUPI = view.findViewById(R.id.enterUpi);

        changePass.setOnClickListener(v -> new FragmentChangePassword().Dialog(v));
        help.setOnClickListener(v -> {
            Intent helpIntent = new Intent(view.getContext(), FAQ.class);
            startActivity(helpIntent);
        });
        user = FirebaseAuth.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        databaseReference.keepSynced(true);
        uid = user.getUid();
        userData = new ArrayList<>();
        RetrieveAccountData();
        RetrieveUserData();
        logout = view.findViewById(R.id.logout);
        logout.setOnClickListener(v -> Logout());

        addBankAccount = view.findViewById(R.id.cardView2);
        addBankAccount.setOnClickListener(v -> startActivity(new Intent(view.getContext(), AccountActivity.class)));

        enterUPI.setOnClickListener(v -> startActivity(new Intent(view.getContext(), SetPIN.class)));

        addFaceOption = view.findViewById(R.id.imageView5);
        addFaceOption.setOnClickListener(v -> startActivity(new Intent(view.getContext(), AddFaceActivity.class)));

        qrCodeOption = view.findViewById(R.id.qrcodeOption);
        qrCodeOption.setOnClickListener(v -> startActivity(new Intent(view.getContext(), ShowQR.class)));
//        Edit the Current user Profile
        edit = view.findViewById(R.id.editProfile);
        edit.setOnClickListener(v -> v.getContext().startActivity(new Intent(view.getContext(), EditProfile.class)));
        backProfile.setOnClickListener(v -> requireActivity().onBackPressed());
        return view;
    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.facePay) {
            Log.e("Switch","True face");
        } else {
            Log.e("Switch","Unexpected value: " + id);
        }
    }
    public void Logout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Logout");
        builder.setMessage("Do you want to logout?");
        builder.setPositiveButton("YES", (dialog, which) -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(view.getContext(), "Logged Out", Toast.LENGTH_LONG).show();
            startActivity(new Intent(view.getContext(), Login.class));
            requireActivity().finish();
            dialog.dismiss();
        });
        builder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());

        AlertDialog alert = builder.create();
        alert.show();
    }
    public void RetrieveUserData() {
        databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                userData.add(Objects.requireNonNull(postSnapshot.getValue()).toString());
            }
            if(userData.isEmpty()){
                addFaceOption.setClickable(false);
                qrCodeOption.setClickable(false);
                addBankAccount.setClickable(false);
                Toast.makeText(getContext(), "Enter your details for the payment features..", Toast.LENGTH_SHORT).show();
                enterUPI.setVisibility(View.VISIBLE);
            }
            if (userData.size() == 6) {
            //For Name
                if(!userData.get(4).equals("")){
                    name.setText(userData.get(4));
                }else if(!Objects.equals(user.getDisplayName(), "")){
                    name.setText(user.getDisplayName());
                }else{
                    String value = "No name";
                    name.setText(value);
                }
                // For Email or Phone Number
                if(!Objects.equals(user.getPhoneNumber(), "")){
                    phNo.setText(user.getPhoneNumber());
                } else if (!userData.get(5).equals("")){
                    phNo.setText(userData.get(5));
                } else phNo.setText(user.getPhoneNumber());
                //For Image
                if (!userData.get(3).equals("")) {
                        Picasso.get().load(userData.get(3)).into(image);
                } else if (user.getPhotoUrl()!= null && !Objects.requireNonNull(user.getPhotoUrl()).toString().equals("")) {
                    Picasso.get().load(user.getPhotoUrl().toString()).into(image);
                } else {
                    Picasso.get().load(R.drawable.logo).into(image);
                }
            }
        }
        @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Profile", "Failed to read value.", error.toException());
            }
        });
    }
    public void RetrieveAccountData() {
        FirebaseDatabase.getInstance().getReference().child("Accounts").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    getAccountList.add(postSnapshot.getValue(GetAccount.class));
                }
                if(getAccountList.isEmpty())
                    accountRecycler.setVisibility(View.GONE);

                AccountAdapter accountAdapter = new AccountAdapter(getAccountList);
                accountRecycler.setAdapter(accountAdapter);
                accountRecycler.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL,false));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Profile", "Failed to read value.", error.toException());
            }
        });
    }
}