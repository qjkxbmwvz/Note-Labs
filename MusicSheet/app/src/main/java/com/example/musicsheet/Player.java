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
    private int endTime;
    private byte[] instruments;
    boolean running;
    private MusicSheet musicSheet;
    private int oldCoord;

    Player(MusicSheet musicSheet) {
        midiDriver = new MidiDriver();
        midiDriver.setOnMidiStartListener(this);
        instruments = new byte[16];
        this.musicSheet = musicSheet;
    }

    void prepare(int tempo, int startTime, ArrayList<Track> tracks) {
        this.tempo = tempo;
        this.startTime = startTime;
        if (times != null)
            times.clear();
        else
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

                            NoteEvent ne1 = new NoteEvent(
                              midiDriver, NoteEvent.EventType.START, i, n);
                            ne1.setPitch((byte)(ne1.getPitch()
                                                + tracks.get(i)
                                                        .getTransposition()));
                            Objects.requireNonNull(times.get(t)).addNote(ne1);

                            NoteEvent ne2 = new NoteEvent(
                              midiDriver, NoteEvent.EventType.STOP, i, n);
                            ne2.setPitch((byte)(ne2.getPitch()
                                                + tracks.get(i)
                                                        .getTransposition()));
                            Objects.requireNonNull(
                              times.get(t + n.getDuration())).addNote(ne2);
                            endTime = t + n.getDuration();
                            break;
                        case REST:
                            endTime += n.getDuration();
                            break;
                    }
                }
            }
        }
        setAllInstruments();

        oldCoord = musicSheet.getScrollCoord();
    }

    @Override
    public void run() {
        int scrollRange = musicSheet.getScrollRange();
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

            musicSheet.scrollToCoord(time * scrollRange / endTime);

            while (neIt.hasNext()) {
                NoteEvent ne = neIt.next();

                ne.play();
                switch (ne.getEventType()) {
                    case START:
                        musicSheet.bluifyNote(ne.getNote());
                        break;
                    case STOP:
                        musicSheet.blackenNote(ne.getNote());
                        break;
                }
            }
        }
        running = false;
        startTime = 0;
        musicSheet.resetPlayButton();

        musicSheet.scrollToCoord(oldCoord);
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

    void directWrite(byte... event) {
        if (!running) {
            midiDriver.write(event);
            if ((event[0] & 0xF0) == 0xC0)
                instruments[event[0] & 0x0F] = event[1];
        }
    }

    private void setAllInstruments() {
        for (int i = 0; i < instruments.length; ++i)
            directWrite((byte)(0xC0 | i), instruments[i]);
    }

    private void stopAllNotes() {
        for (int i = 0; i < instruments.length; ++i)
            for (int j = 0; j < 128; ++j)
                directWrite((byte)(0x80 | i), (byte)j, (byte)0x7F);
    }
}
