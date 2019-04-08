package com.example.musicsheet;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.HashMap;

public class MusicSheet extends AppCompatActivity {
    enum NoteType { WHOLE, HALF, QUARTER }

    HashMap<Integer, Byte> posToPitch;
    final int horizontal = 4;
    final int vertical = 9;

    //Drawing Variables
    Bitmap bitmap;
    Canvas canvas;
    Paint linePaint, fillPaint;

    boolean zooming;
    boolean initPoint;
    PointF position;
    Matrix matrix;
    BitmapShader bmShader;
    Paint paint = new Paint(Color.BLACK);
    Paint bmPaint;
    Paint outlinePaint;
    Bitmap screenState;
    Canvas zoomLoc;

    int[][] staffPositions = new int[horizontal][vertical];

    ScrollView scrollView;
    TableLayout table;
    ImageView imageView;
    TextView textView;
    TableRow row;


    int horizontalStart = 60;
    int horizontalMax = 542;
    int horizontalOffset = (horizontalMax-horizontalStart)/staffPositions.length-1; //hard-coded: splits bar into 4

    int verticalStart = 19;
    int verticalOffset = 21; //hard-coded: eye-balled the distance between each bar lol
    int verticalMax = 334;

    int lastTouchPointX = 0, lastTouchPointY = 0;

    private Player player = new Player();
    Score score;
    Fraction timeSignature;
    HashMap<ImageView, Measure> measures;
    NoteType selectedNoteType;


    @SuppressLint({"ClickableViewAccessibility", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        posToPitch = new HashMap<>();
        measures   = new HashMap<>();
        selectedNoteType = NoteType.QUARTER;
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
        posToPitch.put(292, (byte)60);
        posToPitch.put(313, (byte)59);
        posToPitch.put(334, (byte)57);

        //create the paint variables used by the canvas
        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2);

        fillPaint = new Paint();
        fillPaint.setColor(Color.BLACK);
        fillPaint.setAntiAlias(true);
        fillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        fillPaint.setStrokeWidth(2);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_sheet);

        scrollView = findViewById(R.id.musicSheetScroll);
        textView = findViewById(R.id.textView);
        table = findViewById(R.id.staffs);

        score = new Score(player);
        timeSignature = new Fraction(4, 4);

        final ToggleButton zoomButton = (ToggleButton) findViewById(R.id.tempZoomButton);
        ImageSpan imageSpan = new ImageSpan(this, android.R.drawable.ic_menu_add);
        SpannableString content = new SpannableString("X");
        content.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        zoomButton.setText(content);
        zoomButton.setTextOn(content);
        zoomButton.setTextOff(content);

        final int measureLength = 192 * timeSignature.num / timeSignature.den;

        int count = 0;

