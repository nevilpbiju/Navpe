package com.example.navpe.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.navpe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class UPI_Pin extends AppCompatActivity {

    protected EditText pass;
    protected TextView balance;
    private  String pinValue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upipin);
        balance = findViewById(R.id.amount3);
        Intent inw = getIntent();
        String func = inw.getStringExtra("Function");
        pass = findViewById(R.id.pinUPI);

        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Pin").child(uid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {pinValue = decryption(String.valueOf(snapshot.getValue()));}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
        if(func.equals("Balance")){
            findViewById(R.id.layout2).setVisibility(View.GONE);
        }
        if(func.equals("Pay")) {
            balance.setText(inw.getStringExtra("Amount"));
            pass.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 4) {
                        if (s.toString().equals(pinValue)) {
                            Intent in = new Intent(UPI_Pin.this, PaymentSuccessful.class);
                            in.putExtra("Amount", inw.getStringExtra("Amount"));
                            in.putExtra("Time", inw.getStringExtra("Time"));
                            in.putExtra("UPI", inw.getStringExtra("UPI"));
                            in.putExtra("ReceiverName", inw.getStringExtra("ReceiverName"));
                            startActivity(in);
                            finish();
                        } else {
                            Toast.makeText(UPI_Pin.this, "Wrong pin retry", Toast.LENGTH_SHORT).show();
                            pass.setText("");
                        }
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
        else{
            pass.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 4) {
                        if (s.toString().equals(pinValue)) {
                            Intent in = new Intent(UPI_Pin.this, ShowBalance.class);
                            startActivity(in);
                            finish();
                        } else {
                            Toast.makeText(UPI_Pin.this, "Wrong pin retry", Toast.LENGTH_SHORT).show();
                            pass.setText("");
                        }
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }
    public String decryption(String strEncryptedText){
        String seedValue = "dRgUkXn2r5u8x/A?D(G+KbPeShVmYq3t2";
    String strDecryptedText="";
    try {
        strDecryptedText = AESHelper.decrypt(strEncryptedText, seedValue);
    } catch (Exception e) {
        e.printStackTrace();
    }
    return strDecryptedText;
}
}