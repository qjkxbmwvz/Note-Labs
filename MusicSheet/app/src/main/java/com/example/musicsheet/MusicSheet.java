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

import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;

public class MusicSheet extends AppCompatActivity {
    HashMap<Integer, Byte> posToPitch;
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

    int verticalStart = 82;
    int verticalOffset = 21; //hard-coded: eye-balled the distance between each bar lol
    int verticalMax = 339;

    int lastTouchPointX = 0, lastTouchPointY = 0;

    private Player player = new Player();
    Score score;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        posToPitch = new HashMap<>();
        posToPitch.put( 82, (byte)77);
        posToPitch.put(103, (byte)76);
        posToPitch.put(124, (byte)74);
        posToPitch.put(145, (byte)72);
        posToPitch.put(166, (byte)71);
        posToPitch.put(187, (byte)69);
        posToPitch.put(208, (byte)67);
        posToPitch.put(229, (byte)65);
        posToPitch.put(250, (byte)64);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_sheet);

        scrollView = findViewById(R.id.musicSheetScroll);
        textView = findViewById(R.id.textView);
        table = findViewById(R.id.staffs);

        score = new Score(player);

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

                        imageX = Snap(imageX, horizontalMax, horizontalStart,
                                      horizontalOffset, staffPositions.length);
                        imageY = Snap(imageY, verticalMax,   verticalStart,
                                      verticalOffset,   staffPositions[0].length);

                        textView.setText("imageX: " + imageX + " imageY: " + imageY);

                        /*if(lastTouchPointX != imageX || lastTouchPointY != imageY)
                            textView.setText("imageX: " + imageX + " imageY: " + imageY);

                        lastTouchPointX = imageX;*/

                        switch(event.getAction()){
                        case MotionEvent.ACTION_DOWN: {
                            byte[] midiEvent = new byte[3];

                            midiEvent[0] = (byte) (0x90 | 0);
                            midiEvent[1] = posToPitch.get(imageY);
                            midiEvent[2] = 127;

                            // Send the MIDI event to the synthesizer.
                            player.directWrite(midiEvent);
                        }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (lastTouchPointY != imageY) {
                                byte[] midiEvent = new byte[6];

                                midiEvent[0] = (byte) (0x80 | 0);
                                midiEvent[1] = posToPitch.get(lastTouchPointY);
                                midiEvent[2] = 127;
                                midiEvent[3] = (byte) (0x90 | 0);
                                midiEvent[4] = posToPitch.get(imageY);
                                midiEvent[5] = 127;

                                // Send the MIDI event to the synthesizer.
                                player.directWrite(midiEvent);
                            }

                            //textView.setText("imageX: " + imageX + " imageY: " + imageY);
                            break;
                        case MotionEvent.ACTION_UP: {
                            byte[] midiEvent = new byte[3];

                            midiEvent[0] = (byte) (0x80 | 0);
                            midiEvent[1] = posToPitch.get(imageY);;
                            midiEvent[2] = 127;

                            // Send the MIDI event to the synthesizer.
                            player.directWrite(midiEvent);
                            // TODO: get measure number (0-indexed) and add it multiplied by 192 to timePosition
                            score.addNote(0, 48 * (imageX - 60) / 164,
                                    48, posToPitch.get(imageY), (byte) 127);
                        }
                            break;
                        }
                        lastTouchPointY = imageY;

                        //textView.setText("imageX: " + imageX + " imageY: " + imageY);

                       /* if(imageX < 0)
                            textView.setText("Touch: " + (int)event.getX());
                        else
                            textView.setText("imageX: " + imageX + " imageY: " + imageY + " " + v.toString());*/

                        //textView.setText("sX:"+ scrollX + "sY:" + scrollY + " iX:" + imageX + "iY:" + imageY + " lX:" + localX + "lY:" + localY);
                        return true;
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
        if (touchPoint <= start)
            return start;
        else if(touchPoint >= max)
            return max;
        else {
            for(int i = 0; i < points-1; i++){ //horizontal positions, 4
                prevPoint = start + (i * offset);
                nextPoint = prevPoint + offset;

                //find specific range
                if(touchPoint >= prevPoint && touchPoint <= nextPoint){
                    //compare distances
                    int leftDistance  = touchPoint - prevPoint;
                    int rightDistance = nextPoint  - touchPoint;

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

    public void play(View view)    { score.play(); }

    public void pause(View view)   { score.pause(); }

    public void restart(View view) { score.resetPlayPos(); }

    //this method requires that the last note be stored
    public void undo(View view){
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
