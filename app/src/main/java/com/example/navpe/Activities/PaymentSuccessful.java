package com.example.navpe.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.navpe.MainActivity;
import com.example.navpe.Models.GetHistory;
import com.example.navpe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PaymentSuccessful extends AppCompatActivity {

    String amount, receiverName, time,message, imageUrl = "";
    String uid , upi, receiverId, senderName,receiverPhone;
    int txnNo, receiverTxnNo;
    TextView msg;
    FirebaseUser user;
    FirebaseAuth auth;

    List<String> userData = new ArrayList<>();
    DatabaseReference databaseReference, transactionReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_payment_successful);
        user = FirebaseAuth.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();user = auth.getCurrentUser();
        assert user != null;
        uid = user.getUid();
        databaseReference= FirebaseDatabase.getInstance().getReference().child("History");
        databaseReference.keepSynced(true);
        msg = findViewById(R.id.textView3);

        Intent in = getIntent();
        amount = in.getStringExtra("Amount");
        receiverName = in.getStringExtra("ReceiverName");
        time = in.getStringExtra("Time");
        upi = in.getStringExtra("UPI");
        receiverPhone = upi.contains("navpe") ? upi.substring(0,10) : "";
        message = msg.getText().toString();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    userData.add(Objects.requireNonNull(postSnapshot.getValue()).toString());
                }
                if(userData.size() == 6){
                    imageUrl = receiverPhone.equals(Objects.requireNonNull(user.getPhoneNumber())) ? userData.get(3) : "https://png.pngtree.com/png-vector/20190215/ourmid/pngtree-vector-valid-user-icon-png-image_516298.jpg";
//                    Log.e("", "Image retrieved" + userData.get(3));
                    String name;
                    if(!userData.get(4).equals("")){
                        name = userData.get(4);
                    }else{
                        name = user.getPhoneNumber();
                    }
                    senderName = name;
                    getReceiverId();
                    transactAmount(receiverName, uid, "Paid to", "Debited from "+
                            Objects.requireNonNull(user.getPhoneNumber()).substring(3) + "@navpe", user.getPhoneNumber());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("", "Failed to read value.", error.toException());
            }
        });

        message = message.replace("$$", amount).replace("%%",receiverName).replace("##", time);
        msg.setText(message);
        int TIMEOUT = 6000;
        new Handler().postDelayed(() -> {
            Intent i = new Intent(PaymentSuccessful.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }, TIMEOUT);
    }
    private void transactAmount(String senderName, String uid1, String typeOfTxn, String transaction, String mobile) {
        transactionReference = FirebaseDatabase.getInstance().getReference().child("Transaction").child(uid1).child("counter");
        transactionReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int counter = dataSnapshot.getValue() != null ? Integer.parseInt(dataSnapshot.getValue().toString()) : 1000;
                if(uid1.equals(uid)){
                    txnNo = counter;
                }else {
                    receiverTxnNo = counter;
                }
                StoreDatabase(uid1,typeOfTxn,senderName,transaction,mobile,txnNo);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("", "Failed to read value.", error.toException());
            }
        });
    }
    public void getReceiverId(){
        FirebaseDatabase.getInstance().getReference().child("Registered").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if(upi.contains(Objects.requireNonNull(postSnapshot.getValue()).toString())){
                        receiverId = postSnapshot.getKey();
                        Log.e("Tag",receiverId);
                        transactAmount(senderName, receiverId, "Received From", "Credited to " + upi, user.getPhoneNumber());
                    }else{
                        receiverId = "";
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("", "Failed to read value.", error.toException());
            }
        });
    }
    public void StoreDatabase(String uid, String typeOfTxn, String Name, String transaction, String mobile, int txnNo) {
        GetHistory getHistory = new GetHistory(amount, imageUrl, Name, receiverPhone, time, transaction, typeOfTxn);
        databaseReference.child(uid).child("Txn-" + txnNo).child(mobile).setValue(getHistory);
        int x = txnNo + 1;
        FirebaseDatabase.getInstance().getReference().child("Transaction").child(uid).child("counter").setValue(x);
        Toast.makeText(this, "Details Updated", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onBackPressed() {}
}