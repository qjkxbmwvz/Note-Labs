package com.example.musicsheet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Stack;

public class MusicSheet extends AppCompatActivity {
    enum NoteDur {WHOLE, HALF, QUARTER, EIGHTH}

    ZoomView zv;

    SparseIntArray pitchToPos;
    SparseArray<ArrayList<Integer>> keys;
    SparseArray<ArrayList<Integer>> reverseKeys;
    HashMap<Track.Clef, ArrayList<Integer>> clefMods;
    HashMap<Track.Clef, ArrayList<Integer>> reverseClefMods;

    Stack<Edit> editHistory;

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

    /*boolean zooming;
    boolean initPoint;
    PointF position;
    Matrix matrix;
    BitmapShader bmShader;
    Paint paint = new Paint(Color.BLACK);
    Paint bmPaint;
    Paint outlinePaint;
    Bitmap screenState;
    Canvas zoomLoc;*/

    ScrollView scrollView;
    TableLayout table;

    int horizontalStart = 60;
    int horizontalMax = 542;

    int verticalStart = 19;
    int verticalOffset = 21;
    //hard-coded: eye-balled the distance between each bar lol
    int verticalMax = 334;

    int lastTouchPointX = 0, lastTouchPointY = 0;

    private Player player = new Player();
    Score score;
    Fraction timeSignature;

    HashMap<ImageView, Pair<RelativeLayout, Measure>> measures;
    NoteDur selectedNoteDur;
    Note tempNote;

    // This happens to have its rounding errors in all the right places.
    byte posToPitch(int pos) {
        return (byte)(((334 - pos) / 21 + 27) * 12 / 7 + 11);
    }

