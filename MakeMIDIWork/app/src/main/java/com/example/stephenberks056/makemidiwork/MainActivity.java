package com.example.stephenberks056.makemidiwork;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    private Player player = new Player();
    Score score;
    private byte[] event;
    private static final int[] BUTTON_IDS = {
        R.id.C3,
        R.id.Db3,
        R.id.D3,
        R.id.Eb3,
        R.id.E3,
        R.id.F3,
        R.id.Gb3,
        R.id.G3,
        R.id.Ab3,
        R.id.A3,
        R.id.Bb3,
        R.id.B3,
        R.id.C4,
        R.id.Db4,
        R.id.D4,
        R.id.Eb4,
        R.id.E4,
        R.id.F4,
        R.id.Gb4,
        R.id.G4,
        R.id.Ab4,
        R.id.A4,
        R.id.Bb4,
        R.id.B4,
        R.id.C5,
        R.id.Db5,
        R.id.D5,
        R.id.Eb5,
        R.id.E5,
        R.id.F5,
        R.id.Gb5,
        R.id.G5,
        R.id.Ab5,
        R.id.A5,
        R.id.Bb5,
        R.id.B5,
    };


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (int id : BUTTON_IDS) {
            Button key = findViewById(id);
            key.setOnTouchListener(this);
        }

        Spinner spInstruments = findViewById(R.id.spInstruments);
        spInstruments.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeInstrument((byte)0, (byte)position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Button test = findViewById(R.id.Test);
        test.setOnTouchListener(this);
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

    private void playNote(byte channel, byte note, byte velocity) {
        event = new byte[3];
        event[0] = (byte) (0x90 | channel);  // 0x90 = note On, 0x00 = channel 1
        event[1] = note;  // 0x3C = middle C
        event[2] = velocity;  // 0x7F = the maximum velocity (127)

        // Send the MIDI event to the synthesizer.
        if (player == null)
            player = new Player();
        player.directWrite(event);
    }

    private void stopNote(byte channel, byte note, byte velocity) {
        event = new byte[3];
        event[0] = (byte) (0x80 | channel);  // 0x80 = note Off, 0x00 = channel 1
        event[1] = note;  // 0x3C = middle C
        event[2] = velocity;  // 0x00 = the minimum velocity (0)

        // Send the MIDI event to the synthesizer.
        player.directWrite(event);
    }

    private void changeInstrument(byte channel, byte instrument) {
        event = new byte[2];
        event[0] = (byte) (0xC0 | channel);  // 0xC0 = program change, 0x00 = channel 1
        event[1] = instrument;

        // Send the MIDI event to the synthesizer.
        player.directWrite(event);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(this.getClass().getName(), "Motion event: " + event);
        int note = -1, tempNote = 0;

        int bId = v.getId();
        for (int id : BUTTON_IDS) {
            ++tempNote;
            if (bId == id) {
                break;
            }
        }
        if (tempNote != 36)
            note = tempNote + 0x2F;
        if (note != -1) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Log.d(this.getClass().getName(), "MotionEvent.ACTION_DOWN");
                playNote((byte)0, (byte)(note & 0xFF), (byte)0x7F);
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Log.d(this.getClass().getName(), "MotionEvent.ACTION_UP");
                stopNote((byte)0, (byte)(note & 0xFF), (byte)0x7F);
            }
        } else {
            if (bId == R.id.Test)
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    }, 100);
                    if (score == null) {
                        score = new Score(player);
                        if (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                                == PackageManager.PERMISSION_GRANTED)
                            score.load();
                    }
                    score.play();
//                    if (ContextCompat.checkSelfPermission(MainActivity.this,
//                                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                            == PackageManager.PERMISSION_GRANTED)
//                        score.save();
                }
        }

        return false;
    }
}
