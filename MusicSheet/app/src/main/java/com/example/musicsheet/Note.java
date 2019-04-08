package com.example.musicsheet;

class Note {
    enum NoteType { MELODIC, PERCUSSIVE, REST }

    private NoteType noteType;
    private int duration;
    private byte pitch;
    private byte velocity;

    Note(NoteType noteType, int duration, byte pitch, byte velocity) {
        this.noteType = noteType;
        this.duration = duration;
        this.pitch    = pitch;
        this.velocity = velocity;
    }

    NoteType getNoteType() { return noteType; }

    int getDuration() { return duration; }

    byte getPitch()    { return pitch; }
    byte getVelocity() { return velocity; }
}
