package com.example.musicsheet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openNew(View view){
        Intent intent = new Intent(this, MusicSheet.class);
        startActivity(intent);
    }

    public void openExisting(View view){
        Intent intent = new Intent(this, FileList.class);
        startActivity(intent);
    }
}
