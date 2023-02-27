package com.example.navpe.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.navpe.MainActivity;
import com.example.navpe.R;

public class ShowBalance extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_balance);
        findViewById(R.id.confirm3).setOnClickListener(v -> {
            Intent in = new Intent(getApplicationContext(), CheckBalance.class);
            in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(in);
            finish();
        });
    }
}