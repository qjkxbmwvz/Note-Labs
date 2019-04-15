package com.example.musicsheet;

import android.widget.ImageView;

class Note {
    enum NoteType { MELODIC, PERCUSSIVE, REST }

    private NoteType noteType;
    private int duration;
    private byte pitch;
    private byte accidental;
    private byte velocity;
    private ImageView imageView;

    Note(NoteType noteType, int duration, byte pitch, byte accidental, byte velocity) {
        this.noteType   = noteType;
        this.duration   = duration;
        this.pitch      = pitch;
        this.accidental = accidental;
        this.velocity   = velocity;
    }

    void setNoteType(NoteType noteType) { this.noteType = noteType; }

    NoteType getNoteType() { return noteType; }

    int getDuration() { return duration; }

    void setPitch(byte pitch) { this.pitch = pitch; }

    void setAccidental(byte accidental) { this.accidental = accidental; }

    byte getPitch()      { return pitch;      }
    byte getAccidental() { return accidental; }
    byte getVelocity()   { return velocity;   }

    void setImageView(ImageView iv) { this.imageView = iv; }

    ImageView getImageView() { return imageView; }
}
