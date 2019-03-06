package com.example.stephenberks056.makemidiwork;

public class Note {
    enum NoteType { MELODIC, PERCUSSIVE, REST };

    private NoteType noteType;
    private int duration;
    private byte pitch;
    private byte velocity;

    Note(NoteType noteType, int duration, byte pitch, byte velocity) {
        this.noteType  = noteType;
        this.duration   = duration;
        this.pitch      = pitch;
        this.velocity   = velocity;
    }

    public NoteType getNoteType() { return noteType; }

    public int getDuration() { return duration; }

    public byte getPitch()    { return pitch; }
    public byte getVelocity() { return velocity; }
}
