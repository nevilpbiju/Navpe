package com.example.navpe.Activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.navpe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShowQR extends AppCompatActivity {

    ImageView imageView;
    TextView nameView;
    List<String> userData;
    FirebaseUser user;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    String uid;
    private String phone;
    private String name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qr);
        imageView = findViewById(R.id.imageView2);
        nameView = findViewById(R.id.textViewName);

        user = FirebaseAuth.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        databaseReference.keepSynced(true);
        uid = user.getUid();
        userData = new ArrayList<>();
        RetrieveData();
        new Handler().postDelayed(() -> {
            String upi = "upi://pay?pa" + phone.substring(2) + "@navpe&pn=" + name + "&nav=1234";
            FirebaseDatabase.getInstance().getReference().child("QrCode").child(uid).setValue(upi);
//        upi="upi://pay?pa=nevilpbiju@ybl&pn=Nevil%20P%20Biju&mc=0000&mode=02&purpose=00";
            MultiFormatWriter writer = new MultiFormatWriter();
            try{
                BitMatrix matrix = writer.encode(upi, BarcodeFormat.QR_CODE,600,600);
                BarcodeEncoder encoder = new BarcodeEncoder();
                Bitmap bitmap = encoder.createBitmap(matrix);
                imageView.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }, 100);
    }

    public void RetrieveData() {
        databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    userData.add(Objects.requireNonNull(postSnapshot.getValue()).toString());
                }
                if (userData.size() == 6) {
                    //For Name
                    if(!userData.get(4).equals("")){
                        nameView.setText(userData.get(4));
                        name=userData.get(4);
                    }else if(!Objects.equals(user.getDisplayName(), "")){
                        name=user.getDisplayName();
                        nameView.setText(user.getDisplayName());
                    }else{
                        String value = "No name";
                        nameView.setText(value);
                        name="No name";
                    }
                    // For Email or Phone Number
                    if(!Objects.equals(user.getPhoneNumber(), "")){
                        phone=user.getPhoneNumber();
                    } else if (!userData.get(5).equals("")){
                        phone=user.getPhoneNumber();
                    } else
                        phone=user.getPhoneNumber();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Profile", "Failed to read value.", error.toException());
            }
        });
    }
}