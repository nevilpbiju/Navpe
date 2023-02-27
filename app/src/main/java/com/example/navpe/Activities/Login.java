package com.example.navpe.Activities;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.navpe.MainActivity;
import com.example.navpe.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity {

    EditText phoneNo,otp;
    MaterialButton login,generateOtp;
    FirebaseAuth auth;
    private String verificationId;
    CountryCodePicker ccp;
    String selected_country_code;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();
        phoneNo = findViewById(R.id.phone1);
        otp = findViewById(R.id.otp);
        login = findViewById(R.id.phLoginBtn);
        generateOtp = findViewById(R.id.generateOtp);
        ccp = findViewById(R.id.ccp);
        ccp.setOnCountryChangeListener(() -> selected_country_code = ccp.getSelectedCountryCodeWithPlus());
        generateOtp.setOnClickListener(v -> {
            if (TextUtils.isEmpty(phoneNo.getText().toString())) {
                Toast.makeText(Login.this, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show();
            } else {

                if(selected_country_code == null){
                    selected_country_code = "+91";
                }
                String phone = selected_country_code + phoneNo.getText().toString();
                sendVerificationCode(phone);
                Toast.makeText(Login.this, "Getting OTP...", Toast.LENGTH_SHORT).show();
            }
        });
        login.setOnClickListener(v -> {
            if (TextUtils.isEmpty(otp.getText().toString())) {
                Toast.makeText(Login.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            } else {
                verifyCode(otp.getText().toString());
            }
        });
    }
    private void signInWithCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Toast.makeText(Login.this, "Signing you in...", Toast.LENGTH_SHORT).show();
                String name = Objects.requireNonNull(user).getPhoneNumber();
                assert name != null;
                FirebaseDatabase.getInstance().getReference().child("Registered").child(user.getUid()).setValue(name.substring(3));
                Toast.makeText(getApplicationContext(), "Welcome " + name, Toast.LENGTH_SHORT).show();
                Intent i = new Intent(Login.this, MainActivity.class);
                startActivity(i);
                finish();
            } else {
                Toast.makeText(Login.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void sendVerificationCode(String number) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth).setPhoneNumber(number).setTimeout(
                60L, TimeUnit.SECONDS).setActivity(this).setCallbacks(mCallBack).build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            final String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                otp.setText(code);
                verifyCode(code);
            }
        }
        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };
    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }
}