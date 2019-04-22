package com.example.musicsheet;

import android.util.Pair;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;

class Track {
    enum Clef {TREBLE, ALTO, BASS, PERCUSSION}

    private Clef clef;
    private byte instrument;
    private TreeMap<Integer, LinkedList<Note>> notes;

    private ImageView clefImage, numImage, denImage;
    private ImageView[] keySigImages;

    Track(byte instrument, Clef clef) {
        this.clef = clef;
        this.instrument = instrument;
        notes = new TreeMap<>();
        keySigImages = new ImageView[7];
    }

    void setClef(Clef clef)             { this.clef = clef; }

    Clef getClef()                      { return clef; }

    void setInstrument(byte instrument) { this.instrument = instrument; }

    byte getInstrument()                { return instrument; }

    Iterator<Integer> getTimeIterator() { return notes.keySet().iterator(); }

    Iterator<LinkedList<Note>> getNoteIterator() {
        return notes.values().iterator();
    }

    int getTrackLength() { return notes.size(); }

    void addNote(int time, Note note) {
        if (!notes.containsKey(time))
            notes.put(time, new LinkedList<Note>());
        if (Objects.requireNonNull(notes.get(time)).size() == 1
            && Objects.requireNonNull(notes.get(time)).getFirst().getNoteType()
               == Note.NoteType.REST) {
            Objects.requireNonNull(notes.get(time)).get(0).hide();
            Objects.requireNonNull(notes.get(time)).remove((0));
        }
        Objects.requireNonNull(notes.get(time)).add(note);
    }

    void removeNote(int time, Note note) {
        if (notes.containsKey(time)) {
            if (Objects.requireNonNull(notes.get(time)).size() == 1)
                Objects.requireNonNull(notes.get(time)).getFirst()
                       .setNoteType(Note.NoteType.REST);
            else
                Objects.requireNonNull(notes.get(time)).remove(note);
        }
    }

    boolean noteAtPosition(int time) { return notes.containsKey(time); }

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

    TreeSet<Pair<Integer, LinkedList<Note>>> getMeasure(
      int measureNum, Fraction timeSignature) {
        int startPoint = measureNum * 192 * timeSignature.num
                         / timeSignature.den;
        int endPoint = startPoint + 192 * timeSignature.num
                                    / timeSignature.den;
        TreeSet<Pair<Integer, LinkedList<Note>>> ret
          = new TreeSet<>(new Comparator<Pair<Integer,
          LinkedList<Note>>>() {
            @Override
            public int compare(Pair<Integer, LinkedList<Note>> a,
                               Pair<Integer, LinkedList<Note>> b) {
                return a.first - b.first;
            }
        });

        for (int key : notes.keySet())
            if (key >= startPoint && key < endPoint)
                ret.add(new Pair<>(key, notes.get(key)));
            else if (key >= endPoint)
                break;

        return ret;
    }

    ImageView getClefImage() { return clefImage; }

    void setClefImage(ImageView clefImage) {
        this.clefImage = clefImage;
    }

    public ImageView getNumImage()              { return numImage; }

    public void setNumImage(ImageView numImage) { this.numImage = numImage; }

    public ImageView getDenImage()              { return denImage; }

    public void setDenImage(ImageView denImage) { this.denImage = denImage; }

    public ImageView[] getKeySigImages()        { return keySigImages; }

    public void setKeySigImages(ImageView[] keySigImages) {
        this.keySigImages = keySigImages;
    }

    void hideHead() {
        if (clefImage != null)
            ((RelativeLayout)clefImage.getParent()).removeView(clefImage);
        if (numImage != null)
            ((RelativeLayout)numImage.getParent()).removeView(numImage);
        if (denImage != null)
            ((RelativeLayout)denImage.getParent()).removeView(denImage);
        for (ImageView keySigImage : keySigImages)
            if (keySigImage != null)
                ((RelativeLayout)keySigImage.getParent())
                  .removeView(keySigImage);
    }
}
