package com.example.musicsheet;

import android.util.Pair;

import java.util.Iterator;
import java.util.LinkedList;

class Track {
    private byte instrument;
    private LinkedList<Pair<Integer, Note>> notes;

    Track(byte instrument) {
        this.instrument = instrument;
        notes = new LinkedList<>();
    }

    void setInstrument(byte instrument) { this.instrument = instrument; }

    byte getInstrument() { return instrument; }

    Iterator<Pair<Integer, Note>> getNoteIterator() { return notes.iterator(); }

    int getTrackLength() { return notes.size(); }

    void addNote(int time, Note note) { notes.add(new Pair<>(time, note)); }
}
