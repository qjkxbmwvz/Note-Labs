package com.example.musicsheet;

import android.util.Pair;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
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
        if (Objects.requireNonNull(notes.get(time)).size() == 0
                                || note.getNoteType() != Note.NoteType.REST) {
            if (Objects.requireNonNull(notes.get(time)).size() == 1
             && Objects.requireNonNull(notes.get(time)).getFirst().getNoteType()
                                    == Note.NoteType.REST)
                Objects.requireNonNull(notes.get(time)).remove(0);
            Objects.requireNonNull(notes.get(time)).add(note);
        }
    }

    void removeNote(int time, Note note) {
        if (notes.containsKey(time))
            Objects.requireNonNull(notes.get(time)).remove(note);
        if (Objects.requireNonNull(notes.get(time)).size() == 0)
            Objects.requireNonNull(notes.get(time)).add(new Note(Note.NoteType.REST,
                                                                 (0), (byte)0, (byte)0));
    }

    Note getNote(int time, byte pitch) {
        if (notes.containsKey(time))
            for (Note n : Objects.requireNonNull(notes.get(time)))
                if (n.getPitch() == pitch)
                    return n;
        return null;
    }

    LinkedList<Note> checkNote(int time) {
        if (notes.containsKey(time))
            return notes.get(time);
        else
            return null;
    }

    TreeSet<Pair<Integer, LinkedList<Note>>> getMeasure(int measureNum, Fraction timeSignature) {
        int startPoint = measureNum * 192 * timeSignature.num / timeSignature.den;
        int endPoint   = startPoint + 192 * timeSignature.num / timeSignature.den;
        TreeSet<Pair<Integer, LinkedList<Note>>> ret
                = new TreeSet<>(new Comparator<Pair<Integer, LinkedList<Note>>>() {
            @Override
            public int compare(Pair<Integer, LinkedList<Note>> a,
                               Pair<Integer, LinkedList<Note>> b) { return a.first - b.first; }
        });

        for (int key : notes.keySet())
            if (key >= startPoint && key < endPoint)
                ret.add(new Pair<>(key, notes.get(key)));
            else if (key >= endPoint)
                break;

        return ret;
    }
}
