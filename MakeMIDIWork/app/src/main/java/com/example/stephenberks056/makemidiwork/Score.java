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
        /*notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.add(new LinkedList<NoteEvent>());
        notes.get(0).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x3E, (byte)0));
        notes.get(1).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x3E, (byte)0));
        notes.get(1).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x40, (byte)0));
        notes.get(2).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x40, (byte)0));
        notes.get(2).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x42, (byte)0));
        notes.get(3).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x42, (byte)0));
        notes.get(3).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x45, (byte)0));
        notes.get(4).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x45, (byte)0));
        notes.get(4).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 600, (byte)0x7F, (byte)0x40, (byte)0));
        notes.get(5).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x40, (byte)0));
        notes.get(5).add(new NoteEvent(midiDriver, NoteEvent.EventType.REST, 600, (byte)0, (byte)0, (byte)0));
        notes.get(6).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x4A, (byte)0));
        notes.get(7).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x4A, (byte)0));
        notes.get(7).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x49, (byte)0));
        notes.get(8).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x49, (byte)0));
        notes.get(8).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x47, (byte)0));
        notes.get(9).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x47, (byte)0));
        notes.get(9).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x42, (byte)0));
        notes.get(10).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x42, (byte)0));
        notes.get(10).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 600, (byte)0x7F, (byte)0x45, (byte)0));
        notes.get(11).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x45, (byte)0));
        notes.get(11).add(new NoteEvent(midiDriver, NoteEvent.EventType.REST, 600, (byte)0, (byte)0, (byte)0));
        notes.get(12).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x47, (byte)0));
        notes.get(13).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x47, (byte)0));
        notes.get(13).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x49, (byte)0));
        notes.get(14).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x49, (byte)0));
        notes.get(14).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x4A, (byte)0));
        notes.get(15).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x4A, (byte)0));
        notes.get(15).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 450, (byte)0x7F, (byte)0x45, (byte)0));
        notes.get(16).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x45, (byte)0));
        notes.get(16).add(new NoteEvent(midiDriver, NoteEvent.EventType.REST, 150, (byte)0, (byte)0, (byte)0));
        notes.get(17).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 450, (byte)0x7F, (byte)0x3E, (byte)0));
        notes.get(18).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x3E, (byte)0));
        notes.get(18).add(new NoteEvent(midiDriver, NoteEvent.EventType.REST, 450, (byte)0, (byte)0, (byte)0));
        notes.get(19).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x43, (byte)0));
        notes.get(20).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x43, (byte)0));
        notes.get(20).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x42, (byte)0));
        notes.get(21).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x42, (byte)0));
        notes.get(21).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x3E, (byte)0));
        notes.get(22).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x3E, (byte)0));
        notes.get(22).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 900, (byte)0x7F, (byte)0x39, (byte)0));
        notes.get(23).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x39, (byte)0));
        notes.get(23).add(new NoteEvent(midiDriver, NoteEvent.EventType.REST, 300, (byte)0, (byte)0, (byte)0));
        notes.get(24).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 600, (byte)0x7F, (byte)0x3B, (byte)0));
        notes.get(25).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x3B, (byte)0));
        notes.get(25).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 600, (byte)0x7F, (byte)0x3D, (byte)0));
        notes.get(26).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x3D, (byte)0));
        notes.get(26).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 600, (byte)0x7F, (byte)0x3E, (byte)0));
        notes.get(27).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x3E, (byte)0));
        notes.get(27).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 600, (byte)0x7F, (byte)0x43, (byte)0));
        notes.get(28).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x43, (byte)0));
        notes.get(28).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x42, (byte)0));
        notes.get(29).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x42, (byte)0));
        notes.get(29).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x43, (byte)0));
        notes.get(30).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x43, (byte)0));
        notes.get(30).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x45, (byte)0));
        notes.get(31).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x45, (byte)0));
        notes.get(31).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x42, (byte)0));
        notes.get(32).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x42, (byte)0));
        notes.get(32).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x40, (byte)0));
        notes.get(33).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x40, (byte)0));
        notes.get(33).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x42, (byte)0));
        notes.get(34).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x42, (byte)0));
        notes.get(34).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x43, (byte)0));
        notes.get(35).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x43, (byte)0));
        notes.get(35).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x40, (byte)0));
        notes.get(36).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x40, (byte)0));
        notes.get(36).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x3B, (byte)0));
        notes.get(37).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x3B, (byte)0));
        notes.get(37).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x43, (byte)0));
        notes.get(38).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x43, (byte)0));
        notes.get(38).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x42, (byte)0));
        notes.get(39).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x42, (byte)0));
        notes.get(39).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x39, (byte)0));
        notes.get(40).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x39, (byte)0));
        notes.get(40).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 600, (byte)0x7F, (byte)0x40, (byte)0));
        notes.get(41).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x40, (byte)0));
        notes.get(41).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 600, (byte)0x7F, (byte)0x3D, (byte)0));
        notes.get(42).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x3D, (byte)0));
        notes.get(42).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 600, (byte)0x7F, (byte)0x3E, (byte)0));
        notes.get(43).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x3E, (byte)0));
        notes.get(43).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x39, (byte)0));
        notes.get(44).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x39, (byte)0));
        notes.get(44).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 300, (byte)0x7F, (byte)0x40, (byte)0));
        notes.get(45).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x40, (byte)0));
        notes.get(45).add(new NoteEvent(midiDriver, NoteEvent.EventType.START, 1200, (byte)0x7F, (byte)0x3E, (byte)0));
        notes.get(46).add(new NoteEvent(midiDriver, NoteEvent.EventType.STOP, 0, (byte)0x7F, (byte)0x3E, (byte)0));*/
    }

    public void play() {
        for (LinkedList<NoteEvent> noteEventList : notes) {
            int shortest = Integer.MAX_VALUE;
            for (NoteEvent noteEvent : noteEventList) {
                noteEvent.play();
                if (noteEvent.getEventType() != NoteEvent.EventType.STOP
                 && noteEvent.getDuration() < shortest)
                    shortest = noteEvent.getDuration();
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

            for (LinkedList<NoteEvent> noteEventList : notes) {
                os.writeInt(noteEventList.size());
                for (NoteEvent noteEvent : noteEventList) {
                    os.writeInt( noteEvent.getEventType().ordinal());
                    os.writeInt( noteEvent.getDuration());
                    os.writeByte(noteEvent.getVelocity());
                    os.writeByte(noteEvent.getPitch());
                    os.writeByte(noteEvent.getChannel());
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

            notes.clear();
            int count = 0;

            while (is.available() > 0) {
                notes.add(new LinkedList<NoteEvent>());
                int listLength = is.readInt();

                for (int i = 0; i < listLength; ++i) {
                    NoteEvent.EventType eventType = NoteEvent.EventType.values()[is.readInt()];
                    int duration  = is.readInt();
                    byte velocity = is.readByte();
                    byte pitch    = is.readByte();
                    byte channel  = is.readByte();

                    notes.get(count).add(new NoteEvent(midiDriver, eventType, duration,
                                                       velocity, pitch, channel));
                }
                ++count;
            }
            is.close();
        } catch (Exception ignored) {}
    }
}
