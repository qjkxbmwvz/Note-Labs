package com.example.musicsheet;

import java.util.Iterator;
import java.util.LinkedList;

class TimePosition {
    private int time;
    private LinkedList<NoteEvent> noteEvents;

    TimePosition(int time) {
        this.time = time;
        noteEvents = new LinkedList<>();
    }

    int getTime() { return time; }

    Iterator<NoteEvent> getNoteEventIterator() { return noteEvents.iterator(); }

    void addNote(NoteEvent noteEvent) { noteEvents.add(noteEvent); }
}