        for(int i = 0; i < table.getChildCount(); i++){
            //for each row
            row = (TableRow)table.getChildAt(i);
            for(int j = 0; j < row.getChildCount(); j++){
                //each imageView in row
                imageView = (ImageView)row.getChildAt(j);
                drawStaff(imageView);
                score.addMeasure(timeSignature);

                measures.put(imageView, new Measure(0, count));

                ++count;

                //create onTouchListener for each ImageView
                imageView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Measure m = measures.get(v);
                        int imageX = (int)event.getX();
                        int imageY = (int)event.getY();

                        //textView.setText("imageX: " + imageX + " imageY: " + imageY);

                        imageX = snapToTime((imageX - horizontalStart), measureLength,
                                            (horizontalMax - horizontalStart),
                                            score.getMeasure(m.staff, m.number, timeSignature));
                        imageY = snapToHeight(imageY, verticalMax, verticalStart, verticalOffset);

                        //TODO: fix/optimize zoom
                        zoomInit();  //takes position of last drawStaff
                        position.x = (int)event.getX(); //same as imageX, imageY
                        position.y = (int)event.getY();

                        //int scrollHeight = scrollView.getChildAt(0).getHeight();  //screen
                        //int scrollWidth = scrollView.getChildAt(0).getWidth();

                        zoomLoc = new Canvas(bitmap); //screenState

                        switch(event.getAction()){
                        case MotionEvent.ACTION_DOWN: {
                            byte[] midiEvent = new byte[3];

                            midiEvent[0] = (byte) (0x90 | 0);
                            midiEvent[1] = posToPitch.get(imageY);
                            midiEvent[2] = 127;

                            // Send the MIDI event to the synthesizer.
                            player.directWrite(midiEvent);

                            if(zoomButton.isChecked()){
                                initPoint = true;
                                zooming = true;
                                zoomDraw(zoomLoc);
                                scrollView.invalidate();
                            }
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

                                if(zoomButton.isChecked()){
                                    initPoint = true;
                                    zooming = true;
                                    zoomDraw(zoomLoc);
                                    scrollView.invalidate();
                                }
                            }

                            //textView.setText("imageX: " + imageX + " imageY: " + imageY);
                            break;
                        case MotionEvent.ACTION_UP: {
                            byte[] midiEvent = new byte[3];

                            midiEvent[0] = (byte) (0x80 | 0);
                            midiEvent[1] = posToPitch.get(imageY);
                            midiEvent[2] = 127;

                            // Send the MIDI event to the synthesizer.
                            player.directWrite(midiEvent);

                            int duration;
                            switch (selectedNoteType) {
                            case WHOLE:
                                duration = 192;
                                break;
                            case HALF:
                                duration =  96;
                                break;
                            default:
                                duration =  48;
                            }

                            score.addNote(m.staff, (m.number * 192 + imageX), duration,
                                    posToPitch.get(imageY), (byte) 127);
                            drawNote((ImageView)v, imageX, imageY, selectedNoteType);

                            if(zoomButton.isChecked()){
                                zooming = false;
                                //ZoomDraw(canvas);
                                canvas.drawColor(Color.WHITE);
                                canvas.drawBitmap(screenState, matrix, bmPaint);
                                scrollView.invalidate();
                            }
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
    }

    public void drawStaff(ImageView iv){
        //takes predetermined width and height dimensions from ImageViews and converts to pixels
        float dipW = 206f;
        Resources r = getResources();
        float pxW = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dipW,
                r.getDisplayMetrics()
        );

        float dipH = 130f;
        float pxH = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dipH,
                r.getDisplayMetrics()
        );

        //pretty sure it doesnt work tho haha
        bitmap = Bitmap.createBitmap((int)dipW, (int)dipH, Bitmap.Config.ARGB_8888); //working variabes 206, 130
        canvas = new Canvas(bitmap);

        //use drawLine to draw the lines from a start point x,y to an end point x,y
        float startPointY = 32;
        for(int i = 0; i < 5; i++){//5 lines printed
            canvas.drawLine((0), startPointY, horizontalMax, startPointY, linePaint);
            startPointY += 16;
        }

        //draw vertical lines
        canvas.drawLine(  2, 32,   2, startPointY - 16, linePaint);
        canvas.drawLine(207, 32, 207, startPointY - 16, linePaint);

        //update imageViews bitmap
        imageView.setImageBitmap(bitmap);
    }

    //Draws note on a given X and Y coordinate
    //Currently, it takes the old staff bar image coordinates and manually converts them to xy coordinates that
    //align with the new bitmap staff bars
    public void drawNote(ImageView iv, int x, int y, NoteType nt) {
        Bitmap previousBitmap = ((BitmapDrawable)iv.getDrawable()).getBitmap();
        Canvas newCan = new Canvas(previousBitmap);

        int xActual = x * 50 / 48 + 25;
        int yActual = (y - 19) * 8 / 21 + 8;
        newCan.drawCircle(xActual, yActual, (5),
                          (nt == NoteType.WHOLE || nt == NoteType.HALF) ? linePaint : fillPaint);
        if (nt != NoteType.WHOLE)
            newCan.drawLine((xActual + 5), yActual, (xActual + 5),
                            (yActual + (y < 166 ? 56 : -56)), linePaint);
        iv.setImageBitmap(previousBitmap);
    }

    public void DrawNote(ImageView iv, int x, int y){
        //drawOval(float left, float top, float right, float bottom, Paint paint)
        canvas.drawCircle (x, y, 5, linePaint);
        iv.setImageBitmap(bitmap);
    }

    //readies necessary dependencies for zoom
    //takes last drawStaff for canvas, fix?
    public void zoomInit(){
        //bitmap = Bitmap.createBitmap(206, 130, Bitmap.Config.ARGB_8888);
        //canvas = new Canvas(bitmap);

        position = new PointF();
        //position.x = e.getX();
        //position.y = e.getY();

        matrix = new Matrix();
        screenState = Bitmap.createBitmap(getScreen(scrollView));
        bmShader = new BitmapShader(screenState, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        bmPaint = new Paint();
        bmPaint.setShader(bmShader);
        outlinePaint = new Paint(Color.BLACK);
        outlinePaint.setStyle(Paint.Style.STROKE);

    }

    //too much drawing?
    //idea was to keep backup of working bitmap
    //refresh after MotionEvent
    //need to redraw staves as well as zoom, if zooming
    public void zoomRefresh(){
        screenState = Bitmap.createBitmap(getScreen(scrollView));
        bmShader = new BitmapShader(screenState, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        bmPaint.setShader(bmShader);
    }

    //draws enlarged bitmap in circular shape
    //needs to update as moving position
    public void zoomDraw(@NonNull Canvas canvas){

        if(zooming) {
            matrix.reset();
            matrix.postScale(2f, 2f, position.x, position.y);
            bmPaint.getShader().setLocalMatrix(matrix);
            RectF src = new RectF(position.x - 50, position.y - 50, position.x + 50, position.y + 50);
            RectF dst = new RectF(0, 0, 100, 100);
            matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);
            matrix.postScale(2f, 2f);
            bmPaint.getShader().setLocalMatrix(matrix);

            //canvas.
            canvas.drawCircle(103, 50, 100, bmPaint);
            canvas.drawCircle(position.x, position.y, 100, bmPaint);
            canvas.drawCircle(position.x - 250, position.y - 250, 10, outlinePaint);
        }
        if(initPoint)
            canvas.drawCircle(position.x, position.y, 10, paint);
    }

    /*
    public void zoomSet(ImageView iv){
        float dipW = 157f;
        Resources r = getResources();
        float pxW = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dipW,
                r.getDisplayMetrics()
        );

        float dipH = 150f;
        float pxH = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dipH,
                r.getDisplayMetrics()
        );

        screenState = Bitmap.createBitmap((int)dipW, (int)dipH, Bitmap.Config.ARGB_8888); //working variabes 206, 130
        zoomLoc = new Canvas(screenState);
        imageview2.setImageBitmap(screenState);
    }
    */

    //gets current screen as bitmap
    public Bitmap getScreen(View view){
        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        view.buildDrawingCache();

        if(view.getDrawingCache() == null)
            return null;

        Bitmap screenshot = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        view.destroyDrawingCache();
        return screenshot;
    }

    //generates toggle button with custom image
   /* public void setToggle(){
        ToggleButton zoomButton = (ToggleButton) findViewById(R.id.tempZoomButton);
        ImageSpan imageSpan = new ImageSpan(this, android.R.drawable.ic_menu_add);
        SpannableString content = new SpannableString("X");
        content.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        zoomButton.setText(content);
        zoomButton.setTextOn(content);
        zoomButton.setTextOff(content);
    }
*/
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
        int nextPoint = points[0] * toWidth / fromWidth;

        //if not in range, snap to nearest edge
        if (touchPoint <= points[0])
            return points[0];
        else if(touchPoint >= points[points.length - 1] * toWidth / fromWidth)
            return points[points.length - 1];
        else {
            for(int i = 1; i < points.length ; i++) {
                prevPoint = nextPoint;
                nextPoint = points[i] * toWidth / fromWidth;

                //find specific range
                if(touchPoint >= prevPoint && touchPoint <= nextPoint){
                    //compare distances
                    int leftDistance  = touchPoint - prevPoint;
                    int rightDistance = nextPoint  - touchPoint;

                    if(leftDistance < rightDistance)
                        return points[i - 1];
                    else
                        return points[i];
                }//end range check
            }//end for
            return points[points.length - 1];
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


    /*private void goToInstrumentPanel(){
        ImageButton btnInstr = (ImageButton)findViewById(R.id.addInstrumentButton);
        btnInstr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MusicSheet.this, InstrumentPanel.class));
            }
        });
    }
*/
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

    public void cycleNoteType(View view) {
        switch (selectedNoteType) {
        case WHOLE:
            selectedNoteType = NoteType.HALF;
            break;
        case HALF:
            selectedNoteType = NoteType.QUARTER;
            break;
        case QUARTER:
            selectedNoteType = NoteType.WHOLE;
            break;
        }
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
