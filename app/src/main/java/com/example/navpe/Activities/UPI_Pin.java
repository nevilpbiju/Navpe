package com.example.navpe.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.navpe.R;

public class UPI_Pin extends AppCompatActivity {

    protected EditText pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upipin);
        Intent inw = getIntent();
        String func = inw.getStringExtra("Function");
        if(func.equals("Balance")){
            findViewById(R.id.layout2).setVisibility(View.GONE);
        }
        pass = findViewById(R.id.pinUPI);
        pass.addTextChangedListener(new TextWatcher() {
            @Override   public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(s.length() == 4){
                        if(s.toString().equals("5471")) {
                            Intent in = new Intent(UPI_Pin.this, ShowBalance.class);
                            startActivity(in);
                            finish();
                        }else{
                            Toast.makeText(UPI_Pin.this, "Wrong pin retry", Toast.LENGTH_SHORT).show();
                            pass.setText("");
                        }
                    }
            }
            @Override   public void afterTextChanged(Editable s) {}
        });
    }
}