    // This one hardcodes the values into a data structure because
    // the mathematical approach produces the wrong values.
    void setUpPitchToPos() {
        pitchToPos = new SparseIntArray();
        pitchToPos.put(83, 19);
        pitchToPos.put(81, 40);
        pitchToPos.put(79, 61);
        pitchToPos.put(77, 82);
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
        keys = new SparseArray<>();
        reverseKeys = new SparseArray<>();
        for (int i = -8; i <= 8; ++i) {
            keys.put(i, new ArrayList<Integer>((12)));
            reverseKeys.put(i, new ArrayList<Integer>((12)));
            for (int j = 0; j < 12; ++j) {
                Objects.requireNonNull(keys.get(i)).add(0);
                Objects.requireNonNull(reverseKeys.get(i)).add(0);
            }
        }
        for (int i = 1; i <= 8; ++i) {
            Objects.requireNonNull(keys.get(i)).set(5, 1);  // F#
            Objects.requireNonNull(reverseKeys.get(i)).set(6, -1);
        }
        for (int i = 2; i <= 8; ++i) {
            Objects.requireNonNull(keys.get(i)).set(0, 1);  // C#
            Objects.requireNonNull(reverseKeys.get(i)).set(1, -1);
        }
        for (int i = 3; i <= 8; ++i) {
            Objects.requireNonNull(keys.get(i)).set(7, 1);  // G#
            Objects.requireNonNull(reverseKeys.get(i)).set(8, -1);
        }
        for (int i = 4; i <= 8; ++i) {
            Objects.requireNonNull(keys.get(i)).set(2, 1);  // D#
            Objects.requireNonNull(reverseKeys.get(i)).set(3, -1);
        }
        for (int i = 5; i <= 8; ++i) {
            Objects.requireNonNull(keys.get(i)).set(9, 1);  // A#
            Objects.requireNonNull(reverseKeys.get(i)).set(10, -1);
        }
        for (int i = 6; i <= 8; ++i) {
            Objects.requireNonNull(keys.get(i)).set(4, 1);  // E#
            Objects.requireNonNull(reverseKeys.get(i)).set(5, -1);
        }
        for (int i = 7; i <= 8; ++i) {
            Objects.requireNonNull(keys.get(i)).set(11, 1);  // B#
            Objects.requireNonNull(reverseKeys.get(i)).set(0, -1);
        }
        Objects.requireNonNull(keys.get(8)).set(5, 2);      // Fx
        Objects.requireNonNull(reverseKeys.get(8)).set(7, -2);

        for (int i = -1; i >= -8; --i) {
            Objects.requireNonNull(keys.get(i)).set(11, -1);  // Bb
            Objects.requireNonNull(reverseKeys.get(i)).set(10, 1);
        }
        for (int i = -2; i >= -8; --i) {
            Objects.requireNonNull(keys.get(i)).set(4, -1);  // Eb
            Objects.requireNonNull(reverseKeys.get(i)).set(3, 1);
        }
        for (int i = -3; i >= -8; --i) {
            Objects.requireNonNull(keys.get(i)).set(9, -1);  // Ab
            Objects.requireNonNull(reverseKeys.get(i)).set(8, 1);
        }
        for (int i = -4; i >= -8; --i) {
            Objects.requireNonNull(keys.get(i)).set(2, -1);  // Db
            Objects.requireNonNull(reverseKeys.get(i)).set(1, 1);
        }
        for (int i = -5; i >= -8; --i) {
            Objects.requireNonNull(keys.get(i)).set(7, -1);  // Gb
            Objects.requireNonNull(reverseKeys.get(i)).set(6, 1);
        }
        for (int i = -6; i >= -8; --i) {
            Objects.requireNonNull(keys.get(i)).set(0, -1);  // Cb
            Objects.requireNonNull(reverseKeys.get(i)).set(11, 1);
        }
        for (int i = -7; i >= -8; --i) {
            Objects.requireNonNull(keys.get(i)).set(5, -1);  // Fb
            Objects.requireNonNull(reverseKeys.get(i)).set(4, 1);
        }
        Objects.requireNonNull(keys.get(-8)).set(11, -2);     // Bbb
        Objects.requireNonNull(reverseKeys.get(-8)).set(9, 2);
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
        Objects.requireNonNull(clefMods.get(Track.Clef.ALTO)).set(4, -11);
        Objects.requireNonNull(clefMods.get(Track.Clef.ALTO)).set(11, -11);
        Objects.requireNonNull(
          reverseClefMods.get(Track.Clef.ALTO)).set(0, 11);
        Objects.requireNonNull(
          reverseClefMods.get(Track.Clef.ALTO)).set(5, 11);

        clefMods.put(Track.Clef.BASS, new ArrayList<Integer>((12)));
        reverseClefMods.put(Track.Clef.BASS, new ArrayList<Integer>((12)));
        for (int i = 0; i < 12; ++i) {
            Objects.requireNonNull(clefMods.get(Track.Clef.BASS)).add(-21);
            Objects.requireNonNull(
              reverseClefMods.get(Track.Clef.BASS)).add(21);
        }
        Objects.requireNonNull(clefMods.get(Track.Clef.BASS)).set(0, -20);
        Objects.requireNonNull(clefMods.get(Track.Clef.BASS)).set(5, -20);
        Objects.requireNonNull(clefMods.get(Track.Clef.BASS)).set(7, -20);
        Objects.requireNonNull(
          reverseClefMods.get(Track.Clef.BASS)).set(4, 20);
        Objects.requireNonNull(
          reverseClefMods.get(Track.Clef.BASS)).set(9, 20);
        Objects.requireNonNull(
          reverseClefMods.get(Track.Clef.BASS)).set(11, 20);
    }

