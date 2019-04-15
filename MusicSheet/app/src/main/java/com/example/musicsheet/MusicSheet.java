package com.example.musicsheet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Pair;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;

public class MusicSheet extends AppCompatActivity {
    enum NoteDur { WHOLE, HALF, QUARTER, EIGHTH }

    SparseArray<Byte> posToPitch;
    SparseIntArray pitchToPos;
    HashMap<Integer, ArrayList<Integer>> keys;
    HashMap<Integer, ArrayList<Integer>> reverseKeys;
    HashMap<Track.Clef, ArrayList<Integer>> clefMods;
    HashMap<Track.Clef, ArrayList<Integer>> reverseClefMods;

    int key;
    /* key values:
     * -8: Fb Major / db minor
     * -7: Cb Major / ab minor
     * -6: Gb Major / eb minor
     * -5: Db Major / bb minor
     * -4: Ab Major / f  minor
     * -3: Eb Major / c  minor
     * -2: Bb Major / g  minor
     * -1: F  Major / d  minor
     *  0: C  Major / a  minor
     * +1: G  Major / e  minor
     * +2: D  Major / b  minor
     * +3: A  Major / f# minor
     * +4: E  Major / c# minor
     * +5: B  Major / g# minor
     * +6: F# Major / d# minor
     * +7: C# Major / a# minor
     * +8: G# Major / e# minor
     */
    byte accidental;

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

    ScrollView scrollView;
    TableLayout table;
    TextView textView;


    int horizontalStart = 60;
    int horizontalMax = 542;

    int verticalStart = 19;
    int verticalOffset = 21; //hard-coded: eye-balled the distance between each bar lol
    int verticalMax = 334;

    int /*lastTouchPointX = 0, */lastTouchPointY = 0;

    private Player player = new Player();
    Score score;
    Fraction timeSignature;
    HashMap<ImageView, Pair<RelativeLayout, Measure>> measures;
    NoteDur selectedNoteDur;

    void setUpPosToPitch() {
        posToPitch = new SparseArray<>();

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
    }

    void setUpPitchToPos() {
        pitchToPos = new SparseIntArray();
        pitchToPos.put(83,  19);
        pitchToPos.put(81,  40);
        pitchToPos.put(79,  61);
        pitchToPos.put(77,  82);
        pitchToPos.put(76, 103);
        pitchToPos.put(74, 124);
        pitchToPos.put(72, 145);
        pitchToPos.put(71, 166);
        pitchToPos.put(69, 187);
        pitchToPos.put(67, 208);
        pitchToPos.put(65, 229);
        pitchToPos.put(64, 250);
        pitchToPos.put(62, 271);
        pitchToPos.put(60, 292);
        pitchToPos.put(59, 313);
        pitchToPos.put(57, 334);
    }

