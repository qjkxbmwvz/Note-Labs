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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Stack;

public class MusicSheet extends AppCompatActivity {
    final float STROKE_WIDTH = 2.0f;

    private ZoomView zoomView;

    // MIDI objects
    private Player player = new Player(this);
    private Score score;
    private Fraction timeSignature;

    private Paint linePaint;

    // Sparse Arrays
    private SparseIntArray pitchToPos;
    private SparseArray<ArrayList<Integer>> keys;
    private SparseArray<ArrayList<Integer>> reverseKeys;

    // Hash Maps
    private HashMap<Track.Clef, ArrayList<Integer>> clefMods;
    private HashMap<Track.Clef, ArrayList<Integer>> reverseClefMods;
    private HashMap<ImageView, Pair<RelativeLayout, Measure>> measures;

    // Note Values
    enum NoteDur {
        WHOLE, HALF, QUARTER, EIGHTH
    }

    private NoteDur selectedNoteDur;
    private Note tempNote;
    private Stack<Edit> editHistory; //holds most recently placed notes

    int key;
    int horizontalStart = 60;
    int horizontalMax = 540;
    int verticalStart = 19;
    int verticalOffset = 21;
    int verticalMax = 334;
    int lastTouchPointX = 0, lastTouchPointY = 0;
    int measureLength;

    byte accidental = 0; // set default accidental to be "natural"

    boolean newScore;

    private ImageButton playButton;
    private ImageButton restartButton;
    private ImageButton undoButton;
    private ImageButton accidentButton;
    private ImageButton noteButton;
    private ImageButton saveButton;
    private ImageButton addStuffButton;

    private ToggleButton editButton;
    private ToggleButton dotButton;
    private ToggleButton restButton;

