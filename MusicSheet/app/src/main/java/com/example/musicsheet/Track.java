package com.example.musicsheet;

import android.util.Pair;

import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

class Track {
    private byte instrument;
    private TreeMap<Integer, LinkedList<Note>> notes;

    Track(byte instrument) {
        this.instrument = instrument;
        notes = new TreeMap<>();
    }

    void setInstrument(byte instrument) { this.instrument = instrument; }

    byte getInstrument() { return instrument; }

    Iterator<Integer> getTimeIterator() { return notes.keySet().iterator(); }

    Iterator<LinkedList<Note>> getNoteIterator() { return notes.values().iterator(); }

    int getTrackLength() { return notes.size(); }

    void addNote(int time, Note note) {
        if (!notes.containsKey(time))
            notes.put(time, new LinkedList<Note>());
        if (notes.get(time).size() == 0 || note.getNoteType() != Note.NoteType.REST) {
            if (notes.get(time).size() == 1
             && notes.get(time).getFirst().getNoteType() == Note.NoteType.REST)
                notes.get(time).remove(0);
            notes.get(time).add(note);
        }
    }

    void removeNote(int time, Note note) {
        if (notes.containsKey(time))
            notes.get(time).remove(note);
        if (notes.get(time).size() == 0)
            notes.get(time).add(new Note(Note.NoteType.REST, 0, (byte)0, (byte)0));
    }

    LinkedList<Note> checkNote(int time) {
        if (notes.containsKey(time))
            return notes.get(time);
        else
            return null;
    }

    TreeSet<Integer> getMeasure(int measureNum, Fraction timeSignature) {
        int startPoint = measureNum * 192 * timeSignature.num / timeSignature.den;
        int endPoint   = startPoint + 192 * timeSignature.num / timeSignature.den;
        TreeSet<Integer> ret = new TreeSet<>();

        for (int key : notes.keySet())
            if (key >= startPoint && key < endPoint)
                ret.add(key);
            else if (key >= endPoint)
                break;

        return ret;
    }
}