    void setUpKeys() {
        keys = new HashMap<>();
        reverseKeys = new HashMap<>();
        for (int i = -8; i <= 8; ++i) {
            keys.put(i, new ArrayList<Integer>((12)));
            reverseKeys.put(i, new ArrayList<Integer>((12)));
            for (int j = 0; j < 12; ++j) {
                Objects.requireNonNull(keys.get(i)).add(0);
                Objects.requireNonNull(reverseKeys.get(i)).add(0);
            }
        }
        for (int i = 1; i <= 8; ++i) {
            Objects.requireNonNull(keys.get(i)).set( 5,  1);  // F#
            Objects.requireNonNull(reverseKeys.get(i)).set( 6, -1);
        }
        for (int i = 2; i <= 8; ++i) {
            Objects.requireNonNull(keys.get(i)).set( 0,  1);  // C#
            Objects.requireNonNull(reverseKeys.get(i)).set( 1, -1);
        }
        for (int i = 3; i <= 8; ++i) {
            Objects.requireNonNull(keys.get(i)).set( 7,  1);  // G#
            Objects.requireNonNull(reverseKeys.get(i)).set( 8, -1);
        }
        for (int i = 4; i <= 8; ++i) {
            Objects.requireNonNull(keys.get(i)).set( 2,  1);  // D#
            Objects.requireNonNull(reverseKeys.get(i)).set( 3, -1);
        }
        for (int i = 5; i <= 8; ++i) {
            Objects.requireNonNull(keys.get(i)).set( 9,  1);  // A#
            Objects.requireNonNull(reverseKeys.get(i)).set(10, -1);
        }
        for (int i = 6; i <= 8; ++i) {
            Objects.requireNonNull(keys.get(i)).set( 4,  1);  // E#
            Objects.requireNonNull(reverseKeys.get(i)).set( 5, -1);
        }
        for (int i = 7; i <= 8; ++i) {
            Objects.requireNonNull(keys.get(i)).set(11,  1);  // B#
            Objects.requireNonNull(reverseKeys.get(i)).set( 0, -1);
        }
        Objects.requireNonNull(keys.get(8)).set( 5,  2);      // Fx
        Objects.requireNonNull(reverseKeys.get(8)).set( 7, -2);

        for (int i = -1; i >= -8; --i) {
            Objects.requireNonNull(keys.get(i)).set(11, -1);  // Bb
            Objects.requireNonNull(reverseKeys.get(i)).set(10,  1);
        }
        for (int i = -2; i >= -8; --i) {
            Objects.requireNonNull(keys.get(i)).set( 4, -1);  // Eb
            Objects.requireNonNull(reverseKeys.get(i)).set( 3,  1);
        }
        for (int i = -3; i >= -8; --i) {
            Objects.requireNonNull(keys.get(i)).set( 9, -1);  // Ab
            Objects.requireNonNull(reverseKeys.get(i)).set( 8,  1);
        }
        for (int i = -4; i >= -8; --i) {
            Objects.requireNonNull(keys.get(i)).set( 2, -1);  // Db
            Objects.requireNonNull(reverseKeys.get(i)).set( 1,  1);
        }
        for (int i = -5; i >= -8; --i) {
            Objects.requireNonNull(keys.get(i)).set( 7, -1);  // Gb
            Objects.requireNonNull(reverseKeys.get(i)).set( 6,  1);
        }
        for (int i = -6; i >= -8; --i) {
            Objects.requireNonNull(keys.get(i)).set( 0, -1);  // Cb
            Objects.requireNonNull(reverseKeys.get(i)).set(11,  1);
        }
        for (int i = -7; i >= -8; --i) {
            Objects.requireNonNull(keys.get(i)).set( 5, -1);  // Fb
            Objects.requireNonNull(reverseKeys.get(i)).set( 4,  1);
        }
        Objects.requireNonNull(keys.get(-8)).set(11, -2);     // Bbb
        Objects.requireNonNull(reverseKeys.get(-8)).set( 9,  2);
    }

