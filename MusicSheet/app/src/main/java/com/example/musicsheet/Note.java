package com.example.musicsheet;

import android.widget.ImageView;

class Note {
    enum NoteType {MELODIC, PERCUSSIVE, REST}

    private NoteType noteType;
    private int duration;
    private byte pitch;
    private byte accidental;
    private byte velocity;
    private ImageView imageView, accidentalImageView;

    Note(NoteType noteType, int duration, byte pitch, byte accidental,
         byte velocity) {
        this.noteType = noteType;
        this.duration = duration;
        this.pitch = pitch;
        this.accidental = accidental;
        this.velocity = velocity;
    }

    void setNoteType(NoteType noteType) {
        this.noteType = noteType;
    }

    NoteType getNoteType()         { return noteType; }

    void setDuration(int duration) { this.duration = duration; }

    int getDuration()              { return duration; }

    void setPitch(byte pitch)      { this.pitch = pitch; }

    byte getPitch()                { return pitch; }

    byte getAccidental()           { return accidental; }

    void setAccidental(byte accidental) {
        this.accidental = accidental;
    }

    byte getVelocity()                 { return velocity; }

    ImageView getImageView()           { return imageView; }

    void setImageView(ImageView iv)    { this.imageView = iv; }

    ImageView getAccidentalImageView() {return accidentalImageView; }

    void setAccidentalImageView(ImageView newImageView) {
        accidentalImageView = newImageView;
    }
}
