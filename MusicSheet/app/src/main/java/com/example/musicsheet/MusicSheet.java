package com.example.musicsheet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

public class MusicSheet extends AppCompatActivity {

    ImageView imageView;
    TextView textView;
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_sheet);

      /*  imageView = findViewById(R.id.staffBar0);

        imageView.setClickable(true);*/

        textView = findViewById(R.id.textView);
        /*scrollView = findViewById(R.id.musicSheetScroll);

        //imageView.getDrawable().getBounds();
        //x and y calculated relative to imageView bound

        scrollView.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                int x = (int)event.getX();
                int y = (int)event.getY();
                switch(event.getAction()){
                    default:
                }
                textView.setText("X: " + x + " Y: " + y);
                return true;
            }});*/
    }

    public void AddNote(View view){
        int x = (int)view.getX();
        int y = (int)view.getY();

        textView.setText("X: " + x + " Y: " + y);
    }


    //BUTTONS---

    public void Play(View view){
        //begin playback
    }

    public void Pause(View view){
        //Pause playback
        //store current position
    }

    public void Restart(View view){
        //set playerMarker position to 0
        //syncronize with note data
    }

    //this method requires that the last note be stored
    public void Undo(View view){
        //remove last note added
    }

    //END BUTTONS---



/*    //@Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                textView.setText("X: " + x + " Y: " + y);
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
        }
        return false;
    }*/

}