    void setUpClefMods() {
        clefMods = new HashMap<>();
        reverseClefMods = new HashMap<>();
        clefMods.put(Track.Clef.TREBLE, new ArrayList<Integer>((12)));
        reverseClefMods.put(Track.Clef.TREBLE, new ArrayList<Integer>((12)));
        for (int i = 0; i < 12; ++i) {
            Objects.requireNonNull(clefMods.get(Track.Clef.TREBLE)).add(0);
            Objects.requireNonNull(
                    reverseClefMods.get(Track.Clef.TREBLE)).add(0);
        }

        clefMods.put(Track.Clef.ALTO, new ArrayList<Integer>((12)));
        reverseClefMods.put(Track.Clef.ALTO, new ArrayList<Integer>((12)));
        for (int i = 0; i < 12; ++i) {
            Objects.requireNonNull(clefMods.get(Track.Clef.ALTO)).add(-10);
            Objects.requireNonNull(
                    reverseClefMods.get(Track.Clef.ALTO)).add(10);
        }
        Objects.requireNonNull(clefMods.get(Track.Clef.ALTO)).set( 4, -11);
        Objects.requireNonNull(clefMods.get(Track.Clef.ALTO)).set(11, -11);
        Objects.requireNonNull(
                reverseClefMods.get(Track.Clef.ALTO)).set( 0, 11);
        Objects.requireNonNull(
                reverseClefMods.get(Track.Clef.ALTO)).set( 5, 11);

        clefMods.put(Track.Clef.BASS, new ArrayList<Integer>((12)));
        reverseClefMods.put(Track.Clef.BASS, new ArrayList<Integer>((12)));
        for (int i = 0; i < 12; ++i) {
            Objects.requireNonNull(clefMods.get(Track.Clef.BASS)).add(-21);
            Objects.requireNonNull(
                    reverseClefMods.get(Track.Clef.BASS)).add(21);
        }
        Objects.requireNonNull(clefMods.get(Track.Clef.BASS)).set( 0, -20);
        Objects.requireNonNull(clefMods.get(Track.Clef.BASS)).set( 5, -20);
        Objects.requireNonNull(clefMods.get(Track.Clef.BASS)).set( 7, -20);
        Objects.requireNonNull(
                reverseClefMods.get(Track.Clef.BASS)).set( 4, 20);
        Objects.requireNonNull(
                reverseClefMods.get(Track.Clef.BASS)).set( 9, 20);
        Objects.requireNonNull(
                reverseClefMods.get(Track.Clef.BASS)).set(11, 20);
    }

