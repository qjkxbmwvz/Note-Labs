package com.example.musicsheet;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

class Note {
    enum NoteType {MELODIC, PERCUSSIVE, REST}

    private NoteType noteType;
    private int duration;
    private byte pitch;
    private byte accidental;
    private byte velocity;
    private ImageView imageView, accidentalImageView, dotImageView;

    Note(NoteType noteType, int duration, byte pitch, byte accidental,
         byte velocity) {
        this.noteType = noteType;
        this.duration = duration;
        this.pitch = pitch;
        this.accidental = accidental;
        this.velocity = velocity;
    }

    void setNoteType(NoteType noteType) { this.noteType = noteType; }

    NoteType getNoteType()              { return noteType; }

    void setDuration(int duration)      { this.duration = duration; }

    int getDuration()                   { return duration; }

    void setPitch(byte pitch)           { this.pitch = pitch; }

    byte getPitch()                     { return pitch; }

    void setAccidental(byte accidental) { this.accidental = accidental; }

    byte getAccidental()                { return accidental; }

    void setVelocity(byte velocity)     { this.velocity = velocity; }

    byte getVelocity()                  { return velocity; }

    ImageView getImageView()            { return imageView; }

    void setImageView(ImageView iv)     { this.imageView = iv; }

    ImageView getAccidentalImageView()  {return accidentalImageView; }

    void setAccidentalImageView(ImageView newImageView) {
        accidentalImageView = newImageView;
    }

    ImageView getDotImageView() { return dotImageView; }

    void setDotImageView(ImageView newImageView) {
        this.dotImageView = newImageView;
    }

    void hide() {
        if (imageView != null)
            ((RelativeLayout)imageView.getParent()).removeView(imageView);
        if (accidentalImageView != null)
            ((RelativeLayout)accidentalImageView.getParent())
              .removeView(accidentalImageView);
        if (dotImageView != null)
            ((RelativeLayout)dotImageView.getParent()).removeView(dotImageView);
    }

    //TODO: make it use blue images once those exist.
    void bluify() {
        if (imageView != null) {
            switch (duration) {
                case 288:
                case 192:
                    break;
                case 144:
                case  96:
                    break;
                case 72:
                case 48:
                    break;
                case 36:
                case 24:
                    break;
            }
        }
    }

    void blacken() {
        if (imageView != null) {
            switch (duration) {
                case 288:
                case 192:
                    switch (noteType) {
                        case MELODIC:
                        case PERCUSSIVE:
                            imageView.setImageResource(R.drawable.whole_note);
                            break;
                        case REST:
                            imageView.setImageResource(R.drawable.whole_rest);
                            break;
                    }
                    break;
                case 144:
                case  96:
                    switch (noteType) {
                        case MELODIC:
                        case PERCUSSIVE:
                            imageView.setImageResource(R.drawable.half_note);
                            break;
                        case REST:
                            imageView.setImageResource(R.drawable.half_rest);
                            break;
                    }
                    break;
                case 72:
                case 48:
                    switch (noteType) {
                        case MELODIC:
                        case PERCUSSIVE:
                            imageView.setImageResource(R.drawable.quarter_note);
                            break;
                        case REST:
                            imageView.setImageResource(R.drawable.quarter_rest);
                            break;
                    }
                    break;
                case 36:
                case 24:
                    switch (noteType) {
                        case MELODIC:
                        case PERCUSSIVE:
                            imageView.setImageResource(R.drawable.eighth_note);
                            break;
                        case REST:
                            imageView.setImageResource(R.drawable.eighth_rest);
                            break;
                    }
                    break;
            }
        }
    }
}
