package com.example.musicsheet;

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
        if (imageView != null && imageView.getParent() != null)
            ((RelativeLayout)imageView.getParent()).removeView(imageView);
        if (accidentalImageView != null
            && accidentalImageView.getParent() != null)
            ((RelativeLayout)accidentalImageView.getParent())
              .removeView(accidentalImageView);
        if (dotImageView != null && dotImageView.getParent() != null)
            ((RelativeLayout)dotImageView.getParent()).removeView(dotImageView);
    }

    void bluify() {
        if (imageView != null) {
            Object tag = imageView.getTag();
            switch ((int)tag) {
                case 0:
                    imageView.setImageResource(R.drawable.blue_whole);
                    break;
                case 1:
                    imageView.setImageResource(R.drawable.flipped_blue_half);
                    break;
                case 2:
                    imageView.setImageResource(R.drawable.blue_half);
                    break;
                case 3:
                    imageView.setImageResource(R.drawable.flipped_blue_quarter);
                    break;
                case 4:
                    imageView.setImageResource(R.drawable.blue_quarter);
                    break;
                case 5:
                    imageView.setImageResource(R.drawable.flipped_blue_eighth);
                    break;
                case 6:
                    imageView.setImageResource(R.drawable.blue_eighth);
                    break;
                case 7:
                    imageView.setImageResource(
                      R.drawable.flipped_blue_sixteenth);
                    break;
                case 8:
                    imageView.setImageResource(R.drawable.blue_sixteenth);
                    break;
            }
        }
    }

    void blacken() {
        if (imageView != null) {
            Object tag = imageView.getTag();
            switch ((int)tag) {
                case 0:
                    imageView.setImageResource(R.drawable.whole_note);
                    break;
                case 1:
                    imageView.setImageResource(R.drawable.flipped_half_note);
                    break;
                case 2:
                    imageView.setImageResource(R.drawable.half_note);
                    break;
                case 3:
                    imageView.setImageResource(R.drawable.flipped_quarter_note);
                    break;
                case 4:
                    imageView.setImageResource(R.drawable.quarter_note);
                    break;
                case 5:
                    imageView.setImageResource(R.drawable.flipped_eighth_note);
                    break;
                case 6:
                    imageView.setImageResource(R.drawable.eighth_note);
                    break;
                case 7:
                    imageView.setImageResource(
                      R.drawable.flipped_sixteenth_note);
                    break;
                case 8:
                    imageView.setImageResource(R.drawable.sixteenth_note);
                    break;
            }
        }
    }
}