    Pair<NoteDur, Boolean> numToDurAndDot(int num) {
        NoteDur noteDur;
        boolean dotted = false;

        switch (num) {
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

        return new Pair<>(noteDur, dotted);
    }

    @SuppressLint({"ClickableViewAccessibility", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        measures = new HashMap<>();
        selectedNoteDur = NoteDur.QUARTER;
        key = 0;

        setUpPitchToPos();
        setUpKeys();
        setUpClefMods();

        editHistory = new Stack<>();

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
        zv = new ZoomView(this);
        scrollView.addView(zv);
        table = new TableLayout(this);
        table.setLayoutParams(new LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.MATCH_PARENT,
          LinearLayout.LayoutParams.MATCH_PARENT));

        zv.addView(table);

        score = new Score(player);
        boolean newScore = true;

        tempNote = null;

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            score.load(extras.getString("filename"));
            newScore = false;
        }

        timeSignature = new Fraction(4, 4);

        final ToggleButton editButton = findViewById(R.id.editButton);
        final ToggleButton dotButton = findViewById(R.id.dotButton);
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
                            Pair<NoteDur, Boolean> durAndDot = numToDurAndDot(
                              p.second.getFirst().getDuration());

                            NoteDur noteDur = durAndDot.first;
                            boolean dotted = durAndDot.second;

                            for (Note n : p.second) {
                                byte pitch = (byte)(n.getPitch()
                                                    + Objects.requireNonNull(
                                  reverseKeys.get(key))
                                                             .get(n.getPitch()
                                                                  % 12));

                                drawNote(rl, p.first, pitchToPos.get(
                                  pitch
                                  + Objects.requireNonNull(
                                    reverseClefMods
                                      .get(score.getTrackClef(i % trackCount)))
                                           .get(pitch % 12)), n,
                                         noteDur, dotted, (false));
                            }
                        }
                    }
                }

                measures.put(iv, new Pair<>(rl,
                                            new Measure((i % trackCount),
                                                        counts[i
                                                               % trackCount])));

                ++counts[i % trackCount];

                scrollView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return editButton.isChecked();
                    }
                });

                //create onTouchListener for each ImageView
                iv.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        zv.setIsEnabled(!editButton.isChecked());
                        if (editButton.isChecked() && !player.running) {
                            boolean dotting = dotButton.isChecked();
                            boolean resting = restButton.isChecked();
                            @SuppressWarnings("SuspiciousMethodCalls")
                            RelativeLayout rl = Objects
                              .requireNonNull(measures.get(v)).first;
                            @SuppressWarnings("SuspiciousMethodCalls")
                            Measure m = Objects
                              .requireNonNull(measures.get(v)).second;
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

                            assert m != null;
                            imageX = snapToTime((imageX - horizontalStart),
                                                measureLength,
                                                (horizontalMax
                                                 - horizontalStart),
                                                score.getMeasure(
                                                  m.staff, m.number,
                                                  timeSignature));
                            imageY = snapToHeight(imageY, verticalMax,
                                                  verticalStart,
                                                  verticalOffset);

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
                                    duration = 96;
                                    break;
                                case QUARTER:
                                    duration = 48;
                                    break;
                                default:
                                    duration = 24;
                            }

                            if (dotting)
                                duration += duration >> 1;

                            if (gottenDur != 0 && gottenDur != duration) {
                                Pair<NoteDur, Boolean> durAndDot
                                  = numToDurAndDot(gottenDur);

                                actualNoteDur = durAndDot.first;
                                shouldBeDotted = durAndDot.second;
                            }

                            byte lP = 0;
                            if (lastTouchPointY != 0)
                                lP = posToPitch(lastTouchPointY);
                            byte p = posToPitch(imageY);

                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN: {
                                    v.getParent()
                                     .requestDisallowInterceptTouchEvent(
                                       (true));
                                    byte[] midiEvent = new byte[3];

                                    byte nominalPitch = (byte)(
                                      p + Objects.requireNonNull(
                                        keys.get(key))
                                                 .get(p % 12)
                                      + Objects.requireNonNull(
                                        clefMods.get(
                                          score.getTrackClef(
                                            m.staff)))
                                               .get(p % 12));

                                    midiEvent[0] = (byte)(0x90 | m.staff);
                                    midiEvent[1] =
                                      (byte)(nominalPitch + accidental);
                                    midiEvent[2] = 127;

                                    tempNote = new Note(
                                      resting ? Note.NoteType.REST
                                              : Note.NoteType.MELODIC,
                                      gottenDur == 0 ? duration
                                                     : gottenDur,
                                      resting ? (byte)0
                                              : (byte)(p + Objects
                                                .requireNonNull(
                                                  keys.get(
                                                    key))
                                                .get(p % 12)
                                                       + Objects
                                                         .requireNonNull(
                                                           clefMods.get(
                                                             score.getTrackClef(
                                                               m.staff)))
                                                         .get(p
                                                              % 12)),
                                      accidental, (byte)127);

                                    drawNote(rl, imageX, imageY,
                                             tempNote, actualNoteDur,
                                             shouldBeDotted, (false));

                                    // Send the MIDI event to the synthesizer.
                                    player.directWrite(midiEvent);
                                }
                                break;
                                case MotionEvent.ACTION_MOVE:
                                    if (lastTouchPointY != imageY
                                        || lastTouchPointX != imageX) {
                                        byte[] midiEvent = new byte[6];
                                        byte nominalPitch = (byte)(p + Objects
                                          .requireNonNull(keys.get(key))
                                          .get(p % 12) + Objects.requireNonNull(
                                          clefMods
                                            .get(score.getTrackClef(m.staff)))
                                                                .get(p % 12));

                                        midiEvent[0]
                                          = (byte)(0x80 | m.staff);
                                        midiEvent[1]
                                          = (byte)(lP + Objects
                                          .requireNonNull(keys.get(key))
                                          .get(lP % 12) + accidental + Objects
                                                     .requireNonNull(
                                                       clefMods.get(
                                                         score.getTrackClef(
                                                           m.staff)))
                                                     .get(p % 12));
                                        midiEvent[2] = 127;
                                        midiEvent[3]
                                          = (byte)(0x90 | m.staff);
                                        midiEvent[4]
                                          = (byte)(nominalPitch
                                                   + accidental);
                                        midiEvent[5] = 127;

                                        // Send the MIDI event to the
                                        // synthesizer.
                                        player.directWrite(midiEvent);

                                        tempNote.setPitch(nominalPitch);
                                        if (gottenDur != 0)
                                            tempNote.setDuration(gottenDur);

                                        drawNote(rl, imageX, imageY,
                                                 tempNote, actualNoteDur,
                                                 shouldBeDotted, (false));
                                    }
                                    break;
                                case MotionEvent.ACTION_UP: {
                                    byte[] midiEvent = new byte[3];

                                    midiEvent[0] = (byte)(0x80 | m.staff);
                                    midiEvent[1] = (byte)(p + Objects
                                      .requireNonNull(keys.get(key))
                                      .get(p % 12) + accidental + Objects
                                                            .requireNonNull(
                                                              clefMods.get(
                                                                score
                                                                  .getTrackClef(
                                                                    m.staff)))
                                                            .get(p % 12));
                                    midiEvent[2] = 127;

                                    // Send the MIDI event to the synthesizer.
                                    player.directWrite(midiEvent);

                                    score.addNote(
                                      m.staff,
                                      (m.number * 192 + imageX),
                                      tempNote);

                                    editHistory.push(
                                      new Edit(tempNote,
                                               (m.number * 192 + imageX),
                                               m.staff, Edit.EditType.ADD));

                                    accidental = 0;
                                    ImageView accImg
                                      = findViewById(R.id.accidentButton);
                                    accImg.setImageResource(R.drawable.natural);
                                }
                                v.getParent()
                                 .requestDisallowInterceptTouchEvent(false);
                                break;
                            }
                            lastTouchPointX = imageX;
                            lastTouchPointY = imageY;

                            //textView.setText("imageX: " + imageX + "
                            // imageY: " + imageY);

                       /* if(imageX < 0)
                            textView.setText("Touch: " + (int)event.getX());
                        else
                            textView.setText("imageX: " + imageX + " imageY:
                            " + imageY + " " + v.toString());*/

                            //textView.setText("sX:"+ scrollX + "sY:" +
                            // scrollY + " iX:" + imageX + "iY:" + imageY + "
                            // lX:" + localX + "lY:" + localY);
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

    public void drawStaff(ImageView iv) {
        //takes predetermined width and height dimensions from ImageViews and
        // converts to pixels
        float dipW = 206f;
//        Resources r = getResources();
//        float pxW = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//                                              dipW, r.getDisplayMetrics());
        float dipH = 130f;
//        float pxH = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//                                              dipH, r.getDisplayMetrics());

        //pretty sure it doesnt work tho haha
        bitmap = Bitmap.createBitmap((int)dipW, (int)dipH,
                                     Bitmap.Config.ARGB_8888); //working
        // variabes 206, 130
        canvas = new Canvas(bitmap);

        double adjustment = 1 / getApplicationContext().getResources()
                                                       .getDisplayMetrics()
          .density * 2.625;

        //use drawLine to draw the lines from a start point x,y to an end
        // point x,y
        float startPointY = (float)(32 * adjustment);
        for (int i = 0; i < 5; i++) { //5 lines printed
            canvas.drawLine((0), startPointY, horizontalMax, startPointY,
                            linePaint);
            startPointY += (float)(16 * adjustment);
        }

        //draw vertical lines
        canvas.drawLine((int)(2 * adjustment), (int)(32 * adjustment),
                        (int)(2 * adjustment),
                        (int)((startPointY - 16) * adjustment),
                        linePaint);
        canvas.drawLine((int)(207 * adjustment), (int)(32 * adjustment),
                        (int)(207 * adjustment),
                        (int)((startPointY - 16) * adjustment),
                        linePaint);

        //update imageViews bitmap
        iv.setImageBitmap(bitmap);
    }

    //Draws note on a given X and Y coordinate
    //Currently, it takes the old staff bar image coordinates and manually
    // converts them to xy coordinates that
    //align with the new bitmap staff bars
    public void drawNote(RelativeLayout rl, int x, int y, Note n,
                         NoteDur dur, boolean dotted, boolean positionFilled) {
        if (n.getNoteType() != Note.NoteType.REST) {
            double adjustment = 1 / getApplicationContext().getResources()
                                                           .getDisplayMetrics()
              .density * 2.625;
            int xActual = (x * 125 / 48);
            int yActual = y - 85;
            ImageView noteIv;
            ImageView accidentalImage;
            RelativeLayout.LayoutParams params;
            RelativeLayout.LayoutParams accidentalParams;

            if (n.getImageView() == null) {
                noteIv = new ImageView(getApplicationContext());
                params = new RelativeLayout.LayoutParams(xActual, yActual);
                n.setImageView(noteIv);
                n.getImageView().setLayoutParams(params);
                rl.addView(noteIv);
            } else {
                noteIv = n.getImageView();
                accidentalImage = n.getAccidentalImageView();

                n.hide();

                params = (RelativeLayout.LayoutParams)n.getImageView()
                                                       .getLayoutParams();
                rl.addView(noteIv);
            }

            params.leftMargin = (int)(xActual * adjustment);
            params.topMargin = (int)(yActual * adjustment);
            params.width = (int)(100 * adjustment);
            params.height = (int)(100 * adjustment);

            switch (dur) {
                case WHOLE:
                    noteIv.setImageResource(R.drawable.wholenote);
                    params.leftMargin += (int)(20 * adjustment);
                    params.topMargin += (int)(67 * adjustment);
                    params.height = (int)(40 * adjustment);
                    params.width = (int)(40 * adjustment);
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


            if (n.getAccidental() != 0) {
                accidentalImage = new ImageView(getApplicationContext());
                accidentalParams = new RelativeLayout.LayoutParams(xActual,
                                                                   yActual);
                n.setAccidentalImageView(accidentalImage);
                n.getAccidentalImageView()
                 .setLayoutParams(accidentalParams);
                rl.addView(accidentalImage);

                //TODO: adjust all of this and make it use resources

                accidentalParams.leftMargin = (int)(xActual * adjustment);
                accidentalParams.topMargin = (int)(yActual * adjustment);
                accidentalParams.width = (int)(100 * adjustment);
                accidentalParams.height = (int)(100 * adjustment);
            }
        }

        /*Bitmap previousBitmap = ((BitmapDrawable) iv.getDrawable())
        .getBitmap();
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
                           ArrayList<Pair<Integer, LinkedList<Note>>> points) {
        int prevPoint;
        int nextPoint = points.get(0).first * toWidth / fromWidth;

        //if not in range, snap to nearest edge
        if (touchPoint <= points.get(0).first)
            return points.get(0).first;
        else if (touchPoint
                 >= points.get(points.size() - 1).first * toWidth / fromWidth)
            return points.get(points.size() - 1).first;
        else {
            for (int i = 1; i < points.size(); i++) {
                prevPoint = nextPoint;
                nextPoint = points.get(i).first * toWidth / fromWidth;

                //find specific range
                if (touchPoint >= prevPoint && touchPoint <= nextPoint) {
                    //compare distances
                    int leftDistance = touchPoint - prevPoint;
                    int rightDistance = nextPoint - touchPoint;

                    if (leftDistance < rightDistance)
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
        else if (touchPoint >= max)
            return max;
        else {
            touchPoint -= start;

            return start + ((touchPoint / offset)
                            + ((touchPoint % offset) * 2 / offset)) * offset;
        }
    }

    //BUTTONS---

    public void play(View view) {
        score.play();
    }

    public void pause(View view) {
        score.pause();
    }

    public void restart(View view) {
        score.resetPlayPos();
    }

    public void undo(View view) {
        Edit lastEdit = editHistory.pop();

        switch (lastEdit.editType) {
            case ADD:
                score.removeNote(lastEdit.staff, lastEdit.time,
                                 lastEdit.note.getPitch());
                break;
            case REMOVE:
                score.addNote(lastEdit.staff, lastEdit.time, lastEdit.note);
                break;
        }
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

        switch (accidental) {
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
        ActivityCompat.requestPermissions(MusicSheet.this, new String[]{
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          }, 100);

        LayoutInflater li = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View promptView = li.inflate(
          R.layout.save_prompt, (null));
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
          (this));

        alertDialogBuilder.setView(promptView);

        final EditText userInput = promptView.findViewById(
          R.id.editTextDialogUserInput);

        alertDialogBuilder
          .setCancelable(false)
          .setPositiveButton(("OK"),
                             new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(
                                   DialogInterface dialogInterface, int i) {
                                     score.save(userInput.getText().toString());
                                 }
                             })
          .setNegativeButton("Cancel",
                             new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(
                                   DialogInterface dialogInterface, int i) {
                                     dialogInterface.cancel();
                                 }
                             });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }
}
