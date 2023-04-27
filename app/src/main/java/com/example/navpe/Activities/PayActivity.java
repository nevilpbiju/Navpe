package com.example.navpe.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.navpe.R;
import com.example.navpe.Recognitions.DetectorActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.concurrent.Executor;

public class PayActivity extends AppCompatActivity {

    String upi, name,faceUrl;
    TextView name1, upi1;
    EditText amount;
    ImageView back;
    Intent intent;

    TextInputEditText message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        name1 = findViewById(R.id.user2);
        upi1 = findViewById(R.id.upi0);
        amount = findViewById(R.id.amount2);
        message = findViewById(R.id.message1);
        back = findViewById(R.id.imageView2);
        LoadImage();
        Intent in = getIntent();
        String data = in.getStringExtra("Payment");
        if(data.length() > 10){
            //upi://pay?pa=gokuvarun21@okaxis&pn=Varun%20Wadhwa&aid=uGICAgIC1t8HgMg
            upi = data.substring(13, data.split("&")[0].length());
            name =  data.split("&")[1];
            name = name.substring(3).replace("%20", " ");

        }else if(data.length() == 10){
            upi = data + "@navpe";
            name = data;
        }
        upi1.setText(upi);
        name1.setText(name);
        back.setOnClickListener(v -> onBackPressed());
        // creating a variable for our Executor
        Executor executor = ContextCompat.getMainExecutor(this);
        // this will give us result of AUTHENTICATION
        final BiometricPrompt biometricPrompt = new BiometricPrompt(PayActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });
        final BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle("NAVPE")
                .setDescription("Use your biometric to login ").setNegativeButtonText("Cancel").build();

        String mess = Objects.requireNonNull(message.getText()).toString();
        Button button = findViewById(R.id.confirm1);
        button.setOnClickListener(v -> {
            String amont = amount.getText().toString();
            if(amount.length() != 0 && Integer.parseInt(amont) > 0){
                intent = new Intent(PayActivity.this, DetectorActivity.class);
                intent.putExtra("Amount", amont);
                intent.putExtra("message", mess);
                intent.putExtra("UPI", upi);
                intent.putExtra("ReceiverName", name);
                intent.putExtra("FaceData", faceUrl);
                if(faceUrl.length() > 0){
                    biometricPrompt.authenticate(promptInfo);
                }else{
                    Toast.makeText(this, "Please create a face Image from the Profile menu", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void LoadImage(){
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseDatabase.getInstance().getReference().child("FaceData").child(uid).child("faceImage").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                faceUrl = Objects.requireNonNull(snapshot.getValue()).toString();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("", "Error in retrieving the image data " + error);
            }
        });
    }
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
}