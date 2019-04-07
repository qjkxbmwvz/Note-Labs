package com.example.musicsheet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashMap;

public class MusicSheet extends AppCompatActivity {
    HashMap<Integer, Byte> posToPitch;
    final int horizontal = 4;
    final int vertical = 9;

    //Drawing Variables
    Bitmap bitmap;
    Canvas canvas;
    Paint linePaint;

    int[][] staffPositions = new int[horizontal][vertical];

    ScrollView scrollView;
    TableLayout table;
    ImageView imageView;
    TextView textView;
    TableRow row;


    int horizontalStart = 60;
    int horizontalMax = 720;
    int horizontalOffset = (horizontalMax-horizontalStart)/staffPositions.length-1; //hard-coded: splits bar into 4

    int verticalStart = 19;
    int verticalOffset = 21; //hard-coded: eye-balled the distance between each bar lol
    int verticalMax = 324;

    int lastTouchPointX = 0, lastTouchPointY = 0;

    private Player player = new Player();
    Score score;
    Fraction timeSignature;

    @SuppressLint({"ClickableViewAccessibility", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        posToPitch = new HashMap<>();
        posToPitch.put( 19, (byte)83);
        posToPitch.put( 40, (byte)81);
        posToPitch.put( 61, (byte)79);
        posToPitch.put( 82, (byte)77);
        posToPitch.put(103, (byte)76);
        posToPitch.put(124, (byte)74);
        posToPitch.put(145, (byte)72);
        posToPitch.put(166, (byte)71);
        posToPitch.put(187, (byte)69);
        posToPitch.put(208, (byte)67);
        posToPitch.put(229, (byte)65);
        posToPitch.put(250, (byte)64);
        posToPitch.put(271, (byte)62);
        posToPitch.put(282, (byte)60);
        posToPitch.put(303, (byte)59);
        posToPitch.put(324, (byte)67);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_sheet);

        scrollView = findViewById(R.id.musicSheetScroll);
        textView = findViewById(R.id.textView);
        table = findViewById(R.id.staffs);

        score = new Score(player);
        timeSignature = new Fraction(4, 4);
        score.addMeasure(timeSignature);

        final int measureLength = 192 * timeSignature.num / timeSignature.den;

        for(int i = 0; i < table.getChildCount(); i++){
            //for each row
            row = (TableRow)table.getChildAt(i);
            for(int j = 0; j < row.getChildCount(); j++){
                //each imageView in row
                imageView = (ImageView)row.getChildAt(j);
                DrawStaff(imageView);


                //create onTouchListener for each ImageView
                imageView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int imageX = (int)event.getX();
                        int imageY = (int)event.getY();
                        textView.setText("imageX: " + imageX + " imageY: " + imageY);
                        DrawNote(imageView, imageX, imageY);

                        imageX = snapToTime(imageX, measureLength, horizontalMax,
                                            score.getMeasure(0, 0, timeSignature));
                        imageY = snapToHeight(imageY, verticalMax, verticalStart, verticalOffset);



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
                        case MotionEvent.ACTION_UP:
                            byte[] midiEvent = new byte[3];

                            midiEvent[0] = (byte) (0x80 | 0);
                            midiEvent[1] = posToPitch.get(imageY);
                            midiEvent[2] = 127;

                            // Send the MIDI event to the synthesizer.
                            player.directWrite(midiEvent);
                            // TODO: get measure number (0-indexed) and add it multiplied by 192 to timePosition
                            score.addNote(0, imageX, 48, posToPitch.get(imageY), (byte)127);
                            // TODO: add image of note on position imageX, imageY on staff

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
    }

    public void DrawStaff(ImageView iv){

        //create bitmap and link to canvas (bitmap is a grid/matrix of pixels, canvas lets you change the pixels)
        bitmap = Bitmap.createBitmap(206, 130, Bitmap.Config.ARGB_8888); //working variabes 206, 130
        canvas = new Canvas(bitmap);

        //create the paint variable used by the canvas
        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2);

        //use drawLine to draw the lines from a start point x,y to an end point x,y
        float startPointY = 32;
        for(int i = 0; i < 5; i++){//5 lines printed
            canvas.drawLine(0, startPointY, horizontalMax, startPointY, linePaint);
            startPointY += 16;
        }

        //draw vertical lines
        canvas.drawLine(2, 32, 2, startPointY - 16, linePaint);
        canvas.drawLine(207, 32, 207, startPointY - 16, linePaint);

        //update imageViews bitmap
        imageView.setImageBitmap(bitmap);
    }

    public void DrawNote(ImageView iv, int x, int y){
        //drawOval(float left, float top, float right, float bottom, Paint paint)
        canvas.drawCircle (x, y, 5, linePaint);
        iv.setImageBitmap(bitmap);
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
    private int snapToTime(int touchPoint, int fromWidth, int toWidth, int[] points){
        int prevPoint;
        int nextPoint = 0;

        //if not in range, snap to nearest edge
        if (touchPoint <= points[0])
            return points[0];
        else if(touchPoint >= points[points.length - 1])
            return points[points.length - 1];
        else {
            for(int i = 0; i < points.length - 1; i++) {
                prevPoint = points[i]     * toWidth / fromWidth;
                nextPoint = points[i + 1] * toWidth / fromWidth;

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

    private int snapToHeight(int touchPoint, int max, int start, int offset){
        //if not in range, snap to nearest edge
        if (touchPoint <= start)
            return start;
        else if(touchPoint >= max)
            return max;
        else {
            touchPoint -= start;

            return start + ((touchPoint / offset)
                         + ((touchPoint % offset) * 2 / offset)) * offset;
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
