package com.example.musicsheet;

import android.util.Log;

import org.billthefarmer.mididriver.MidiDriver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.TreeMap;

public class Player implements Runnable, MidiDriver.OnMidiStartListener {
    private MidiDriver midiDriver;
    private TreeMap<Integer, TimePosition> times;
    private int tempo;
    private int startTime;
    private byte[] instruments;
    boolean running;
    MusicSheet musicSheet;

    Player(MusicSheet musicSheet) {
        midiDriver = new MidiDriver();
        midiDriver.setOnMidiStartListener(this);
        instruments = new byte[16];
        this.musicSheet = musicSheet;
    }

    void prepare(int tempo, int startTime, ArrayList<Track> tracks) {
        this.tempo = tempo;
        this.startTime = startTime;
        times = new TreeMap<>();

        for (byte i = 0; i < tracks.size(); ++i) {
            Iterator<Integer> tIt = tracks.get(i).getTimeIterator();
            Iterator<LinkedList<Note>> nIt = tracks.get(i).getNoteIterator();
            instruments[i] = tracks.get(i).getInstrument();

            while (tIt.hasNext()) {
                int t = tIt.next();
                LinkedList<Note> nl = nIt.next();

                for (Note n : nl) {
                    switch (n.getNoteType()) {
                        case MELODIC:
                        case PERCUSSIVE:
                            if (!times.containsKey(t))
                                times.put(t, new TimePosition(t));
                            if (!times.containsKey(t + n.getDuration()))
                                times.put(t + n.getDuration(),
                                          new TimePosition(
                                                  t + n.getDuration()));
                            Objects.requireNonNull(times.get(t)).addNote(
                                    new NoteEvent(midiDriver,
                                                  NoteEvent.EventType.START, i,
                                                  n));
                            Objects.requireNonNull(
                                    times.get(t + n.getDuration())).addNote(
                                    new NoteEvent(midiDriver,
                                                  NoteEvent.EventType.STOP, i,
                                                  n));
                            break;
                        case REST:
                            break;
                    }
                }
            }
        }
        setAllInstruments();
    }

    @Override
    public void run() {
        running = true;

        Iterator<TimePosition> tpIt = times.values().iterator();
        int prevTime = 0;

        while (tpIt.hasNext()) {
            TimePosition tp = tpIt.next();
            int time = tp.getTime();
            if (time < startTime) {
                prevTime = time;
                continue;
            }

            if (time > prevTime) {
                try {
                    Thread.sleep((time - prevTime) * 600 / tempo);
                } catch (InterruptedException e) {
                    running = false;
                    stopAllNotes();
                    return;
                }
                prevTime = time;
                startTime = time;
            }
            Iterator<NoteEvent> neIt = tp.getNoteEventIterator();

            while (neIt.hasNext())
                neIt.next().play();
        }
        running = false;
        startTime = 0;
        musicSheet.resetPlayButton();
    }

    @Override
    public void onMidiStart() {
        Log.d(this.getClass().getName(), "onMidiStart()");
    }

    void resume() {
        midiDriver.start();
        if (!running)
            stopAllNotes();

        // Get the configuration.
        int[] config = midiDriver.config();

        // Print out the details.
        Log.d(this.getClass().getName(), "maxVoices: " + config[0]);
        Log.d(this.getClass().getName(), "numChannels: " + config[1]);
        Log.d(this.getClass().getName(), "sampleRate: " + config[2]);
        Log.d(this.getClass().getName(), "mixBufferSize: " + config[3]);

        setAllInstruments();
    }

    int getStartTime() { return startTime; }

    void pause()       { midiDriver.stop(); }

    void directWrite(byte[] event) {
        if (!running) {
            midiDriver.write(event);
            if ((event[0] & 0xF0) == 0xC0)
                instruments[event[0] & 0x0F] = event[1];
        }
    }

    private void setAllInstruments() {
        byte[] event = new byte[2];

        for (int i = 0; i < instruments.length; ++i) {
            event[0] = (byte)(0xC0
                              | i);  // 0xC0 = program change, 0x0X = channel X
            event[1] = instruments[i];
            midiDriver.write(event);
        }
    }

    private void stopAllNotes() {
        byte[] event = new byte[3];

        event[2] = 0x7F;

        for (int i = 0; i < instruments.length; ++i) {
            event[0] = (byte)(0x80 | i);
            for (int j = 0; j < 128; ++j) {
                event[1] = (byte)j;
                midiDriver.write(event);
            }
        }
    }
}
