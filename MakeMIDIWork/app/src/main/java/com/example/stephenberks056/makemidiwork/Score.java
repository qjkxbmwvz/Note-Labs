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

class Score {
    private MidiDriver midiDriver;

    private TreeMap<Integer, TimePosition> times;
    private int tempo;
    private Track[] tracks;

    Score(MidiDriver midiDriver) {
        this.midiDriver = midiDriver;
        times = new TreeMap<>();
//        tempo = 36;
//        tracks = new Track[1];
//
//        tracks[0] = new Track((byte)0x00);
//        tracks[0].addNote(   0, new Note(Note.NoteType.MELODIC, 24, (byte)0x3E, (byte)0x7F));
//        tracks[0].addNote(  24, new Note(Note.NoteType.MELODIC, 24, (byte)0x40, (byte)0x7F));
//        tracks[0].addNote(  48, new Note(Note.NoteType.MELODIC, 24, (byte)0x42, (byte)0x7F));
//        tracks[0].addNote(  72, new Note(Note.NoteType.MELODIC, 24, (byte)0x45, (byte)0x7F));
//        tracks[0].addNote(  96, new Note(Note.NoteType.MELODIC, 48, (byte)0x40, (byte)0x7F));
//        tracks[0].addNote( 144, new Note(Note.NoteType.REST,    48, (byte)0x00, (byte)0x00));
//        tracks[0].addNote( 192, new Note(Note.NoteType.MELODIC, 24, (byte)0x4A, (byte)0x7F));
//        tracks[0].addNote( 216, new Note(Note.NoteType.MELODIC, 24, (byte)0x49, (byte)0x7F));
//        tracks[0].addNote( 240, new Note(Note.NoteType.MELODIC, 24, (byte)0x47, (byte)0x7F));
//        tracks[0].addNote( 264, new Note(Note.NoteType.MELODIC, 24, (byte)0x42, (byte)0x7F));
//        tracks[0].addNote( 288, new Note(Note.NoteType.MELODIC, 48, (byte)0x45, (byte)0x7F));
//        tracks[0].addNote( 336, new Note(Note.NoteType.REST,    48, (byte)0x00, (byte)0x00));
//        tracks[0].addNote( 384, new Note(Note.NoteType.MELODIC, 24, (byte)0x47, (byte)0x7F));
//        tracks[0].addNote( 408, new Note(Note.NoteType.MELODIC, 24, (byte)0x49, (byte)0x7F));
//        tracks[0].addNote( 432, new Note(Note.NoteType.MELODIC, 24, (byte)0x4A, (byte)0x7F));
//        tracks[0].addNote( 456, new Note(Note.NoteType.MELODIC, 36, (byte)0x45, (byte)0x7F));
//        tracks[0].addNote( 492, new Note(Note.NoteType.REST,    12, (byte)0x00, (byte)0x00));
//        tracks[0].addNote( 504, new Note(Note.NoteType.MELODIC, 36, (byte)0x3E, (byte)0x7F));
//        tracks[0].addNote( 540, new Note(Note.NoteType.REST,    36, (byte)0x00, (byte)0x00));
//        tracks[0].addNote( 576, new Note(Note.NoteType.MELODIC, 24, (byte)0x43, (byte)0x7F));
//        tracks[0].addNote( 600, new Note(Note.NoteType.MELODIC, 24, (byte)0x42, (byte)0x7F));
//        tracks[0].addNote( 624, new Note(Note.NoteType.MELODIC, 24, (byte)0x3E, (byte)0x7F));
//        tracks[0].addNote( 648, new Note(Note.NoteType.MELODIC, 96, (byte)0x39, (byte)0x7F));
//        tracks[0].addNote( 744, new Note(Note.NoteType.REST,    24, (byte)0x00, (byte)0x00));
//        tracks[0].addNote( 768, new Note(Note.NoteType.MELODIC, 48, (byte)0x3B, (byte)0x7F));
//        tracks[0].addNote( 816, new Note(Note.NoteType.MELODIC, 48, (byte)0x3D, (byte)0x7F));
//        tracks[0].addNote( 864, new Note(Note.NoteType.MELODIC, 48, (byte)0x3E, (byte)0x7F));
//        tracks[0].addNote( 912, new Note(Note.NoteType.MELODIC, 48, (byte)0x43, (byte)0x7F));
//        tracks[0].addNote( 960, new Note(Note.NoteType.MELODIC, 24, (byte)0x42, (byte)0x7F));
//        tracks[0].addNote( 984, new Note(Note.NoteType.MELODIC, 24, (byte)0x43, (byte)0x7F));
//        tracks[0].addNote(1008, new Note(Note.NoteType.MELODIC, 24, (byte)0x45, (byte)0x7F));
//        tracks[0].addNote(1032, new Note(Note.NoteType.MELODIC, 24, (byte)0x42, (byte)0x7F));
//        tracks[0].addNote(1056, new Note(Note.NoteType.MELODIC, 24, (byte)0x40, (byte)0x7F));
//        tracks[0].addNote(1080, new Note(Note.NoteType.MELODIC, 24, (byte)0x42, (byte)0x7F));
//        tracks[0].addNote(1104, new Note(Note.NoteType.MELODIC, 24, (byte)0x43, (byte)0x7F));
//        tracks[0].addNote(1128, new Note(Note.NoteType.MELODIC, 24, (byte)0x40, (byte)0x7F));
//        tracks[0].addNote(1152, new Note(Note.NoteType.MELODIC, 24, (byte)0x3B, (byte)0x7F));
//        tracks[0].addNote(1176, new Note(Note.NoteType.MELODIC, 24, (byte)0x43, (byte)0x7F));
//        tracks[0].addNote(1200, new Note(Note.NoteType.MELODIC, 24, (byte)0x42, (byte)0x7F));
//        tracks[0].addNote(1224, new Note(Note.NoteType.MELODIC, 24, (byte)0x39, (byte)0x7F));
//        tracks[0].addNote(1248, new Note(Note.NoteType.MELODIC, 48, (byte)0x40, (byte)0x7F));
//        tracks[0].addNote(1296, new Note(Note.NoteType.MELODIC, 48, (byte)0x3D, (byte)0x7F));
//        tracks[0].addNote(1344, new Note(Note.NoteType.MELODIC, 48, (byte)0x3E, (byte)0x7F));
//        tracks[0].addNote(1392, new Note(Note.NoteType.MELODIC, 24, (byte)0x39, (byte)0x7F));
//        tracks[0].addNote(1416, new Note(Note.NoteType.MELODIC, 24, (byte)0x40, (byte)0x7F));
//        tracks[0].addNote(1440, new Note(Note.NoteType.MELODIC, 96, (byte)0x3E, (byte)0x7F));
    }

