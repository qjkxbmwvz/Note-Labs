package com.example.musicsheet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

public class MusicSheet extends AppCompatActivity {

    final int horizontal = 4;
    final int vertical = 9;

    int[][] staffPositions = new int[horizontal][vertical];

    ScrollView scrollView;
    TableLayout table;
    ImageView imageView;
    TextView textView;
    TableRow row;


    int horizontalStart = 60;
    int horizontalMax = 720;
    int horizontalOffset = (horizontalMax-horizontalStart)/staffPositions.length-1; //hard-coded: splits bar into 4

    int verticalStart = 130;
    int verticalOffset = 25; //hard-coded: eye-balled the distance between each bar lol
    int verticalMax = 310;

    private Player player = new Player();
    Score score;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_sheet);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        scrollView = findViewById(R.id.musicSheetScroll);
        textView = findViewById(R.id.textView);
        table = findViewById(R.id.staffs);

        /*if(horizontalMax <= (horizontalStart + horizontalOffset*staffPositions.length)){
            textView.setText("Good Vertical");
        }
        else{
            textView.setText("bad");
            horizontalMax = horizontalStart + horizontalOffset*staffPositions.length;
        }*/

        /*if(verticalMax <= (verticalStart + verticalOffset*staffPositions[0].length))
            textView.setText("Good Vertical");*/

        for(int i = 0; i < table.getChildCount(); i++){
            //for each row
            row = (TableRow) table.getChildAt(i);
            for(int j = 0; j < row.getChildCount(); j++){
                //each imageView in row
                imageView = (ImageView) row.getChildAt(j);

                //create onTouchListener for each ImageView
                imageView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int imageX = (int)event.getX();
                        int imageY = (int)event.getY();

                        imageX = Snap(imageX, horizontalMax, horizontalStart, horizontalOffset, staffPositions.length);
                        imageY = Snap(imageY, verticalMax, verticalStart, verticalOffset, staffPositions[0].length);
                        if(imageX < 0)
                            textView.setText("Touch: " + (int)event.getX());
                        else
                            textView.setText("imageX: " + imageX + " imageY: " + imageY);

                        //textView.setText("sX:"+ scrollX + "sY:" + scrollY + " iX:" + imageX + "iY:" + imageY + " lX:" + localX + "lY:" + localY);
                        return false;
                    }
                });
            }
        }

        /*btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ProjectsPage.class));
            }
        });*/

        /*TableLayout tblLayout = (TableLayout)findViewById(R.id.tableLayout);
        TableRow row = (TableRow)tblLayout.getChildAt(0); // Here get row id depending on number of row
        Button button = (Button)row.getChildAt(XXX); // get child index on particular row
        String buttonText = button.getText().toString();*/


        //run this code for each view you click on




        /*goToInstrumentPanel();
        goToNotePanel();*/
        /*if (score == null) {
            ActivityCompat.requestPermissions(MusicSheet.this, new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            }, 100);
            if (score == null) {
                score = new Score(player);
                if (ContextCompat.checkSelfPermission(MusicSheet.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)
                    score.load();
            }
            score.play();
        }*/
    }

    public void AddNote(View view){

    }

    //synchronize a click on a staff to the score.tracks.addNote(time, note)

    @Override
    protected void onResume() {
        super.onResume();
        player.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.pause();
    }


    //returns nearest point relative to where the user touched
    private int Snap(int touchPoint, int max, int start, int offset, int points){
        int prevPoint;
        int nextPoint = 0;
        //if not in range, snap to nearest edge
        if(touchPoint <= start){
            return start;
        }
        else if(touchPoint >= max)
            return max;
        else{
            for(int i = 0; i < points-1; i++){ //horizontal positions, 4
                prevPoint = start + (i * offset);
                nextPoint = prevPoint + offset;

                //find specific range
                if(touchPoint >= prevPoint && touchPoint <= nextPoint){
                    //compare distances
                    int leftDistance = touchPoint - prevPoint;
                    int rightDistance = nextPoint - touchPoint;

                    if(leftDistance < rightDistance)
                        return prevPoint;
                    else
                        return nextPoint;
                }//end range check
            }//end for
            return nextPoint;
        }
    }

    private void goToInstrumentPanel(){
        ImageButton btnInstr = (ImageButton)findViewById(R.id.addInstrumentButton);
        btnInstr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MusicSheet.this, InstrumentPanel.class));
            }
        });
    }

    private void goToNotePanel(){
        ImageButton btnNote = (ImageButton)findViewById(R.id.noteButton);
        btnNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MusicSheet.this, NotePanel.class));
            }
        });
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

         switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }

                    scrollView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                //set a listener for the imageViews
                v.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        imageName = v.toString();
                        return false;
                    }
                });

                //set a onTouchListener for each imageView
                textView.setText("x:" + event.getX() + "v:" + imageName);

                return false;
            }
        });
    }*/

}
