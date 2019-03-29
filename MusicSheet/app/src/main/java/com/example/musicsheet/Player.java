package com.example.musicsheet;

import android.util.Log;
import android.util.Pair;

import org.billthefarmer.mididriver.MidiDriver;

import java.util.Iterator;
import java.util.TreeMap;

public class Player implements Runnable, MidiDriver.OnMidiStartListener {
    private MidiDriver midiDriver;
    private TreeMap<Integer, TimePosition> times;
    private int tempo;
    private int startTime;
    private byte[] instruments;
    boolean running;

    Player() {
        midiDriver = new MidiDriver();
        midiDriver.setOnMidiStartListener(this);
        instruments = new byte[16];
    }

    void prepare(int tempo, int startTime, Track[] tracks) {
        this.tempo = tempo;
        this.startTime = startTime;
        times = new TreeMap<>();

        for (byte i = 0; i < tracks.length; ++i) {
            Iterator<Pair<Integer, Note>> nIt = tracks[i].getNoteIterator();
            instruments[i] = tracks[i].getInstrument();

            while (nIt.hasNext()) {
                Pair<Integer, Note> p = nIt.next();

                switch (p.second.getNoteType()) {
                    case MELODIC:
                    case PERCUSSIVE:
                        if (!times.containsKey(p.first))
                            times.put(p.first, new TimePosition(p.first));
                        if (!times.containsKey(p.first + p.second.getDuration()))
                            times.put(p.first + p.second.getDuration(),
                                    new TimePosition(p.first + p.second.getDuration()));
                        times.get(p.first).addNote(new NoteEvent(midiDriver,
                                                                 NoteEvent.EventType.START,
                                                                 i, p.second));
                        times.get(p.first + p.second.getDuration()).addNote(
                                new NoteEvent(midiDriver, NoteEvent.EventType.STOP, i, p.second));
                        break;
                    case REST:
                        break;
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
            if (time < startTime)
                continue;

            if (time > prevTime) {
                try {
                    Thread.sleep((time - prevTime) * 300 / tempo);
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
    }

    @Override
    public void onMidiStart() { Log.d(this.getClass().getName(), "onMidiStart()"); }

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

    void pause() { midiDriver.stop(); }

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
            event[0] = (byte) (0xC0 | i);  // 0xC0 = program change, 0x0X = channel X
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