    private ScrollView scrollView;
    private TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_music_sheet);

        Context context = getApplicationContext();
        tableLayout = new TableLayout(context);
        scrollView = findViewById(R.id.musicSheetScroll);

        score = new Score(player);
        measures = new HashMap<>();
        editHistory = new Stack<>();

        zoomView = new ZoomView(context);
        editButton = findViewById(R.id.editButton);
        dotButton = findViewById(R.id.dotButton);
        restButton = findViewById(R.id.restButton);

        playButton = findViewById(R.id.playButton);
        restartButton = findViewById(R.id.restartButton);
        undoButton = findViewById(R.id.undoButton);
        addStuffButton = findViewById(R.id.addStuffButton);
        accidentButton = findViewById(R.id.accidentButton);
        noteButton = findViewById(R.id.noteButton);
        saveButton = findViewById(R.id.saveButton);

        cycleButtons(null);

        linePaint = setUpPaint(Color.BLACK, (true), Paint.Style.STROKE);

        selectedNoteDur = NoteDur.QUARTER;
        key = 0;

        setUpPitchToPos();
        setUpKeys();
        setUpClefMods();

        scrollView.addView(zoomView);
        tableLayout.setLayoutParams(new LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.MATCH_PARENT,
          LinearLayout.LayoutParams.MATCH_PARENT));

        zoomView.addView(tableLayout);

        tempNote = null;

        Bundle extras = getIntent().getExtras();

        newScore = true;
        if (extras != null) {
            score.load(extras.getString("filename"));
            newScore = false;
        }

        timeSignature = new Fraction(4, 4);

        measureLength = 192 * timeSignature.num / timeSignature.den;

        int trackCount = 1;
        int measureCount = 4;

        if (!newScore) {
            trackCount = score.getTrackCount();
            measureCount = score.getMeasureCount();
        } else
            for (int i = 0; i < measureCount; ++i)
                score.addMeasure(timeSignature);

        for (int i = 0; i < trackCount; ++i) {
            if (newScore) {
                score.addTrack(new Track((byte)0, Track.Clef.TREBLE));
                for (int j = 0; j < score.getMeasureCount(); ++j) {
                    score.addNote(i, (measureLength * j), new Note(
                      Note.NoteType.REST, measureLength,
                      (byte)0, (byte)0, (byte)0));
                }
            }
            addStaff(i, measureCount);
        }
    }

    void addStaff(int staffNum, int measureCount) {
        int count = -1;
        boolean odd = (measureCount + 1) % 2 != 0;
        int height = measureCount / 2 + (odd ? 1 : 0);
        Context context = getApplicationContext();

        for (int i = 0; i < height; ++i) {
            //for each row
            TableRow tableRow = new TableRow(context);

            tableRow.setLayoutParams(new TableRow.LayoutParams(
              TableRow.LayoutParams.MATCH_PARENT));

            for (int j = 0; j < (odd && i == height - 1 ? 1 : 2); ++j)
                addMeasure(staffNum, count++, tableRow);

            tableLayout.addView(tableRow, (staffNum + i * (staffNum + 1)));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addMeasure(int staffNum, int count, TableRow tableRow) {
        Context context = getApplicationContext();
        TableRow.LayoutParams layoutParams
          = new TableRow.LayoutParams();

        RelativeLayout relativeLayout = new RelativeLayout(context);

        relativeLayout.setLayoutParams(layoutParams);
        relativeLayout.getLayoutParams().height = (int)(
          verticalMax * context.getResources()
                               .getDisplayMetrics().density / 2.625);
        relativeLayout.getLayoutParams().width = (int)(
          horizontalMax * context.getResources()
                                 .getDisplayMetrics().density / 2.625);

        ImageView imageView = new ImageView(context);

        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        drawStaff(imageView);

        if (count != -1 && count < score.getMeasureCount()) {
            ArrayList<Pair<Integer, LinkedList<Note>>> measure
              = score.getMeasure(staffNum, count, timeSignature);
            for (Pair<Integer, LinkedList<Note>> p : measure) {
                Pair<NoteDur, Boolean> durAndDot = numToDurAndDot(
                  p.second.getFirst().getDuration());

                NoteDur noteDur = durAndDot.first;
                boolean dotted = durAndDot.second;

                for (Note n : p.second) {
                    byte pitch = (byte)(n.getPitch()
                                        + Objects.requireNonNull(
                      reverseKeys.get(key)).get(n.getPitch() % 12));

                    drawNote(relativeLayout, p.first, pitchToPos.get(
                      pitch + Objects.requireNonNull(
                        reverseClefMods
                          .get(score.getTrackClef(staffNum)))
                                     .get(pitch % 12)),
                             n, noteDur, dotted, (false));
                }
            }
        }

        measures.put(imageView,
                     new Pair<>(relativeLayout, new Measure(staffNum,
                                                            count)));

        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return editButton.isChecked();
            }
        });

        //create onTouchListener for each ImageView
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                zoomView.setIsEnabled(!editButton.isChecked());
                if (editButton.isChecked() && !player.running) {
                    boolean dotting = dotButton.isChecked();
                    boolean resting = restButton.isChecked();
                    @SuppressWarnings("SuspiciousMethodCalls")
                    RelativeLayout rl = Objects
                      .requireNonNull(measures.get(v)).first;
                    @SuppressWarnings("SuspiciousMethodCalls") final Measure
                      measure = Objects
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

                    assert measure != null;

                    if (measure.number >= 0) {
                        imageX = snapToTime((imageX - horizontalStart),
                                            measureLength,
                                            (horizontalMax
                                             - horizontalStart),
                                            score.getMeasure(
                                              measure.staff,
                                              measure.number,
                                              timeSignature));
                        imageY = snapToHeight(imageY, verticalMax,
                                              verticalStart,
                                              verticalOffset);

//                            TextView debugText = findViewById(R.id.debugText);
//                            debugText
//                              .setText(imageX + ", " + imageY);

                        NoteDur actualNoteDur = selectedNoteDur;
                        int gottenDur = score.durationAtTime(
                          measure.staff,
                          (measure.number * measureLength + imageX));
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
                                 .requestDisallowInterceptTouchEvent((true));
                                byte[] midiEvent = new byte[3];

                                byte nominalPitch = (byte)(
                                  p + Objects.requireNonNull(
                                    keys.get(key))
                                             .get(p % 12)
                                  + Objects.requireNonNull(
                                    clefMods.get(
                                      score.getTrackClef(
                                        measure.staff)))
                                           .get(p % 12));

                                midiEvent[0] = (byte)(0x90
                                                      | measure.staff);
                                midiEvent[1] =
                                  (byte)(nominalPitch
                                         + accidental);
                                midiEvent[2] = 127;

                                // Send the MIDI event to the
                                // synthesizer.
                                player.directWrite(midiEvent);

                                tempNote = new Note(
                                  resting ? Note.NoteType.REST
                                          : Note.NoteType.MELODIC,
                                  gottenDur == 0 ? duration
                                                 : gottenDur,
                                  resting ? (byte)0
                                          : (byte)(
                                            p + Objects
                                              .requireNonNull(
                                                keys.get(
                                                  key))
                                              .get(p
                                                   % 12)
                                            + Objects
                                              .requireNonNull(
                                                clefMods.get(
                                                  score.getTrackClef(
                                                    measure.staff)))
                                              .get(p
                                                   % 12)),
                                  accidental, (byte)127);

                                drawNote(rl, imageX, imageY,
                                         tempNote, actualNoteDur,
                                         shouldBeDotted, (false));
                            }
                            break;
                            case MotionEvent.ACTION_MOVE:
                                if (lastTouchPointY != imageY
                                    || lastTouchPointX != imageX) {
                                    byte[] midiEvent = new byte[6];
                                    byte nominalPitch
                                      = (byte)(
                                      p
                                      + Objects
                                        .requireNonNull(
                                          keys.get(key)).get(p % 12)
                                      + Objects.requireNonNull(
                                        clefMods.get(
                                          score.getTrackClef(
                                            measure.staff)))
                                               .get(p % 12));

                                    midiEvent[0]
                                      = (byte)(0x80 | measure.staff);
                                    midiEvent[1]
                                      = (byte)(lP + Objects
                                      .requireNonNull(
                                        keys.get(key))
                                      .get(lP % 12) + accidental
                                               + Objects
                                                 .requireNonNull(
                                                   clefMods.get(
                                                     score.getTrackClef(
                                                       measure.staff)))
                                                 .get(lP % 12));
                                    midiEvent[2] = 127;
                                    midiEvent[3]
                                      = (byte)(0x90 | measure.staff);
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

                                midiEvent[0]
                                  = (byte)(0x80 | measure.staff);
                                midiEvent[1]
                                  = (byte)(p + Objects
                                  .requireNonNull(keys.get(key)).get(p % 12)
                                           + accidental + Objects
                                             .requireNonNull(
                                               clefMods.get(score
                                                              .getTrackClef(
                                                                measure.staff)))
                                             .get(p % 12));
                                midiEvent[2] = 127;

                                // Send the MIDI event to the
                                // synthesizer.
                                player.directWrite(midiEvent);

                                score.addNote(measure.staff,
                                              (measure.number * measureLength
                                               + imageX), tempNote);

                                editHistory.push(new Edit(tempNote,
                                                          (measure.number
                                                           * measureLength
                                                           + imageX),
                                                          measure.staff,
                                                          Edit.EditType.ADD));

                                accidental = 0;
                                ImageView accImg = findViewById(
                                  R.id.accidentButton);

                                accImg.setImageResource(R.drawable.natural);

                                v.getParent()
                                 .requestDisallowInterceptTouchEvent((false));
                                break;
                            }
                            case MotionEvent.ACTION_CANCEL:
                                if (tempNote != null)
                                    tempNote.hide();
                                break;
                        }
                        lastTouchPointX = imageX;
                        lastTouchPointY = imageY;
                    } else { // Measure -1 is for clefs and signatures.
                        if (event.getAction()
                            == MotionEvent.ACTION_DOWN) {
                            LayoutInflater li
                              = LayoutInflater.from(MusicSheet.this);
                            View promptView = li.inflate(
                              R.layout.instrument_prompt, (null));
                            AlertDialog.Builder alertDialogBuilder
                              = new AlertDialog.Builder(
                              (MusicSheet.this));

                            alertDialogBuilder.setView(promptView);

                            Spinner instrumentSpinner = promptView
                              .findViewById(R.id.instrument_spinner);

                            byte instrument = score
                              .getTrackInstrument(measure.staff);

                            instrumentSpinner.setSelection(instrument);

                            instrumentSpinner.setOnItemSelectedListener(
                              new AdapterView.OnItemSelectedListener() {
                                  @Override
                                  public void onItemSelected(
                                    AdapterView<?> parent, View view,
                                    int position, long id) {
                                      score
                                        .setTrackInstrument(
                                          measure.staff,
                                          (byte)position);
                                  }

                                  @Override
                                  public void onNothingSelected(
                                    AdapterView<?> parent) {}
                              });

                            instrumentSpinner.setSelection(
                              score.getTrackInstrument(measure.staff));


                            Spinner clefSpinner = promptView
                              .findViewById(R.id.clef_spinner);

                            clefSpinner.setSelection(
                              score.getTrackClef(measure.staff)
                                   .ordinal());

                            clefSpinner.setOnItemSelectedListener(
                              new AdapterView.OnItemSelectedListener() {
                                  @Override
                                  public void onItemSelected(
                                    AdapterView<?> parent, View view,
                                    int position, long id) {
                                      score.setTrackClef(
                                        measure.staff,
                                        Track.Clef
                                          .values()[position]);
                                  }

                                  @Override
                                  public void onNothingSelected(
                                    AdapterView<?> parent) {}
                              });

                            clefSpinner.setSelection(
                              score.getTrackClef(measure.staff)
                                   .ordinal());


                            alertDialogBuilder.setPositiveButton(
                              ("Done"),
                              new DialogInterface.OnClickListener() {
                                  @Override
                                  public void onClick(
                                    DialogInterface dialogInterface,
                                    int i) {
                                      dialogInterface.dismiss();
                                  }
                              });

                            AlertDialog alertDialog = alertDialogBuilder
                              .create();

                            alertDialog.show();
                        }
                    }
                }
                return true;
            }
        });
        relativeLayout.addView(imageView);
        tableRow.addView(relativeLayout);
    }

    // Hard codes values into data structure, mathematical approach produces
    // wrong values.
    private void setUpPitchToPos() {
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

    private void setUpKeys() {
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

    private void setUpClefMods() {
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

    private Paint setUpPaint(int color, boolean isAntialiased,
                             Paint.Style style) {
        Paint p = new Paint();

        p.setColor(color);
        p.setAntiAlias(isAntialiased);
        p.setStyle(style);
        p.setStrokeWidth(STROKE_WIDTH);

        return p;
    }

    private Pair<NoteDur, Boolean> numToDurAndDot(int num) {
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

    // This happens to have its rounding errors in all the right places.
    byte posToPitch(int pos) {
        return (byte)(((verticalMax - pos) / 21 + 27) * 12 / 7 + 11);
    }

    private void drawStaff(ImageView iv) {
        double adjustment =
          getApplicationContext().getResources()
                                 .getDisplayMetrics().density
          / 2.625;

        float dipW = (float)(horizontalMax * adjustment);
        float dipH = (float)(verticalMax * adjustment);

        // Drawing Variables
        Bitmap bitmap = Bitmap.createBitmap((int)dipW, (int)dipH,
                                            Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        //use drawLine to draw the lines from a start point x,y to an end
        // point x,y
        float vDiff = (float)(8.0 / 65.0 * verticalMax);

        float startPointY = 2 * vDiff;
        for (int i = 0; i < 5; i++) { //5 lines printed
            canvas.drawLine((0), (int)(startPointY * adjustment),
                            (int)((horizontalMax - 1) * adjustment),
                            (int)(startPointY * adjustment), linePaint);
            startPointY += vDiff;
        }

        //draw vertical lines
        canvas.drawLine((0), (int)(2 * vDiff * adjustment), (0),
                        (int)((startPointY - vDiff) * adjustment), linePaint);
        canvas.drawLine((int)((horizontalMax - 1) * adjustment),
                        (int)(2 * vDiff * adjustment),
                        (int)((horizontalMax - 1) * adjustment),
                        (int)((startPointY - vDiff) * adjustment),
                        linePaint);

        //update imageViews bitmap
        iv.setImageBitmap(bitmap);
    }

    // TODO: replace positionFilled with a TimePosition for
    // stem sharing and add an accidental bias for matching
    // and contrasting with the key and recent notes
    private void drawNote(RelativeLayout rl, int x, int y, Note n,
                          NoteDur dur, boolean dotted, boolean positionFilled) {
        double adjustment =
          getApplicationContext().getResources()
                                 .getDisplayMetrics().density
          / 2.625;

        int xActual = (x * 125 / 48);
        int yActual = y - 85;
        ImageView noteIv;
        ImageView accidentalImage;
        ImageView dottedImage;

        RelativeLayout.LayoutParams params;

        RelativeLayout.LayoutParams accidentalParams;

        RelativeLayout.LayoutParams dottedParams;

        noteIv = n.getImageView();
        if (noteIv == null) {
            noteIv = new ImageView(getApplicationContext());
            params = new RelativeLayout.LayoutParams(xActual, yActual);
            n.setImageView(noteIv);
            n.getImageView().setLayoutParams(params);
        } else {
            n.hide();
            params = (RelativeLayout.LayoutParams)noteIv.getLayoutParams();
        }
        rl.addView(noteIv);

        switch (n.getNoteType()) {
            case MELODIC:
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
                    accidentalImage = n.getAccidentalImageView();
                    if (accidentalImage == null) {
                        accidentalImage = new ImageView(
                          getApplicationContext());
                        accidentalParams = new RelativeLayout.LayoutParams(
                          xActual, yActual);
                        n.setAccidentalImageView(accidentalImage);
                        n.getAccidentalImageView()
                         .setLayoutParams(accidentalParams);
                    } else {
                        accidentalParams
                          = (RelativeLayout.LayoutParams)accidentalImage
                          .getLayoutParams();
                    }
                    rl.addView(accidentalImage);

                    //TODO: adjust all of this and make it use resources

                    accidentalParams.leftMargin = (int)((xActual - 30)
                                                        * adjustment);
                    accidentalParams.topMargin = (int)((yActual + 54)
                                                       * adjustment);
                    accidentalParams.width = (int)(55 * adjustment);
                    accidentalParams.height = (int)(55 * adjustment);

                    switch (n.getAccidental()) {
                        // TODO: adjust by key so that, for
                        // instance, a +1 accidental on a Bb
                        // in F Major is rendered as a natural
                        case -1:
                            accidentalImage.setImageResource(R.drawable.flat);
                            break;
                        case 1:
                            accidentalImage.setImageResource(R.drawable.sharp);
                    }
                }
                break;
            case REST:
                // TODO: add rest rendering
                yActual = 81; // probably correct

                break;
        }
        // TODO: add dot rendering for both notes and rests
        if (dotted) {
            dottedImage = n.getDotImageView();
            if (dottedImage == null) {
                dottedImage = new ImageView(getApplicationContext());
                dottedParams = new RelativeLayout.LayoutParams(xActual,
                                                               yActual);
                n.setDotImageView(dottedImage);
                n.getDotImageView().setLayoutParams(dottedParams);
            } else
                dottedParams = (RelativeLayout.LayoutParams)dottedImage
                  .getLayoutParams();

            rl.addView(dottedImage);

            dottedParams.leftMargin = (int)((xActual + 45)
                                            * adjustment);
            dottedParams.topMargin = (int)((yActual + 54) * adjustment);
            dottedParams.width = (int)(45 * adjustment);
            dottedParams.height = (int)(45 * adjustment);

            //TODO: find dot image and set it here
            dottedImage.setImageResource(R.drawable.saveicon);
        }
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

    //Returns nearest point relative to where the user touched
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

    //Buttons
    public void play(View view) {
        ImageView image = findViewById(R.id.playButton);
        //redundant
        if (!player.running) {
            score.play();
            image.setImageResource(R.drawable.ic_media_pause);
        } else {
            score.pause();
            image.setImageResource(R.drawable.ic_media_play);
        }
    }

    public void resetPlayButton() {
        ImageView image = findViewById(R.id.playButton);

        image.setImageResource(R.drawable.ic_media_play);
    }

    public void restart(View view) {
        if (!player.running) {
            score.resetPlayPos();
            ImageView image = findViewById(R.id.playButton);

            image.setImageResource(R.drawable.ic_media_play);
        }
    }

    public void undo(View view) {
        if (!editHistory.empty()) {
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
    }

    public void cycleNoteType(View view) {
        // TODO: make the rest toggle button display
        // a rest matching the chosen duration
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
        }
    }

    public void cycleButtons(View view) {
        ToggleButton editButton = findViewById(R.id.editButton);

        if (editButton.isChecked()) {
            playButton.setVisibility(View.GONE);
            restartButton.setVisibility(View.GONE);
            undoButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            addStuffButton.setVisibility(View.VISIBLE);
            accidentButton.setVisibility(View.VISIBLE);
            noteButton.setVisibility(View.VISIBLE);
            dotButton.setVisibility(View.VISIBLE);
            restButton.setVisibility(View.VISIBLE);
        } else {
            playButton.setVisibility(View.VISIBLE);
            restartButton.setVisibility(View.VISIBLE);
            undoButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            addStuffButton.setVisibility(View.GONE);
            accidentButton.setVisibility(View.GONE);
            noteButton.setVisibility(View.GONE);
            dotButton.setVisibility(View.GONE);
            restButton.setVisibility(View.GONE);
        }
    }

    public void addStuff(View view) {
        LayoutInflater li = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View promptView = li.inflate(
          R.layout.stuff_prompt, (null));
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
          (this));

        alertDialogBuilder.setView(promptView);

        Button addInstrumentButton = promptView
          .findViewById(R.id.addInstrumentButton);
        Button addMeasuresButton = promptView
          .findViewById(R.id.addMeasuresButton);

        addInstrumentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Track track = new Track((byte)0, Track.Clef.TREBLE);

                score.addTrack(track);

                for (int i = 0; i < score.getMeasureCount(); ++i)
                    score
                      .addNote((score.getTrackCount() - 1), (measureLength * i),
                               new Note(Note.NoteType.REST, measureLength,
                                        (byte)0, (byte)0, (byte)0));

                LayoutInflater li = LayoutInflater.from(MusicSheet.this);
                View promptView = li
                  .inflate(R.layout.instrument_prompt, (null));
                AlertDialog.Builder alertDialogBuilder
                  = new AlertDialog.Builder((MusicSheet.this));

                alertDialogBuilder.setView(promptView);

                Spinner instrumentSpinner = promptView
                  .findViewById(R.id.instrument_spinner);

                instrumentSpinner.setOnItemSelectedListener(
                  new AdapterView.OnItemSelectedListener() {
                      @Override
                      public void onItemSelected(AdapterView<?> parent,
                                                 View view, int position,
                                                 long id) {
                          score.setTrackInstrument((score.getTrackCount() - 1),
                                                   (byte)position);
                      }

                      @Override
                      public void onNothingSelected(
                        AdapterView<?> parent) {}
                  });

                instrumentSpinner.setSelection(
                  score.getTrackInstrument(score.getTrackCount() - 1));

                Spinner clefSpinner = promptView
                  .findViewById(R.id.clef_spinner);

                clefSpinner.setOnItemSelectedListener(
                  new AdapterView.OnItemSelectedListener() {
                      @Override
                      public void onItemSelected(AdapterView<?> parent,
                                                 View view, int position,
                                                 long id) {
                          score.setTrackClef((score.getTrackCount() - 1),
                                             Track.Clef.values()[position]);
                      }

                      @Override
                      public void onNothingSelected(
                        AdapterView<?> parent) {}
                  });

                clefSpinner.setSelection(
                  score.getTrackClef(score.getTrackCount() - 1).ordinal());

                addStaff((score.getTrackCount() - 1), score.getMeasureCount());


                alertDialogBuilder.setPositiveButton(
                  ("Done"),
                  new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialogInterface,
                                          int i) { dialogInterface.dismiss(); }
                  });

                AlertDialog alertDialog = alertDialogBuilder.create();

                alertDialog.show();
            }
        });

        addMeasuresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater li = LayoutInflater.from(MusicSheet.this);
                View promptView = li
                  .inflate(R.layout.up_down, (null));
                AlertDialog.Builder alertDialogBuilder
                  = new AlertDialog.Builder((MusicSheet.this));

                alertDialogBuilder.setView(promptView);

                Button upButton = promptView.findViewById(R.id.up_button);
                Button downButton = promptView.findViewById(R.id.down_button);
                final TextView addCount = promptView
                  .findViewById(R.id.add_count);

                upButton.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View view) {
                        int temp = Integer
                          .parseInt(addCount.getText().toString());
                        ++temp;
                        addCount.setText(Integer.toString(temp));
                    }
                });

                downButton.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View view) {
                        int temp = Integer
                          .parseInt(addCount.getText().toString());
                        if (temp > 0) {
                            --temp;
                            addCount.setText(Integer.toString(temp));
                        }
                    }
                });

                alertDialogBuilder.setPositiveButton(
                  ("Done"),
                  new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialogInterface,
                                          int i) {
                          int toBeAdded = Integer
                            .parseInt(addCount.getText().toString());

                          for (int j = 0; j < toBeAdded; ++j) {
                              score.addMeasure(timeSignature);

                              for (int k = 0; k < score.getTrackCount(); ++k) {
                                  TableRow tableRow;

                                  if (toBeAdded != 1
                                      || score.getMeasureCount() % 2 == 0) {
                                      tableRow = new TableRow(
                                        getApplicationContext());
                                      tableLayout.addView(
                                        tableRow, (k + score.getTrackCount() * (
                                          (score.getMeasureCount() + 1) / 2)));
                                  } else {
                                      tableRow = (TableRow)tableLayout
                                        .getChildAt(
                                          (k + score.getTrackCount() *
                                               (score.getMeasureCount() / 2)));
                                  }
                                  addMeasure(k, (score.getMeasureCount() - 1),
                                             tableRow);
                              }
                          }

                          dialogInterface.dismiss();
                      }
                  });

                AlertDialog alertDialog = alertDialogBuilder.create();

                alertDialog.show();
            }
        });

        alertDialogBuilder
          .setCancelable(false)
          .setPositiveButton(("Done"),
                             new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(
                                   DialogInterface dialogInterface, int i) {
                                     dialogInterface.dismiss();
                                 }
                             });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
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
                                   DialogInterface dialogInterface,
                                   int i) {
                                     score.save(userInput.getText()
                                                         .toString());
                                 }
                             })
          .setNegativeButton("Cancel",
                             new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(
                                   DialogInterface dialogInterface,
                                   int i) {
                                     dialogInterface.cancel();
                                 }
                             });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }
}