    @SuppressLint({"ClickableViewAccessibility", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        measures = new HashMap<>();
        selectedNoteDur = NoteDur.QUARTER;
        key = 0;

        setUpPosToPitch();
        setUpPitchToPos();
        setUpKeys();
        setUpClefMods();

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
        boolean newScore = true;

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            score.load(extras.getString("filename"));
            newScore = false;
        }

        timeSignature = new Fraction(4, 4);

        final ToggleButton zoomButton = findViewById(R.id.tempZoomButton);
        ImageSpan imageSpan = new ImageSpan((this),
                                            android.R.drawable.ic_menu_add);
        SpannableString content = new SpannableString("X");
        content.setSpan(imageSpan, (0), (1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        zoomButton.setText(content);
        zoomButton.setTextOn(content);
        zoomButton.setTextOff(content);

        final ToggleButton editButton = findViewById(R.id.editButton);
        final ToggleButton dotButton  = findViewById(R.id.dotButton);
        final ToggleButton restButton = findViewById(R.id.restButton);

        final int measureLength = 192 * timeSignature.num / timeSignature.den;

        Context context = getApplicationContext();

        int height = 10;
        boolean odd = false;
        int trackCount = 2;

        if (!newScore) {
            trackCount = score.getTrackCount();
            height = score.getMeasureCount() * trackCount / 2;
            odd = score.getMeasureCount() % 2 != 0;
        } else
            score.addTrack(new Track((byte)0, Track.Clef.BASS));

        int[] counts = new int[trackCount];

        for (int i = 0; i < (odd ? height + 1 : height); i++) {
            //for each row
            TableRow tr = new TableRow(context);

            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT));

            for (int j = 0; j < (odd && i == height ? 1 : 2); j++) {
                //each imageView in row
                TableRow.LayoutParams lp = new TableRow.LayoutParams();

                RelativeLayout rl = new RelativeLayout(context);

                rl.setLayoutParams(lp);
                rl.getLayoutParams().height
                        = (int)(130 * context.getResources()
                        .getDisplayMetrics().density);
                rl.getLayoutParams().width
                        = (int)(206 * context.getResources()
                        .getDisplayMetrics().density);

                ImageView iv = new ImageView(context);

                iv.setLayoutParams(lp);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);

                drawStaff(iv);

                if (newScore && i % trackCount == 0)
                    score.addMeasure(timeSignature);
                else {
                    if (counts[i % trackCount] < score.getMeasureCount()) {
                        ArrayList<Pair<Integer, LinkedList<Note>>> measure
                                = score.getMeasure((i % trackCount),
                                                   counts[i % trackCount],
                                                   timeSignature);
                        for (Pair<Integer, LinkedList<Note>> p : measure) {
                            NoteDur noteDur;
                            boolean dotted = false;

                            switch (p.second.getFirst().getDuration()) {
                            case 192:
                                noteDur = NoteDur.WHOLE;
                                break;
                            case 148:
                                noteDur = NoteDur.HALF;
                                dotted = true;
                                break;
                            case 96:
                                noteDur = NoteDur.HALF;
                                break;
                            case 72:
                                noteDur = NoteDur.QUARTER;
                                dotted = true;
                                break;
                            case 48:
                                noteDur = NoteDur.QUARTER;
                                break;
                            case 36:
                                noteDur = NoteDur.EIGHTH;
                                dotted = true;
                                break;
                            default:
                                noteDur = NoteDur.EIGHTH;
                            }

                            for (Note n : p.second) {
                                byte pitch = (byte)(n.getPitch()
                                        + Objects.requireNonNull(
                                                reverseKeys.get(key))
                                           .get(n.getPitch() % 12));

                                drawNote(rl, p.first, pitchToPos.get(
                                        pitch
                                      + reverseClefMods.get(score.getTrackClef(
                                                i % trackCount))
                                        .get(pitch % 12)), n,
                                        noteDur, dotted, (false));
                            }
                        }
                    }
                }

                measures.put(iv, new Pair<>(rl,
                        new Measure((i % trackCount), counts[i % trackCount])));

                ++counts[i % trackCount];

                //create onTouchListener for each ImageView
                iv.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (editButton.isChecked() && !player.running) {
                            boolean dotting = dotButton.isChecked();
                            boolean resting = restButton.isChecked();
                            @SuppressWarnings("SuspiciousMethodCalls")
                            RelativeLayout rl = measures.get(v).first;
                            @SuppressWarnings("SuspiciousMethodCalls")
                            Measure m = measures.get(v).second;
                            int imageX = (int)(event.getX()
                                             / getApplicationContext()
                                               .getResources()
                                                .getDisplayMetrics().density
                                             * 2.625);
                            int imageY = (int)(event.getY()
                                             / getApplicationContext()
                                               .getResources()
                                                .getDisplayMetrics().density
                                             * 2.625);

                            //textView.setText("imageX: " + imageX + " imageY: " + imageY);

                            assert m != null;
                            imageX = snapToTime((imageX - horizontalStart),
                                                measureLength,
                                                (horizontalMax
                                               - horizontalStart),
                                                score.getMeasure(m.staff,
                                                        m.number,
                                                        timeSignature));
                            imageY = snapToHeight(imageY, verticalMax,
                                                  verticalStart,
                                                  verticalOffset);

                            //TODO: fix/optimize zoom
                            zoomInit();  //takes position of last drawStaff
                            position.x = (int)event.getX(); //same as imageX, imageY
                            position.y = (int)event.getY();

                            //int scrollHeight = scrollView.getChildAt(0).getHeight();  //screen
                            //int scrollWidth = scrollView.getChildAt(0).getWidth();

                            zoomLoc = new Canvas(bitmap); //screenState

                            switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN: {
                                if (zoomButton.isChecked()) {
                                    initPoint = true;
                                    zooming = true;
                                    zoomDraw(zoomLoc);
                                    scrollView.invalidate();
                                } else {
                                    byte[] midiEvent = new byte[3];

                                    byte p = posToPitch.get(imageY);

                                    midiEvent[0] = (byte) (0x90 | m.staff);
                                    midiEvent[1] =
                                            (byte)(p
                                                 + Objects.requireNonNull(
                                                           keys.get(key))
                                                    .get(p % 12)
                                                 + accidental
                                                 + clefMods.get(
                                                           score
                                                           .getTrackClef(
                                                                   m.staff))
                                                   .get(p % 12));
                                    midiEvent[2] = 127;

                                    // Send the MIDI event to the synthesizer.
                                    player.directWrite(midiEvent);
                                }
                            }
                            break;
                            case MotionEvent.ACTION_MOVE:
                                if (lastTouchPointY != imageY) {
                                    if (zoomButton.isChecked()) {
                                        initPoint = true;
                                        zooming = true;
                                        zoomDraw(zoomLoc);
                                        scrollView.invalidate();
                                    }
                                    else
                                        {
                                        byte[] midiEvent = new byte[6];
                                        byte lP = posToPitch.get(
                                                lastTouchPointY);
                                        byte p  = posToPitch.get(imageY);

                                        midiEvent[0]
                                                = (byte)(0x80 | m.staff);
                                        midiEvent[1] =
                                                (byte)(lP
                                                     + Objects
                                                       .requireNonNull(
                                                               keys
                                                               .get(key))
                                                        .get(lP % 12)
                                                     + accidental
                                                     + clefMods.get(
                                                               score
                                                               .getTrackClef
                                                                (m.staff))
                                                       .get(p % 12));
                                        midiEvent[2] = 127;
                                        midiEvent[3]
                                                = (byte)(0x90 | m.staff);
                                        midiEvent[4] =
                                                (byte)(p
                                                     + Objects
                                                       .requireNonNull(
                                                               keys
                                                               .get(key))
                                                        .get(p % 12)
                                                     + accidental
                                                     + clefMods.get(
                                                               score
                                                               .getTrackClef
                                                                (m.staff))
                                                       .get(p % 12));
                                        midiEvent[5] = 127;

                                        // Send the MIDI event to the synthesizer.
                                        player.directWrite(midiEvent);
                                    }
                                }

                                //textView.setText("imageX: " + imageX + " imageY: " + imageY);
                                break;
                            case MotionEvent.ACTION_UP: {
                                if (zoomButton.isChecked()) {
                                    zooming = false;
                                    //zoomDraw(zoomLoc);
                                    canvas.drawColor(Color.WHITE);
                                    canvas.drawBitmap(screenState,
                                                      matrix, bmPaint);
                                    scrollView.invalidate();
                                } else {
                                    byte[] midiEvent = new byte[3];
                                    byte p  = posToPitch.get(imageY);

                                    midiEvent[0] = (byte) (0x80 | m.staff);
                                    midiEvent[1] =
                                            (byte)(p
                                                 + Objects.requireNonNull(
                                                         keys.get(key))
                                                   .get(p % 12)
                                                 + accidental
                                                 + clefMods.get(
                                                           score
                                                           .getTrackClef(
                                                                   m.staff))
                                                   .get(p % 12));
                                    midiEvent[2] = 127;

                                    // Send the MIDI event to the synthesizer.
                                    player.directWrite(midiEvent);

                                    NoteDur actualNoteDur = selectedNoteDur;
                                    int gottenDur = score.durationAtTime(
                                            m.staff,
                                            (m.number * 192 + imageX));
                                    int duration;
                                    boolean shouldBeDotted = dotting;

                                    switch (selectedNoteDur) {
                                    case WHOLE:
                                        duration = 192;
                                        break;
                                    case HALF:
                                        duration =  96;
                                        break;
                                    case QUARTER:
                                        duration =  48;
                                        break;
                                    default:
                                        duration =  24;
                                    }

                                    if (dotting)
                                        duration += duration >> 1;

                                    if (gottenDur != 0
                                     && gottenDur != duration)
                                        switch (gottenDur) {
                                        case 192:
                                            actualNoteDur
                                                    = NoteDur.WHOLE;
                                            break;
                                        case 148:
                                            actualNoteDur
                                                    = NoteDur.HALF;
                                            shouldBeDotted  = true;
                                            break;
                                        case  96:
                                            actualNoteDur
                                                    = NoteDur.HALF;
                                            break;
                                        case  72:
                                            actualNoteDur
                                                    = NoteDur.QUARTER;
                                            shouldBeDotted  = true;
                                            break;
                                        case  48:
                                            actualNoteDur
                                                    = NoteDur.QUARTER;
                                            break;
                                        case  36:
                                            actualNoteDur
                                                    = NoteDur.EIGHTH;
                                            shouldBeDotted  = true;
                                            break;
                                        default:
                                            actualNoteDur
                                                    = NoteDur.EIGHTH;
                                        }
                                    Note n = new Note(
                                            resting ? Note.NoteType.REST
                                                    : Note.NoteType.MELODIC,
                                            gottenDur == 0 ? duration
                                                           : gottenDur,
                                            resting ? (byte)0
                                                    : (byte)(
                                                    p
                                                  + Objects.requireNonNull(
                                                            keys.get(key))
                                                            .get(p % 12)
                                                          + clefMods.get(
                                                             score
                                                             .getTrackClef(
                                                              m.staff))
                                                            .get(p % 12)),
                                            accidental, (byte)127);

                                    score.addNote(
                                            m.staff,
                                            (m.number * 192 + imageX), n);

                                    drawNote(rl, imageX, imageY,
                                             n, actualNoteDur,
                                             shouldBeDotted, (false));

                                    accidental = 0;
                                    ImageView accImg
                                            = findViewById(R.id.accidentButton);
                                    accImg.setImageResource(R.drawable.natural);
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
                        }
                        return true;
                    }
                });
                rl.addView(iv);
                tr.addView(rl);
            }
            table.addView(tr);
        }
    }

    public void drawStaff(ImageView iv){
        //takes predetermined width and height dimensions from ImageViews and converts to pixels
        float dipW = 206f;
//        Resources r = getResources();
//        float pxW = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//                                              dipW, r.getDisplayMetrics());
        float dipH = 130f;
//        float pxH = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//                                              dipH, r.getDisplayMetrics());

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
        iv.setImageBitmap(bitmap);
    }

    //Draws note on a given X and Y coordinate
    //Currently, it takes the old staff bar image coordinates and manually converts them to xy coordinates that
    //align with the new bitmap staff bars
    public void drawNote(RelativeLayout rl, int x, int y, Note n,
                         NoteDur dur, boolean dotted, boolean positionFilled) {
        if (n.getNoteType() != Note.NoteType.REST) {
            ImageView noteIv = new ImageView(getApplicationContext());
            //noteIv.setScaleType(ImageView.ScaleType.CENTER);

            int xActual = x * 50 / 48 + 25;
            int yActual = (y - 19) * 8 / 21 + 8;

            RelativeLayout.LayoutParams params
                    = new RelativeLayout.LayoutParams(xActual, yActual);

            params.leftMargin = xActual;
            params.topMargin = yActual;
            params.width = 100;
            params.height = 100;
            n.setImageView(noteIv);

            switch (dur)
            {
                case WHOLE:
                    noteIv.setImageResource(R.drawable.wholenote);
                    params.height = 65;
                    params.width = 65;
                    break;
                case HALF:
                    noteIv.setImageResource(R.drawable.halfnote);
                    break;
                case QUARTER:
                    noteIv.setImageResource(R.drawable.quarternote);
                    break;
                case EIGHTH:
                    noteIv.setImageResource(R.drawable.eighthnote);

            }

            rl.addView(noteIv, params);
        }

        /*Bitmap previousBitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
        Canvas newCan = new Canvas(previousBitmap);


        switch (n.getNoteType()) {
        case MELODIC:
            newCan.drawCircle(xActual, yActual, (5),
                    (dur == NoteDur.WHOLE || dur == NoteDur.HALF) ? linePaint
                                                                  : fillPaint);
            if (dur != NoteDur.WHOLE)
                newCan.drawLine((xActual + (y < 166 ? -5 : 5)), yActual,
                                (xActual + (y < 166 ? -5 : 5)),
                                (yActual + (y < 166 ? 56 : -56)), linePaint);
            if (dotted)
                newCan.drawCircle(xActual + 9, yActual, (1), fillPaint);
            break;
        default:
        }
        iv.setImageBitmap(previousBitmap);*/
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
    /*public void zoomRefresh(){
        screenState = Bitmap.createBitmap(getScreen(scrollView));
        bmShader = new BitmapShader(screenState, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        bmPaint.setShader(bmShader);
    }*/

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
    private int snapToTime(int touchPoint, int fromWidth, int toWidth,
                           ArrayList<Pair<Integer, LinkedList<Note>>> points){
        int prevPoint;
        int nextPoint = points.get(0).first * toWidth / fromWidth;

        //if not in range, snap to nearest edge
        if (touchPoint <= points.get(0).first)
            return points.get(0).first;
        else if(touchPoint >= points.get(points.size() - 1).first * toWidth / fromWidth)
            return points.get(points.size() - 1).first;
        else {
            for(int i = 1; i < points.size() ; i++) {
                prevPoint = nextPoint;
                nextPoint = points.get(i).first * toWidth / fromWidth;

                //find specific range
                if(touchPoint >= prevPoint && touchPoint <= nextPoint){
                    //compare distances
                    int leftDistance  = touchPoint - prevPoint;
                    int rightDistance = nextPoint  - touchPoint;

                    if(leftDistance < rightDistance)
                        return points.get(i - 1).first;
                    else
                        return points.get(i).first;
                }//end range check
            }//end for
            return points.get(points.size() - 1).first;
        }
    }

    private int snapToHeight(int touchPoint, int max, int start, int offset) {
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
    /*private void goToNotePanel(){
        ImageButton btnNote = (ImageButton)findViewById(R.id.noteButton);
        btnNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MusicSheet.this, NotePanel.class));
            }
        });
    }*/


    //BUTTONS---

    public void play(View view)    { score.play(); }

    public void pause(View view)   { score.pause(); }

    public void restart(View view) { score.resetPlayPos(); }

    public void undo(View view){
        //remove last note added
    }

    public void cycleNoteType(View view) {
        ImageView image = findViewById(R.id.noteButton);

        switch (selectedNoteDur) {
        case WHOLE:
            selectedNoteDur = NoteDur.HALF;
            image.setImageResource(R.drawable.halfnote);
            break;
        case HALF:
            selectedNoteDur = NoteDur.QUARTER;
            image.setImageResource(R.drawable.quarternote);
            break;
        case QUARTER:
            selectedNoteDur = NoteDur.EIGHTH;
            image.setImageResource(R.drawable.eighthnote);
            break;
        case EIGHTH:
            selectedNoteDur = NoteDur.WHOLE;
            image.setImageResource(R.drawable.wholenote);
        }
    }

    public void cycleAccidental(View view) {
        ImageView image = findViewById(R.id.accidentButton);

        ++accidental;

        if (accidental == 2)
            accidental = -1;

        switch(accidental)
        {
        case 0:
            image.setImageResource(R.drawable.natural);
            break;
        case 1:
            image.setImageResource(R.drawable.sharp);
            break;
        case -1:
            image.setImageResource(R.drawable.flat);
            break;
        }
    }

    public void save(View view) {
        ActivityCompat.requestPermissions(MusicSheet.this, new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        }, 100);

        LayoutInflater li = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View promptView = li.inflate(R.layout.save_prompt, (null));
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setView(promptView);

        final EditText userInput = promptView.findViewById(R.id.editTextDialogUserInput);

        alertDialogBuilder.setCancelable(false).setPositiveButton(("OK"),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                score.save(userInput.getText().toString());
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
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
