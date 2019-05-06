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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
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
    private ZoomView zoomView;

    // MIDI objects
    private Player player = new Player((this));
    private Score score;

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
    private Stack<Edit> editHistory;

    int key;
    int horizontalStart = 60;
    int horizontalMax = 540;
    int verticalStart = 19;
    int verticalOffset = 21;
    int verticalMax = 334;
    int lastTouchPointX = 0, lastTouchPointY = 0;

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
    Fraction timeSignature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_music_sheet);

        Context context = getApplicationContext();
        tableLayout = new TableLayout(context);
        scrollView = findViewById(R.id.music_sheet_scroll);

        score = new Score(player);
        measures = new HashMap<>();
        editHistory = new Stack<>();

        zoomView = new ZoomView(context);
        editButton = findViewById(R.id.edit_button);
        dotButton = findViewById(R.id.dot_button);
        restButton = findViewById(R.id.rest_button);

        playButton = findViewById(R.id.play_button);
        restartButton = findViewById(R.id.restart_button);
        undoButton = findViewById(R.id.undo_button);
        addStuffButton = findViewById(R.id.add_stuff_button);
        accidentButton = findViewById(R.id.accident_button);
        noteButton = findViewById(R.id.note_button);
        saveButton = findViewById(R.id.save_button);

        //No Longer Have to Cycle Buttons, but Edit Button must keep its ability to toggle Zoom
        //cycleButtons(null);

        linePaint = setUpPaint();

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

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        newScore = true;
        if (extras != null) {
            score.load(extras.getString("filename"));
            newScore = false;
            Fraction ts = score.getTimeSignature();
            timeSignature = new Fraction(ts.num, ts.den);
        } else {
            timeSignature = new Fraction(4, 4);
            score.setTimeSignature(timeSignature);
        }

        int trackCount = 1;
        int measureCount = 4;

        if (!newScore) {
            trackCount = score.getTrackCount();
            measureCount = score.getMeasureCount();
        } else
            for (int i = 0; i < measureCount; ++i)
                score.addMeasure();

        for (int i = 0; i < trackCount; ++i) {
            if (newScore)
                score.addTrack(new Track((byte)0, Track.Clef.TREBLE, key, (0)));
            addStaff(i, measureCount);
        }
    }

    void addStaff(int staffNum, int measureCount) {
        int count = -1;
        boolean odd = (measureCount + 1) % 2 != 0;
        int height = measureCount / 2 + 1;
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

    private void drawMeasure(int staffNum, int count,
                             RelativeLayout relativeLayout) {
        ArrayList<Pair<Integer, LinkedList<Note>>> measure = score
          .getMeasure(staffNum, count);
        for (Pair<Integer, LinkedList<Note>> p : measure) {
            Pair<NoteDur, Boolean> durAndDot = numToDurAndDot(
              p.second.getFirst().getDuration());

            NoteDur noteDur = durAndDot.first;
            boolean dotted = durAndDot.second;

            for (Note n : p.second) {
                byte pitch = (byte)(n.getPitch() + Objects
                  .requireNonNull(
                    reverseKeys.get(score.getTrack(staffNum).getKey()))
                  .get(n.getPitch() % 12));
                drawNote(relativeLayout,
                         new XYCoord(p.first, pitchToPos.get(
                           pitch + reverseKeys
                             .get(trackWorkingKey(score.getTrack(staffNum)))
                             .get(pitch % 12) + Objects
                             .requireNonNull(reverseClefMods.get(
                               score.getTrackClef(staffNum)))
                             .get(pitch % 12))), n, noteDur, dotted,
                         score.getTrack(staffNum).getKey(), measure);
            }
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

        final ImageView imageView = new ImageView(context);

        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        drawStaff(imageView);

        if (count == -1)
            drawStaffHead(relativeLayout, score.getTrack(staffNum));
        else if (count < score.getMeasureCount())
            drawMeasure(staffNum, count, relativeLayout);

        measures.put(imageView,
                     new Pair<>(relativeLayout, new Measure(staffNum, count)));

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
                      measure = Objects.requireNonNull(measures.get(v)).second;

                    int imageX = (int)(
                      event.getX() / getApplicationContext().getResources()
                                                            .getDisplayMetrics()
                        .density * 2.625);
                    int imageY = (int)(
                      event.getY() / getApplicationContext().getResources()
                                                            .getDisplayMetrics()
                        .density * 2.625);

                    assert measure != null;

                    if (measure.number >= 0) {
                        imageX = snapToTime((imageX - horizontalStart),
                                            score.getMeasureLength(),
                                            (horizontalMax
                                             - horizontalStart),
                                            score.getMeasure(
                                              measure.staff,
                                              measure.number));
                        imageY = snapToHeight(imageY, verticalMax,
                                              verticalStart,
                                              verticalOffset);

                        NoteDur actualNoteDur = selectedNoteDur;
                        int gottenDur = score.durationAtTime(
                          measure.staff,
                          (measure.number * score.getMeasureLength() + imageX));
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
                                byte nominalPitch = (byte)(
                                  p + keys.get(
                                    trackWorkingKey(
                                      score.getTrack(measure.staff)))
                                          .get(p % 12) + Objects.requireNonNull(
                                    clefMods.get(score.getTrackClef(
                                      measure.staff))).get(p % 12));

                                player
                                  .directWrite((byte)(0x90 | measure.staff),
                                               (byte)(nominalPitch
                                                      + accidental + score
                                                        .getTrack(measure.staff)
                                                        .getTransposition()),
                                               (byte)127);

                                tempNote = new Note(
                                  resting ? Note.NoteType.REST
                                          : Note.NoteType.MELODIC,
                                  gottenDur == 0 ? duration : gottenDur,
                                  resting ? 0 : nominalPitch,
                                  accidental, (byte)127);

                                drawNote(rl, new XYCoord(imageX, imageY),
                                         tempNote, actualNoteDur,
                                         shouldBeDotted,
                                         score.getTrack(measure.staff).getKey(),
                                         score.getMeasure(measure.staff,
                                                          measure.number));
                            }
                            break;
                            case MotionEvent.ACTION_MOVE:
                                if (lastTouchPointY != imageY
                                    || lastTouchPointX != imageX) {
                                    byte nominalPitch = (byte)(
                                      p + keys.get(
                                        trackWorkingKey(
                                          score.getTrack(measure.staff)))
                                              .get(p % 12)
                                      + Objects.requireNonNull(
                                        clefMods.get(score.getTrackClef(
                                          measure.staff))).get(p % 12));
                                    byte oldNominalPitch = (byte)(
                                      lP + keys.get(
                                        trackWorkingKey(
                                          score.getTrack(measure.staff)))
                                               .get(p % 12) + Objects
                                        .requireNonNull(
                                          clefMods.get(score.getTrackClef(
                                            measure.staff))).get(p % 12));

                                    player.directWrite(
                                      (byte)(0x80 | measure.staff),
                                      (byte)(oldNominalPitch + accidental
                                             + score
                                               .getTrack(measure.staff)
                                               .getTransposition()),
                                      (byte)127, (byte)(0x90 | measure.staff),
                                      (byte)(nominalPitch + accidental + score
                                        .getTrack(measure.staff)
                                        .getTransposition()),
                                      (byte)127);

                                    tempNote.setPitch(nominalPitch);
                                    if (gottenDur != 0)
                                        tempNote.setDuration(gottenDur);

                                    drawNote(rl, new XYCoord(imageX, imageY),
                                             tempNote, actualNoteDur,
                                             shouldBeDotted,
                                             score.getTrack(measure.staff)
                                                  .getKey(), score
                                               .getMeasure(measure.staff,
                                                           measure.number));
                                }
                                break;
                            case MotionEvent.ACTION_UP: {
                                byte nominalPitch = (byte)(
                                  p + keys.get(
                                    trackWorkingKey(
                                      score.getTrack(measure.staff)))
                                          .get(p % 12) + Objects.requireNonNull(
                                    clefMods.get(score.getTrackClef(
                                      measure.staff))).get(p % 12));

                                player
                                  .directWrite((byte)(0x80 | measure.staff),
                                               (byte)(nominalPitch
                                                      + accidental + score
                                                        .getTrack(measure.staff)
                                                        .getTransposition()),
                                               (byte)127);

                                LinkedList<Note> rests = score
                                  .addNote(measure.staff,
                                           (measure.number * score
                                             .getMeasureLength()
                                            + imageX), tempNote);

                                int restTime = imageX + tempNote.getDuration();

                                if (rests != null && !rests.isEmpty())
                                    for (Note rest : rests) {
                                        Pair<NoteDur, Boolean> durAndDot
                                          = numToDurAndDot(rest.getDuration());
                                        NoteDur restDur = durAndDot.first;
                                        boolean restDotted = durAndDot.second;

                                        drawNote(rl, new XYCoord(restTime, (0)),
                                                 rest, restDur,
                                                 restDotted,
                                                 score.getTrack(measure.staff)
                                                      .getKey(), score
                                                   .getMeasure(measure.staff,
                                                               measure.number));
                                        restTime += rest.getDuration();
                                    }

                                editHistory.push(
                                  new Edit(tempNote,
                                           (measure.number * score
                                             .getMeasureLength()
                                            + imageX),
                                           measure.staff, Edit.EditType.ADD));

                                accidental = 0;

                                ImageView accImg = findViewById(
                                  R.id.accident_button);

                                accImg.setImageResource(R.drawable.natural);

                                v.getParent()
                                 .requestDisallowInterceptTouchEvent((false));
                                break;
                            }
                            case MotionEvent.ACTION_CANCEL:
                                byte nominalPitch = (byte)(
                                  p + Objects.requireNonNull(keys.get(
                                    score.getTrack(measure.staff).getKey()))
                                             .get(p % 12)
                                  + Objects.requireNonNull(clefMods.get(
                                    score.getTrackClef(measure.staff)))
                                           .get(p % 12));

                                player
                                  .directWrite((byte)(0x80 | measure.staff),
                                               (byte)(nominalPitch
                                                      + accidental + score
                                                        .getTrack(measure.staff)
                                                        .getTransposition()),
                                               (byte)127);

                                if (tempNote != null)
                                    tempNote.hide();
                                break;
                        }
                        lastTouchPointX = imageX;
                        lastTouchPointY = imageY;
                    } else { // Measure -1 is for clefs and signatures.
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            LayoutInflater li = LayoutInflater
                              .from(MusicSheet.this);
                            View promptView = li
                              .inflate(R.layout.instrument_prompt, (null));
                            AlertDialog.Builder alertDialogBuilder
                              = new AlertDialog.Builder(
                              (MusicSheet.this));

                            alertDialogBuilder.setView(promptView);

                            Spinner instrumentSpinner = promptView
                              .findViewById(R.id.instrument_spinner);

                            byte instrument = score
                              .getTrackInstrument(measure.staff);

                            instrumentSpinner.setSelection(instrument);

                            instrumentSpinner
                              .setOnItemSelectedListener(
                                new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(
                                      AdapterView<?> parent, View view,
                                      int position, long id) {
                                        score.setTrackInstrument(
                                          measure.staff, (byte)position);
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
                                        Track.Clef.values()[position]);
                                      drawStaffHead(
                                        Objects.requireNonNull(
                                          measures.get(imageView)).first,
                                        score.getTrack(measure.staff));
                                  }

                                  @Override
                                  public void onNothingSelected(
                                    AdapterView<?> parent) {}
                              });

                            clefSpinner.setSelection(
                              score.getTrackClef(measure.staff)
                                   .ordinal());

                            Spinner keySpinner = promptView
                              .findViewById(R.id.key_spinner);

                            keySpinner.setOnItemSelectedListener(
                              new AdapterView.OnItemSelectedListener() {
                                  @Override
                                  public void onItemSelected(
                                    AdapterView<?> adapterView,
                                    View view, int i, long l) {
                                      score.getTrack(measure.staff)
                                           .setKey(i - 7);

                                      drawStaffHead(
                                        Objects.requireNonNull(
                                          (RelativeLayout)imageView
                                            .getParent()),
                                        score
                                          .getTrack(measure.staff));
                                  }

                                  @Override
                                  public void onNothingSelected(
                                    AdapterView<?> adapterView) {}
                              });

                            keySpinner.setSelection(
                              score.getTrack(measure.staff).getKey() + 7);

                            final TextView transpositionText = promptView
                              .findViewById(R.id.transposition_text);

                            final SeekBar transpositionBar = promptView
                              .findViewById(R.id.transposition_bar);

                            transpositionText
                              .setText(getString(R.string.transposition_text,
                                                 score.getTrack(measure.staff)
                                                      .getTransposition()));
                            transpositionBar.setProgress(
                              score.getTrack(measure.staff).getTransposition());

                            transpositionBar.setOnSeekBarChangeListener(
                              new SeekBar.OnSeekBarChangeListener() {
                                  @Override
                                  public void onProgressChanged(SeekBar seekBar,
                                                                int i,
                                                                boolean b) {
                                      if (b)
                                          transpositionText.setText(
                                            getString(
                                              R.string.transposition_text, i));
                                  }

                                  @Override
                                  public void onStartTrackingTouch(
                                    SeekBar seekBar) {}

                                  @Override
                                  public void onStopTrackingTouch(
                                    SeekBar seekBar) {
                                      score.getTrack(measure.staff)
                                           .setTransposition(
                                             seekBar.getProgress());
                                      drawStaffHead(
                                        Objects.requireNonNull(
                                          measures.get(imageView)).first,
                                        score.getTrack(measure.staff));
                                  }
                              });

                            final TextView numeratorText = promptView
                              .findViewById(R.id.numerator_text);

                            final SeekBar numeratorBar = promptView
                              .findViewById(R.id.numerator_bar);

                            numeratorText
                              .setText(getString(R.string.numerator_text,
                                                 timeSignature.num));
                            numeratorBar.setProgress(timeSignature.num);

                            numeratorBar.setOnSeekBarChangeListener(
                              new SeekBar.OnSeekBarChangeListener() {
                                  @Override
                                  public void onProgressChanged(SeekBar seekBar,
                                                                int i,
                                                                boolean b) {
                                      if (b)
                                          numeratorText.setText(
                                            getString(
                                              R.string.numerator_text, i));
                                  }

                                  @Override
                                  public void onStartTrackingTouch(
                                    SeekBar seekBar) {}

                                  @Override
                                  public void onStopTrackingTouch(
                                    SeekBar seekBar) {
                                      timeSignature.num = seekBar.getProgress();
                                      drawStaffHead(
                                        Objects.requireNonNull(
                                          measures.get(imageView)).first,
                                        score.getTrack(measure.staff));
                                      int oldMeasureCount = score
                                        .getMeasureCount();
                                      score.setTimeSignature(timeSignature);
                                      //TODO: redraw entire score
                                      redrawScore(oldMeasureCount);
                                  }
                              });

                            final TextView denominatorText = promptView
                              .findViewById(R.id.denominator_text);

                            final SeekBar denominatorBar = promptView
                              .findViewById(R.id.denominator_bar);

                            denominatorText
                              .setText(getString(R.string.denominator_text,
                                                 timeSignature.den));
                            denominatorBar.setProgress(timeSignature.den);

                            denominatorBar.setOnSeekBarChangeListener(
                              new SeekBar.OnSeekBarChangeListener() {
                                  @Override
                                  public void onProgressChanged(SeekBar seekBar,
                                                                int i,
                                                                boolean b) {
                                      if (b)
                                          denominatorText.setText(
                                            getString(
                                              R.string.denominator_text, i));
                                  }

                                  @Override
                                  public void onStartTrackingTouch(
                                    SeekBar seekBar) {}

                                  @Override
                                  public void onStopTrackingTouch(
                                    SeekBar seekBar) {
                                      timeSignature.den = seekBar.getProgress();
                                      drawStaffHead(
                                        Objects.requireNonNull(
                                          measures.get(imageView)).first,
                                        score.getTrack(measure.staff));
                                      score.setTimeSignature(timeSignature);
                                      //TODO: redraw entire score
                                  }
                              });

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

    void redrawScore(int oldMeasureCount) {
        if (score.getMeasureCount() > oldMeasureCount) {
            for (int i = 0; i < oldMeasureCount; ++i) {
                for (int j = 0; j < score.getTrackCount(); ++j) {
                    int h = (i + 1) / 2;
                    drawMeasure(j, i,
                                (RelativeLayout)((TableRow)(
                                  tableLayout.getChildAt((h + j * (h + 1)))))
                                  .getVirtualChildAt(((i + 1) % 2)));
                }
            }
            while (oldMeasureCount < score.getMeasureCount()) {
                for (int i = 0; i < score.getTrackCount(); ++i) {
                    TableRow tr;
                    if ((oldMeasureCount + 1) % 2 == 0) {
                        tr = new TableRow(getApplicationContext());
                        tr.setLayoutParams(new TableRow.LayoutParams(
                          TableRow.LayoutParams.MATCH_PARENT));
                        tableLayout.addView(tr,
                                            (i
                                             + (oldMeasureCount + 1) / 2 * score
                                              .getTrackCount()));
                    } else {
                        tr = (TableRow)tableLayout
                          .getChildAt(
                            (i + (oldMeasureCount + 1) / 2 * score
                              .getTrackCount()));
                    }
                    addMeasure(i, oldMeasureCount++, tr);
                }
            }
        } else if (score.getMeasureCount() < oldMeasureCount) {
            for (int i = 0; i < score.getMeasureCount(); ++i) {
                for (int j = 0; j < score.getTrackCount(); ++j) {
                    int h = (i + 1) / 2;

                    drawMeasure(j, i,
                                (RelativeLayout)((TableRow)(
                                  tableLayout.getChildAt((h + j * (h + 1)))))
                                  .getVirtualChildAt(((i + 1) % 2)));
                }
            }
            while (oldMeasureCount > score.getMeasureCount()) {
                for (int i = score.getTrackCount() - 1; i >= 0; --i) {
                    //TODO: remove individual measures from rows.
                    int ind = (i + (oldMeasureCount + 1) / 2
                                   * score.getTrackCount());
                    if (oldMeasureCount % 2 == 0)
                        tableLayout.removeViewAt(ind);
                }
                --oldMeasureCount;
            }
        }
    }

    private int trackWorkingKey(Track track) {
        int workingKey = track.getKey();

        if (track.getTransposition() < 0)
            for (int i = 0; i > track.getTransposition() % 12; --i) {
                workingKey -= 5;
                if (workingKey < -7)
                    workingKey += 12;
            }
        else if (track.getTransposition() > 0)
            for (int i = 0; i < track.getTransposition() % 12; ++i) {
                workingKey += 5;
                if (workingKey > 7)
                    workingKey -= 12;
            }
        return workingKey;
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
        for (int i = -7; i <= 7; ++i) {
            keys.put(i, new ArrayList<Integer>((12)));
            reverseKeys.put(i, new ArrayList<Integer>((12)));
            for (int j = 0; j < 12; ++j) {
                Objects.requireNonNull(keys.get(i)).add(0);
                Objects.requireNonNull(reverseKeys.get(i)).add(0);
            }
        }
        for (int i = 1; i <= 7; ++i) {
            Objects.requireNonNull(keys.get(i)).set(5, 1);  // F#
            Objects.requireNonNull(reverseKeys.get(i)).set(6, -1);
        }
        for (int i = 2; i <= 7; ++i) {
            Objects.requireNonNull(keys.get(i)).set(0, 1);  // C#
            Objects.requireNonNull(reverseKeys.get(i)).set(1, -1);
        }
        for (int i = 3; i <= 7; ++i) {
            Objects.requireNonNull(keys.get(i)).set(7, 1);  // G#
            Objects.requireNonNull(reverseKeys.get(i)).set(8, -1);
        }
        for (int i = 4; i <= 7; ++i) {
            Objects.requireNonNull(keys.get(i)).set(2, 1);  // D#
            Objects.requireNonNull(reverseKeys.get(i)).set(3, -1);
        }
        for (int i = 5; i <= 7; ++i) {
            Objects.requireNonNull(keys.get(i)).set(9, 1);  // A#
            Objects.requireNonNull(reverseKeys.get(i)).set(10, -1);
        }
        for (int i = 6; i <= 7; ++i) {
            Objects.requireNonNull(keys.get(i)).set(4, 1);  // E#
            Objects.requireNonNull(reverseKeys.get(i)).set(5, -1);
        }
        Objects.requireNonNull(keys.get(7)).set(11, 1);  // B#
        Objects.requireNonNull(reverseKeys.get(7)).set(0, -1);

        for (int i = -1; i >= -7; --i) {
            Objects.requireNonNull(keys.get(i)).set(11, -1);  // Bb
            Objects.requireNonNull(reverseKeys.get(i)).set(10, 1);
        }
        for (int i = -2; i >= -7; --i) {
            Objects.requireNonNull(keys.get(i)).set(4, -1);  // Eb
            Objects.requireNonNull(reverseKeys.get(i)).set(3, 1);
        }
        for (int i = -3; i >= -7; --i) {
            Objects.requireNonNull(keys.get(i)).set(9, -1);  // Ab
            Objects.requireNonNull(reverseKeys.get(i)).set(8, 1);
        }
        for (int i = -4; i >= -7; --i) {
            Objects.requireNonNull(keys.get(i)).set(2, -1);  // Db
            Objects.requireNonNull(reverseKeys.get(i)).set(1, 1);
        }
        for (int i = -5; i >= -7; --i) {
            Objects.requireNonNull(keys.get(i)).set(7, -1);  // Gb
            Objects.requireNonNull(reverseKeys.get(i)).set(6, 1);
        }
        for (int i = -6; i >= -7; --i) {
            Objects.requireNonNull(keys.get(i)).set(0, -1);  // Cb
            Objects.requireNonNull(reverseKeys.get(i)).set(11, 1);
        }
        Objects.requireNonNull(keys.get(-7)).set(5, -1);  // Fb
        Objects.requireNonNull(reverseKeys.get(-7)).set(4, 1);
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

    private Paint setUpPaint() {
        final float STROKE_WIDTH = 2.0f;

        Paint p = new Paint();

        p.setColor(Color.BLACK);
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
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
            case 144:
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
                                 .getDisplayMetrics().density / 2.625;

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

    // TODO: rotate notes based on height, but make all notes in the
    // same TimePosition have the same orientation.  Also, draw eighth
    // notes as quarters unless they're the highest in the given
    // TimePosition (if rightside-up) or the lowest (if upside-down).
    private void drawNote(RelativeLayout rl, XYCoord xy, Note n,
                          NoteDur dur, boolean dotted, int key,
                          ArrayList<Pair<Integer, LinkedList<Note>>> measure) {
        double adjustment =
          getApplicationContext().getResources()
                                 .getDisplayMetrics().density / 2.625;

        int xActual = (xy.x * 500 / score.getMeasureLength()) + 30;
        int yActual = xy.y - 85;
        ImageView noteIv;
        ImageView accidentalImage;
        ImageView dottedImage;

        RelativeLayout.LayoutParams noteParams;

        RelativeLayout.LayoutParams accidentalParams;

        RelativeLayout.LayoutParams dotParams;

        noteIv = n.getImageView();
        if (noteIv == null) {
            noteIv = new ImageView(getApplicationContext());
            noteParams = new RelativeLayout.LayoutParams(xActual, yActual);
            n.setImageView(noteIv);
            n.getImageView().setLayoutParams(noteParams);
        } else {
            n.hide();
            noteParams = (RelativeLayout.LayoutParams)noteIv.getLayoutParams();
        }
        rl.addView(noteIv);

        switch (n.getNoteType()) {
            case MELODIC:
                noteParams.leftMargin = (int)(xActual * adjustment);
                noteParams.topMargin = (int)(yActual * adjustment);
                noteParams.width = (int)(100 * adjustment);
                noteParams.height = (int)(100 * adjustment);

                switch (dur) {
                    case WHOLE:
                        noteIv.setImageResource(R.drawable.whole_note);
                        noteParams.leftMargin += (int)(20 * adjustment);
                        noteParams.topMargin += (int)(67 * adjustment);
                        noteParams.height = (int)(40 * adjustment);
                        noteParams.width = (int)(40 * adjustment);
                        break;
                    case HALF:
                        noteIv.setImageResource(R.drawable.half_note);
                        break;
                    case QUARTER:
                        noteIv.setImageResource(R.drawable.quarter_note);
                        break;
                    case EIGHTH: {
                        for (int i = 0; i < measure.size(); ++i) {
                            if (measure.get(i).first == xy.x) {
                                if (
                                  measure.get(i).second.getFirst().getNoteType()
                                  != Note.NoteType.REST)
                                    noteIv.setImageResource(
                                      R.drawable.quarter_note);
                                else
                                    noteIv
                                      .setImageResource(R.drawable.eighth_note);
                                break;
                            }
                        }
                    }
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

                    switch (n.getAccidental() + keys.get(key).get(
                      posToPitch(xy.y) % 12)) {
                        case -2:
                            accidentalImage
                              .setImageResource(R.drawable.double_flat);
                            break;
                        case -1:
                            accidentalImage.setImageResource(R.drawable.flat);
                            break;
                        case 0:
                            accidentalImage
                              .setImageResource(R.drawable.natural);
                            break;
                        case 1:
                            accidentalImage.setImageResource(R.drawable.sharp);
                            break;
                        case 2:
                            accidentalImage
                              .setImageResource(R.drawable.double_sharp_temp);
                    }
                }
                break;
            case REST:
                yActual = 82;
                noteParams.leftMargin = (int)((xActual - 20) * adjustment);
                noteParams.topMargin = (int)(yActual * adjustment);
                noteParams.width = (int)(100 * adjustment);
                noteParams.height = (int)(100 * adjustment);

                switch (dur) {
                    case WHOLE:
                        noteIv.setImageResource(R.drawable.whole_rest);
                        break;
                    case HALF:
                        noteParams.topMargin = (int)((yActual + 23)
                                                     * adjustment);
                        noteIv.setImageResource(R.drawable.half_rest);
                        break;
                    case QUARTER:
                        noteParams.width = (int)(150 * adjustment);
                        noteParams.height = (int)(150 * adjustment);
                        noteIv.setImageResource(R.drawable.quarter_rest);
                        break;
                    case EIGHTH:
                        noteParams.topMargin = (int)((yActual + 22)
                                                     * adjustment);
                        noteParams.width = (int)(150 * adjustment);
                        noteParams.height = (int)(150 * adjustment);
                        noteIv.setImageResource(R.drawable.eighth_rest);
                }

        }

        if (dotted) {
            dottedImage = n.getDotImageView();
            if (dottedImage == null) {
                dottedImage = new ImageView(getApplicationContext());
                dotParams = new RelativeLayout.LayoutParams(xActual,
                                                            yActual);
                n.setDotImageView(dottedImage);
                n.getDotImageView().setLayoutParams(dotParams);
            } else
                dotParams = (RelativeLayout.LayoutParams)dottedImage
                  .getLayoutParams();

            rl.addView(dottedImage);

            dotParams.leftMargin = (int)((xActual + 100) * adjustment);
            dotParams.topMargin = (int)((yActual + 73) * adjustment);
            dotParams.width = (int)(35 * adjustment);
            dotParams.height = (int)(35 * adjustment);

            dottedImage.setImageResource(R.drawable.dot);
        }
    }

    protected void drawStaffHead(RelativeLayout rl, Track track) {
        double adjustment =
          getApplicationContext().getResources()
                                 .getDisplayMetrics().density / 2.625;

        ImageView clefIv = track.getClefImage();
        RelativeLayout.LayoutParams clefParams;

        if (clefIv == null) {
            clefIv = new ImageView(getApplicationContext());
            clefParams = new RelativeLayout.LayoutParams(
              (int)(100 * adjustment), (int)(100 * adjustment));
            track.setClefImage(clefIv);
            track.getClefImage().setLayoutParams(clefParams);
        } else {
            track.hideHead();
            clefParams = (RelativeLayout.LayoutParams)clefIv.getLayoutParams();
        }
        rl.addView(clefIv);

        clefParams.leftMargin = (int)(-60 * adjustment);
        clefParams.width = (int)(298 * adjustment);
        clefParams.height = (int)(298 * adjustment); //200

        switch (track.getClef()) {
            case TREBLE:
                clefParams.leftMargin = (int)(-90 * adjustment);
                clefParams.topMargin = (int)(18 * adjustment); //80
                clefIv.setImageResource(R.drawable.treble_clef);
                break;
            case ALTO:
                clefParams.topMargin = (int)(38 * adjustment);
                clefParams.width = (int)(250 * adjustment);
                clefParams.height = (int)(250 * adjustment);
                clefIv.setImageResource(R.drawable.alto_clef);
                break;
            case BASS:
                clefParams.leftMargin = (int)(-90 * adjustment);
                clefParams.topMargin = (int)(1 * adjustment);
                clefIv.setImageResource(R.drawable.bass_clef);
                break;
            case PERCUSSION:
                clefIv.setImageResource(R.drawable.percussion_clef);
        }

        ArrayList<ImageView> keySigImages = track.getKeySigImages();
        ArrayList<RelativeLayout.LayoutParams> keySigParamses
          = new ArrayList<>();

        if (!keySigImages.isEmpty())
            keySigImages.clear();

        int workingKey = trackWorkingKey(track);

        int accidentalCount = workingKey > 0 ? workingKey : -workingKey;
        boolean sharp = workingKey > 0;

        int[] heights = {82, 145, 61, 124, 187, 103, 166};

        for (int i = 0; i < accidentalCount; ++i) {
            keySigImages.add(new ImageView(getApplicationContext()));
            keySigParamses.add(
              new RelativeLayout.LayoutParams(
                (int)(100 * adjustment),
                (int)(100 * adjustment)));
            keySigParamses.get(i).leftMargin = 100 + i * 45;

            int height = sharp ? heights[i] : heights[6 - i];

            if (sharp)
                height -= 50;
            else
                height -= 74;

            if (track.getClef() == Track.Clef.ALTO)
                height += 21;
            else if (track.getClef() == Track.Clef.BASS)
                height += 42;

            keySigParamses.get(i).topMargin = (int)(height * adjustment);
            keySigImages.get(i).setLayoutParams(keySigParamses.get(i));
            keySigImages.get(i).setImageResource(sharp ? R.drawable.sharp
                                                       : R.drawable.flat);
            rl.addView(keySigImages.get(i));
        }

        ImageView numImage = track.getNumImage();
        ImageView denImage = track.getDenImage();

        RelativeLayout.LayoutParams numParams;
        RelativeLayout.LayoutParams denParams;

        //TODO: adjust the numbers' sizes and positions
        if (numImage == null) {
            numImage = new ImageView(getApplicationContext());
            numParams = new RelativeLayout.LayoutParams(
              (int)(800 * adjustment), (int)(800 * adjustment));
            track.setNumImage(numImage);
            track.getNumImage().setLayoutParams(numParams);
        } else
            numParams = (RelativeLayout.LayoutParams)numImage.getLayoutParams();
        if (denImage == null) {
            denImage = new ImageView(getApplicationContext());
            denParams = new RelativeLayout.LayoutParams(
              (int)(2000 * adjustment), (int)(2000 * adjustment));
            track.setDenImage(denImage);
            track.getDenImage().setLayoutParams(denParams);
        } else
            denParams = (RelativeLayout.LayoutParams)denImage.getLayoutParams();

        rl.addView(numImage);
        rl.addView(denImage);

        numParams.leftMargin = (int)(300 * adjustment);
        denParams.leftMargin = (int)(300 * adjustment);
        numParams.topMargin = (int)(-70 * adjustment);
        denParams.topMargin = (int)(70 * adjustment);

        numImage.setImageResource(timeSignatureResource(timeSignature.num));
        denImage.setImageResource(timeSignatureResource(timeSignature.den));
    }

    int timeSignatureResource(int i) {
        switch (i) {
            case 1:
                return R.drawable.time_signature_1;
            case 2:
                return R.drawable.time_signature_2;
            case 3:
                return R.drawable.time_signature_3;
            case 4:
                return R.drawable.time_signature_4;
            case 5:
                return R.drawable.time_signature_5;
            case 6:
                return R.drawable.time_signature_6;
            case 7:
                return R.drawable.time_signature_7;
            case 8:
                return R.drawable.time_signature_8;
            default:
                return 0;
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
        ImageView image = findViewById(R.id.play_button);
        //redundant
        if (!player.running) {
            score.play();
            image.setImageResource(R.drawable.ic_media_pause);
        } else {
            score.pause();
            image.setImageResource(R.drawable.play);
        }
    }

    public void resetPlayButton() {
        final ImageView image = findViewById(R.id.play_button); //need final?

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                image.setImageResource(
                  R.drawable.play); //crash fixed; image updates BG thread,
                // needs to update main thread
            }
        });
    }

    public void restart(View view) {
        if (!player.running) {
            score.resetPlayPos();
            ImageView image = findViewById(R.id.play_button);

            image.setImageResource(R.drawable.play);
        }
    }

    public void undo(View view) {
        if (!editHistory.empty()) {
            Edit lastEdit = editHistory.pop();

            switch (lastEdit.editType) {
                case ADD:
                    Note n = score.removeNote(lastEdit.staff, lastEdit.time,
                                              lastEdit.note.getPitch()).note;
                    RelativeLayout rl = (RelativeLayout)n.getImageView()
                                                         .getParent();

                    Pair<NoteDur, Boolean> p = numToDurAndDot(n.getDuration());

                    drawNote(rl, new XYCoord(
                               (lastEdit.time % score.getMeasureLength()),
                               (0)), n,
                             p.first, p.second,
                             score.getTrack(lastEdit.staff).getKey(), score
                               .getMeasure(lastEdit.staff,
                                           (lastEdit.time / score
                                             .getMeasureLength())));
                    break;
                case REMOVE:
                    score.addNote(lastEdit.staff, lastEdit.time, lastEdit.note);
                    break;
            }
        }
    }

    public void cycleNoteType(View view) {
        ImageView noteImage = findViewById(R.id.note_button);

        switch (selectedNoteDur) {
            case WHOLE:
                selectedNoteDur = NoteDur.HALF;
                noteImage.setImageResource(R.drawable.half_note);
                break;
            case HALF:
                selectedNoteDur = NoteDur.QUARTER;
                noteImage.setImageResource(R.drawable.quarter_note);
                break;
            case QUARTER:
                selectedNoteDur = NoteDur.EIGHTH;
                noteImage.setImageResource(R.drawable.eighth_note);
                break;
            case EIGHTH:
                selectedNoteDur = NoteDur.WHOLE;
                noteImage.setImageResource(R.drawable.whole_note);
        }
    }

    public void cycleAccidental(View view) {
        ImageView image = findViewById(R.id.accident_button);

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

    /*public void cycleButtons(View view) {
        ToggleButton editButton = findViewById(R.id.edit_button);

        if (editButton.isChecked()) {
            //playButton.setVisibility(View.GONE);
            //restartButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            addStuffButton.setVisibility(View.VISIBLE);
            //undoButton.setVisibility(View.VISIBLE);
            accidentButton.setVisibility(View.VISIBLE);
            //noteButton.setVisibility(View.VISIBLE);
            dotButton.setVisibility(View.VISIBLE);
            restButton.setVisibility(View.VISIBLE);
        } else {
            //playButton.setVisibility(View.VISIBLE);
            //restartButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            addStuffButton.setVisibility(View.GONE);
            //undoButton.setVisibility(View.GONE);
            accidentButton.setVisibility(View.GONE);
            //noteButton.setVisibility(View.GONE);
            dotButton.setVisibility(View.GONE);
            restButton.setVisibility(View.GONE);
        }
    }*/

    public void addStuff(View view) {
        LayoutInflater li = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View promptView = li.inflate(
          R.layout.stuff_prompt, (null));
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
          (this));

        alertDialogBuilder.setView(promptView);

        Button addInstrumentButton = promptView
          .findViewById(R.id.add_instrument_button);
        Button addMeasuresButton = promptView
          .findViewById(R.id.add_measures_button);
        final TextView tempoText = promptView.findViewById(R.id.tempo_tex);
        SeekBar tempoBar = promptView.findViewById(R.id.tempo_bar);

        addInstrumentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Track track = new Track((byte)0, Track.Clef.TREBLE, key, (0));

                score.addTrack(track);

                for (int i = 0; i < score.getMeasureCount(); ++i)
                    score
                      .addNote((score.getTrackCount() - 1),
                               (score.getMeasureLength() * i),
                               new Note(Note.NoteType.REST,
                                        score.getMeasureLength(),
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
                                                 View view,
                                                 int position, long id) {
                          score.setTrackClef((score.getTrackCount() - 1),
                                             Track.Clef.values()[position]);
                          drawStaffHead(
                            Objects.requireNonNull((RelativeLayout)score
                              .getTrack(score.getTrackCount() - 1)
                              .getClefImage().getParent()),
                            score.getTrack(score.getTrackCount() - 1));
                      }

                      @Override
                      public void onNothingSelected(
                        AdapterView<?> parent) {}
                  });

                clefSpinner.setSelection(
                  score.getTrackClef(score.getTrackCount() - 1).ordinal());

                Spinner keySpinner = promptView.findViewById(R.id.key_spinner);

                keySpinner.setOnItemSelectedListener(
                  new AdapterView.OnItemSelectedListener() {
                      @Override
                      public void onItemSelected(AdapterView<?> adapterView,
                                                 View view, int i, long l) {
                          score.getTrack(score.getTrackCount() - 1)
                               .setKey(i - 7);

                          drawStaffHead(
                            Objects.requireNonNull((RelativeLayout)score
                              .getTrack(score.getTrackCount() - 1)
                              .getClefImage().getParent()),
                            score.getTrack(score.getTrackCount() - 1));
                      }

                      @Override
                      public void onNothingSelected(
                        AdapterView<?> adapterView) {}
                  });

                keySpinner.setSelection(
                  score.getTrack(score.getTrackCount() - 1).getKey() + 7);

                final TextView transpositionText = promptView
                  .findViewById(R.id.transposition_text);

                final SeekBar transpositionBar = promptView
                  .findViewById(R.id.transposition_bar);

                transpositionText.setText(getString(R.string.transposition_text,
                                                    score.getTrack(
                                                      score.getTrackCount() - 1)
                                                         .getTransposition()));
                transpositionBar.setProgress(0);

                transpositionBar.setOnSeekBarChangeListener(
                  new SeekBar.OnSeekBarChangeListener() {
                      @Override
                      public void onProgressChanged(SeekBar seekBar, int i,
                                                    boolean b) {
                          if (b)
                              transpositionText.setText(
                                getString(R.string.transposition_text, i));
                      }

                      @Override
                      public void onStartTrackingTouch(SeekBar seekBar) {}

                      @Override
                      public void onStopTrackingTouch(SeekBar seekBar) {
                          score.getTrack(score.getTrackCount() - 1)
                               .setTransposition(seekBar.getProgress());
                      }
                  });

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
                              score.addMeasure();

                              for (int k = 0; k < score.getTrackCount(); ++k) {
                                  TableRow tableRow;

                                  if (score.getMeasureCount() % 2 == 0) {
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

        tempoText.setText(getString(R.string.tempo_text, score.getTempo()));

        tempoBar.setProgress(score.getTempo());

        tempoBar.setOnSeekBarChangeListener(
          new SeekBar.OnSeekBarChangeListener() {
              @Override
              public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                  if (b)
                      tempoText.setText(getString(R.string.tempo_text, i));
              }

              @Override
              public void onStartTrackingTouch(SeekBar seekBar) {

              }

              @Override
              public void onStopTrackingTouch(SeekBar seekBar) {
                  score.setTempo(seekBar.getProgress());
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
