package com.example.navpe.Activities;

import static android.view.WindowManager.*;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.navpe.MainActivity;
import com.example.navpe.R;
import com.google.firebase.auth.FirebaseAuth;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    Animation topAnimation,bottomAnimation;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN,
                LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        topAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.top_animation);
        bottomAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_animation);

        imageView = findViewById(R.id.imageView1);

        imageView.setAnimation(topAnimation);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        int SPLASH_SCREEN = 1500;
        new Handler().postDelayed(() -> {
            if (auth.getCurrentUser() != null) {
                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }else {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
            finish();
        }, SPLASH_SCREEN);
    }
}