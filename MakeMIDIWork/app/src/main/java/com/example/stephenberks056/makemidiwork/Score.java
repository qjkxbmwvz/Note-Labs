package com.example.stephenberks056.makemidiwork;

import org.billthefarmer.mididriver.MidiDriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import android.os.Environment;
import android.util.Pair;

public class Score {
    private MidiDriver midiDriver;

    private TreeMap<Integer, TimePosition> times;
    private int tempo;
    private Track[] tracks;

    Score(MidiDriver midiDriver) {
        this.midiDriver = midiDriver;
        times = new TreeMap<>();
//        tempo = 72;
//
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x3E, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x3E, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x40, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x40, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x42, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x42, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x45, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x45, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 600, (byte)0, (byte)0x40, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 600, (byte)0, (byte)0x40, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x4A, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x4A, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x49, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x49, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x47, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x47, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x42, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x42, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 600, (byte)0, (byte)0x45, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 600, (byte)0, (byte)0x45, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x47, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x47, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x49, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x49, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x4A, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x4A, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 450, (byte)0, (byte)0x45, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 150, (byte)0, (byte)0x45, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 450, (byte)0, (byte)0x3E, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 450, (byte)0, (byte)0x3E, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x43, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x43, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x42, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x42, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x3E, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x3E, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 900, (byte)0, (byte)0x39, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 300, (byte)0, (byte)0x39, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 600, (byte)0, (byte)0x3B, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x3B, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 600, (byte)0, (byte)0x3D, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x3D, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 600, (byte)0, (byte)0x3E, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x3E, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 600, (byte)0, (byte)0x43, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x43, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x42, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x42, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x43, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x43, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x45, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x45, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x42, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x42, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x40, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x40, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x42, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x42, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x43, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x43, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x40, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x40, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x3B, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x3B, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x43, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x43, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x42, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x42, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x39, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x39, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 600, (byte)0, (byte)0x40, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x40, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 600, (byte)0, (byte)0x3D, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x3D, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 600, (byte)0, (byte)0x3E, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x3E, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x39, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x39, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0, (byte)0x40, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x40, (byte)0x7F));
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 1200, (byte)0, (byte)0x3E, (byte)0x7F));
//        notes.add(new LinkedList<NoteEvent>());
//        notes.get(notes.size() - 1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0, (byte)0x3E, (byte)0x7F));
    }

    public void play() {
        times.clear();

        for (Track track : tracks) {
            Iterator<Pair<Integer, Note>> nIt = track.getNoteIterator();

            while (nIt.hasNext()) {
                Pair<Integer, Note> p = nIt.next();
                if (!times.containsKey(p.first)) {
                    times.put(p.first, new TimePosition(p.first));
                }
            }
        }

        Iterator<TimePosition> tpIt = times.iterator();
        int prevTime = 0;
        while (tpIt.hasNext()) {
            TimePosition tp = tpIt.next();
            int time = tp.getTime();
            if (time > prevTime) {
                try {
                    Thread.sleep(time - prevTime);
                } catch (Exception ignored) {}
            }
            Iterator<NoteEvent> neIt = tp.getNoteEventIterator();

            while (neIt.hasNext())
                neIt.next().play();
        }
    }

    public void save() {
        try {
            File out = new File(Environment.getExternalStorageDirectory() + "/saved.nl");
            DataOutputStream os = new DataOutputStream(new FileOutputStream(out, false));

            os.writeInt(tempo);
            os.writeByte((byte)tracks.length);
            for (Track track : tracks) {
                os.writeByte(track.getInstrument());
                os.writeInt(track.getTrackLength());
                Iterator<Pair<Integer, Note>> it = track.getNoteIterator();
                while (it.hasNext()) {
                    Pair<Integer, Note> p = it.next();
                    os.writeInt(p.first);
                    os.writeInt(p.second.getNoteType().ordinal());
                    os.writeInt(p.second.getDuration());

                    os.writeByte(p.second.getPitch());
                    os.writeByte(p.second.getVelocity());
                }
            }
            os.flush();
            os.close();
        } catch (Exception ignored) {}
    }

    public void load() {
        try {
            File in = new File(Environment.getExternalStorageDirectory() + "/saved.nl");
            DataInputStream is = new DataInputStream(new FileInputStream(in));
            byte[] event = new byte[2];

            tempo = is.readInt();
            byte il = is.readByte();
            tracks = new Track[il];
            for (int i = 0; i < il; ++i) {
                tracks[i] = new Track(is.readByte());
                event[0] = (byte) (0xC0 | i);  // 0xC0 = program change, 0x0X = channel X
                event[1] = tracks[i].getInstrument();
                midiDriver.write(event);

                int tl = is.readInt();

                for (int j = 0; j < tl; ++j) {
                    int time = is.readInt();

                    Note.NoteType noteType = Note.NoteType.values()[is.readInt()];

                    int duration = is.readInt();

                    byte pitch    = is.readByte();
                    byte velocity = is.readByte();
                    tracks[i].addNote(time, new Note(noteType, duration, pitch, velocity));
                }
            }
            is.close();
        } catch (Exception ignored) {}
    }
}