    void play() {
        byte[] event = new byte[2];

        times.clear();

        for (byte i = 0; i < tracks.length; ++i) {
            Iterator<Pair<Integer, Note>> nIt = tracks[i].getNoteIterator();

            event[0] = (byte) (0xC0 | i);  // 0xC0 = program change, 0x0X = channel X
            event[1] = tracks[i].getInstrument();
            midiDriver.write(event);

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
                        times.get(p.first).addNote(new NoteEvent(midiDriver, NoteEvent.EventType.START,
                                i, p.second));
                        times.get(p.first + p.second.getDuration()).addNote(new NoteEvent(midiDriver,
                                NoteEvent.EventType.STOP, i, p.second));
                        break;
                    case REST:
                        break;
                }
            }
        }

        Iterator<TimePosition> tpIt = times.values().iterator();
        int prevTime = 0;
        while (tpIt.hasNext()) {
            TimePosition tp = tpIt.next();
            int time = tp.getTime();
            if (time > prevTime) {
                try {
                    Thread.sleep((time - prevTime) * 300 / tempo);
                } catch (Exception ignored) {}
                prevTime = time;
            }
            Iterator<NoteEvent> neIt = tp.getNoteEventIterator();

            while (neIt.hasNext())
                neIt.next().play();
        }
    }

    void save() {
        try {
            File out = new File(Environment.getExternalStorageDirectory() + "/test.nl");
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

    void load() {
        try {
            File in = new File(Environment.getExternalStorageDirectory() + "/test.nl");
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
