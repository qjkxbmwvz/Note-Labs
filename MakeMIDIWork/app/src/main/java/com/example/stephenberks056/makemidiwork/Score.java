package com.example.stephenberks056.makemidiwork;

import org.billthefarmer.mididriver.MidiDriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;

import android.os.Environment;

public class Score {
    private MidiDriver midiDriver;

    private ArrayList<LinkedList<NoteEvent>> notes;
    private int tempo;

    Score(MidiDriver midiDriver) {
        this.midiDriver = midiDriver;
        notes = new ArrayList<>();
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
        for (LinkedList<NoteEvent> noteEventList : notes) {
            int shortest = Integer.MAX_VALUE;
            for (NoteEvent noteEvent : noteEventList) {
                noteEvent.play();
                if (noteEvent.getDuration() < shortest && noteEvent.getDuration() != 0)
                    shortest = noteEvent.getDuration() * tempo / 120;
            }
            try {
                Thread.sleep(shortest);
            } catch (Exception ignored) {}
        }
    }

    public void save() {
        try {
            File out = new File(Environment.getExternalStorageDirectory() + "/saved.nl");
            DataOutputStream os = new DataOutputStream(new FileOutputStream(out, false));

            os.writeInt(tempo);
            for (LinkedList<NoteEvent> noteEventList : notes) {
                os.writeInt(noteEventList.size());
                for (NoteEvent noteEvent : noteEventList) {
                    os.writeInt( noteEvent.getEventType().ordinal());
                    os.writeInt( noteEvent.getDuration());
                    os.writeByte(noteEvent.getChannel());
                    os.writeByte(noteEvent.getPitch());
                    os.writeByte(noteEvent.getVelocity());
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

            tempo = is.readInt();
            notes.clear();
            int count = 0;

            while (is.available() > 0) {
                notes.add(new LinkedList<NoteEvent>());
                int listLength = is.readInt();

                for (int i = 0; i < listLength; ++i) {
                    NoteEvent.EventType eventType = NoteEvent.EventType.values()[is.readInt()];
                    int duration  = is.readInt();
                    byte channel  = is.readByte();
                    byte pitch    = is.readByte();
                    byte velocity = is.readByte();

                    notes.get(count).add(new NoteEvent(midiDriver, eventType, duration,
                                                       channel, pitch, velocity));
                }
                ++count;
            }
            is.close();
        } catch (Exception ignored) {}
    }
}
