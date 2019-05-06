package com.example.musicsheet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void openNew(View view) {
        Button newBtn = (Button) findViewById(R.id.NewButton);
        bounce(newBtn);
        Intent intent = new Intent(this, MusicSheet.class);
        startActivity(intent);
    }

    public void openExisting(View view) {
        Button newBtn = (Button) findViewById(R.id.LoadButton);
        bounce(newBtn);
        Intent intent = new Intent(this, FileList.class);
        startActivity(intent);
    }

    public void bounce(Button button){
        //Handler h = new Handler();

        final Animation bounce = new TranslateAnimation(0, 0, -10, 10);
        bounce.setDuration(50);
        bounce.setFillAfter(true);
        bounce.setRepeatMode(Animation.REVERSE);
        bounce.setRepeatCount(1);
        button.startAnimation(bounce);

        /*h.postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        }, 150);*/
    }
}
