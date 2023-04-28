package com.example.navpe.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.navpe.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SetPIN extends AppCompatActivity {
    private Button button;
    String p;
    private TextInputLayout confirmPin;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setpin);
        button = findViewById(R.id.set_new_password_btn);
        findViewById(R.id.backFromPin).setOnClickListener(v -> onBackPressed());
        TextInputLayout pin = findViewById(R.id.new_pin);
        confirmPin = findViewById(R.id.confirm_pin);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Objects.requireNonNull(pin.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 4) confirmPin.requestFocus();
                else if(s.length() > 4){
                    pin.getEditText().setText("");
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        Objects.requireNonNull(confirmPin.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 1){confirmPin.setError(null);confirmPin.clearFocus();}
                if(s.length() == 4 && s.toString().contentEquals(pin.getEditText().getText())) {
                    confirmPin.setBoxBackgroundColor(Color.GREEN);
                    button.requestFocus();
                }else if(s.length() >= 4){
                    confirmPin.setError("Wrong Pin Re-enter");
                    confirmPin.requestFocus();
                    confirmPin.getEditText().setText("");
                    confirmPin.setBoxBackgroundColor(0);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        button.setOnClickListener(v -> {
            p = pin.getEditText().getText().toString();
            String encryptedKey = encryption(p);
            FirebaseDatabase.getInstance().getReference().child("Pin").child(Objects.requireNonNull(user).getUid()).setValue(encryptedKey);
            Toast.makeText(this, "The UPI pin has been set.", Toast.LENGTH_SHORT).show();
            onBackPressed();
        });
    }
    public String encryption(String strNormalText){
        String seedValue = "dRgUkXn2r5u8x/A?D(G+KbPeShVmYq3t2";
        String normalTextEnc = "";
        try {
            normalTextEnc = AESHelper.encrypt(strNormalText, seedValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return normalTextEnc;
    }
}
