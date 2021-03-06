package com.example.musicsheet;

import android.os.Environment;
import android.util.Pair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

class Score {
    private int tempo;
    private int startTime;
    private int measureCount;
    private ArrayList<Track> tracks;
    private Player player;
    private Thread playerThread;
    private int measureLength;
    private Fraction timeSignature;

    Score(Player player) {
        tempo = 120;
        measureLength = 0;
        startTime = 0;
        measureCount = 0;
        tracks = new ArrayList<>();
        this.player = player;
    }

    int getTempo()           { return tempo; }

    void setTempo(int tempo) { this.tempo = tempo; }

    int getMeasureLength()   { return measureLength; }

    void setTimeSignature(Fraction timeSignature) {
        int oldMeasureLength = measureLength;
        int oldMeasureCount = measureCount;

        if (this.timeSignature != null) {
            measureCount =
              measureCount * this.timeSignature.num * timeSignature.den
              / this.timeSignature.den / timeSignature.num;
            this.timeSignature.num = timeSignature.num;
            this.timeSignature.den = timeSignature.den;
        } else
            this.timeSignature = timeSignature;
        measureLength = 192 * timeSignature.num / timeSignature.den;

        if (oldMeasureCount != 0)
            if (measureCount * measureLength
                < oldMeasureCount * oldMeasureLength)
                ++measureCount;

        if (measureCount < oldMeasureCount && measureCount % 2 == 0)
            ++measureCount;

        for (Track track : tracks) {
            for (int i = 0; i < oldMeasureCount * oldMeasureLength;
                 i += oldMeasureLength) {
                Note rest = track.getNote(i, (byte)0);
                if (rest != null && rest.getNoteType() == Note.NoteType.REST
                    && rest.getDuration() == oldMeasureLength
                                             - rest.getDuration()
                                               % oldMeasureLength)
                    track.removeNoteHardcore(i, rest);
            }
            for (int i = 0; i < measureCount * measureLength;
                 i += measureLength) {
                int emptyLength = measureLength;
                TreeSet<Pair<Integer, LinkedList<Note>>> measure
                  = track.getMeasure((i / measureLength), measureLength);

                if (measure.size() > 0) {
                    Pair<Integer, LinkedList<Note>> first = measure.pollFirst();

                    if (first.first == i) {
                        if (measure.size() == 0 && first.second.size() == 1
                            && first.second.getFirst().getNoteType()
                               == Note.NoteType.REST)
                            first.second.getFirst().setDuration(measureLength);
                        emptyLength = 0;
                    } else
                        emptyLength = first.first - i;

                    if (measure.size() > 0) {
                        Pair<Integer, LinkedList<Note>> last
                          = measure.pollLast();

                        if (last.second.size() == 1)
                            //TODO: split notes with tie bars
                            if (last.second.getFirst().getNoteType()
                                == Note.NoteType.REST)
                                if (last.first + last.second.getFirst()
                                                            .getDuration()
                                    > i + measureLength)
                                    last.second.getFirst().setDuration(
                                      i + measureLength - last.first);
                        if (last.first + last.second.getFirst().getDuration()
                            < i + measureLength)
                            track
                              .addNote((last.first + last.second.getFirst()
                                                                .getDuration()),
                                       new Note(Note.NoteType.REST,
                                                (i + measureLength - last.first
                                                 - last.second.getFirst()
                                                              .getDuration()),
                                                (byte)0, (byte)0, (byte)0));
                    } else if (first.second.getFirst().getDuration()
                               < measureLength)
                        track
                          .addNote((i + first.second.getFirst().getDuration()),
                                   new Note(Note.NoteType.REST,
                                            (measureLength - first.second
                                              .getFirst().getDuration()),
                                            (byte)0, (byte)0, (byte)0));
                }
                if (emptyLength > 0)
                    track.addNote(i, new Note(Note.NoteType.REST, emptyLength,
                                              (byte)0, (byte)0, (byte)0));
            }
        }
    }

    Fraction getTimeSignature() { return timeSignature; }

    Track getTrack(int track)   { return tracks.get(track); }

    int getTrackCount()         { return tracks.size(); }

    void setTrackClef(int track, Track.Clef clef) {
        tracks.get(track).setClef(clef);
    }

    Track.Clef getTrackClef(int track) { return tracks.get(track).getClef(); }

    void setTrackInstrument(int track, byte instrument) {
        tracks.get(track).setInstrument(instrument);

        player.directWrite((byte)(0xC0 | track), instrument);
    }

    byte getTrackInstrument(int track) {
        return tracks.get(track).getInstrument();
    }

    void addTrack(Track track) {
        if (tracks.size() < 16) {
            tracks.add(track);
            for (int i = 0; i < measureCount * measureLength;
                 i += measureLength)
                addNote((tracks.size() - 1), i,
                        new Note(Note.NoteType.REST, measureLength,
                                 (byte)0, (byte)0, (byte)0));
        }
    }

    //velocity 127: good volume
    LinkedList<Note> addNote(int track, int timePosition, Note note) {
        tracks.get(track).addNote(timePosition, note);

        Iterator<Integer> it = tracks.get(track).getTimeIterator();

        while (it.hasNext()) {
            int tp = it.next();
            if (tp > timePosition && tp < timePosition + note.getDuration()) {
                if (tracks.get(track).checkNote(tp).getFirst()
                          .getNoteType() == Note.NoteType.REST) {
                    tracks.get(track).checkNote(tp).getFirst().hide();
                    it.remove();
                } else
                    break;
            }
        }

        timePosition += note.getDuration();

        LinkedList<Note> ret = new LinkedList<>();

        if (timePosition % measureLength != 0) {
            int remainder = (measureLength - timePosition % measureLength) / 2;
            int dur = 3;

            remainder /= 1.5;
            while (remainder != 0) {
                if (tracks.get(track).noteAtPosition(timePosition))
                    return ret;
                boolean addHere = remainder % 2 == 1;

                remainder /= 2;

                if (addHere) {
                    Note rest = new Note(Note.NoteType.REST, dur,
                                         (byte)0, (byte)0, (byte)0);

                    tracks.get(track).addNote(timePosition, rest);
                    ret.add(rest);
                    timePosition += dur;
                }
                dur *= 2;
            }
            return ret;
        } else
            return null;
    }

    Edit removeNote(int track, int timePosition, byte pitch) {
        Edit ret = new Edit(tracks.get(track).getNote(timePosition, pitch),
                            timePosition, track, Edit.EditType.REMOVE);

        tracks.get(track).removeNote(timePosition, ret.note);

        return ret;
    }

    Note getNote(int track, int timePosition, byte pitch) {
        return tracks.get(track).getNote(timePosition, pitch);
    }

    void addMeasure() {
        for (Track track : tracks)
            track.addNote((measureCount * measureLength),
                          new Note(Note.NoteType.REST, measureLength,
                                   (byte)0, (byte)0, (byte)0));
        ++measureCount;
    }

    int getMeasureCount() { return measureCount; }

    int durationAtTime(int track, int timePosition) {
        if (tracks.get(track).checkNote(timePosition).getFirst().getNoteType()
            != Note.NoteType.REST)
            return tracks.get(track).checkNote(timePosition)
                         .getFirst().getDuration();
        else
            return 0;
    }

    int availableDuration(int track, int timePosition) {
        Iterator<Integer> it = tracks.get(track).getTimeIterator();
        boolean open = true;
        int openDur = 0;

        while (open && it.hasNext()) {
            int tp = it.next();
            if (tp >= timePosition) {
                if (tracks.get(track).checkNote(timePosition).getFirst()
                          .getNoteType() != Note.NoteType.REST) {
                    open = false;
                    openDur = tp - timePosition;
                }
            }
        }

        return openDur;
    }

    ArrayList<Pair<Integer, LinkedList<Note>>> getMeasure(int track,
                                                          int measureNum) {
        if (measureNum >= measureCount)
            throw new IndexOutOfBoundsException();
        else {
            TreeSet<Pair<Integer, LinkedList<Note>>> times
              = tracks.get(track).getMeasure(measureNum, measureLength);
            ArrayList<Pair<Integer, LinkedList<Note>>> ret = new ArrayList<>();

            for (Pair<Integer, LinkedList<Note>> p : times)
                ret.add(new Pair<>(p.first - measureNum * measureLength,
                                   p.second));
            return ret;
        }
    }

    void play() {
        player.prepare(tempo, startTime, tracks);
        playerThread = new Thread(player);
        playerThread.start();
    }

    void pause() {
        playerThread.interrupt();
        try {
            playerThread.join();
            startTime = player.getStartTime();
        } catch (Exception ignored) {}
    }

    void resetPlayPos() { startTime = 0; }

    void save(String filename) {
        try {
            if (!filename.endsWith(".nl"))
                filename += ".nl";

            File out = new File((Environment.getExternalStorageDirectory()
                                 + "/" + filename));
            DataOutputStream os = new DataOutputStream(
              new FileOutputStream(out, (false)));

            os.writeInt(tempo);
            os.writeByte(timeSignature.num);
            os.writeByte(timeSignature.den);
            os.writeByte((byte)tracks.size());
            for (Track track : tracks) {
                os.writeInt(track.getClef().ordinal());
                os.writeByte(track.getKey());
                os.writeByte(track.getTransposition());
                os.writeByte(track.getInstrument());
                os.writeInt(track.getTrackLength());
                Iterator<Integer> tIt = track.getTimeIterator();
                Iterator<LinkedList<Note>> nIt = track.getNoteIterator();

                while (tIt.hasNext()) {
                    int t = tIt.next();
                    LinkedList<Note> nl = nIt.next();

                    os.writeInt(t);
                    os.writeInt(nl.size());

                    for (Note n : nl) {
                        os.writeInt(n.getNoteType().ordinal());
                        os.writeInt(n.getDuration());

                        os.writeByte(n.getPitch());
                        os.writeByte(n.getAccidental());
                        os.writeByte(n.getVelocity());
                    }
                }
            }
            os.flush();
            os.close();
        } catch (Exception ignored) {}
    }

    void load(String filename) {
        try {
            File in = new File((Environment.getExternalStorageDirectory()
                                + "/" + filename));
            DataInputStream is = new DataInputStream(new FileInputStream(in));
            int maxTime = 0;

            tempo = is.readInt();
            int num = is.readByte();
            int den = is.readByte();
            byte il = is.readByte();
            tracks = new ArrayList<>(il);
            for (int i = 0; i < il; ++i) {
                Track.Clef clef = Track.Clef.values()[is.readInt()];
                int key = is.readByte();
                int transposition = is.readByte();
                tracks.add(new Track(is.readByte(), clef, key, transposition));
                player.directWrite((byte)(0xC0 | i),
                                   tracks.get(i).getInstrument());

                int tl = is.readInt();

                for (int j = 0; j < tl; ++j) {
                    int time = is.readInt();

                    int notesAtTime = is.readInt();

                    for (int k = 0; k < notesAtTime; ++k) {
                        Note.NoteType noteType
                          = Note.NoteType.values()[is.readInt()];

                        int duration = is.readInt();
                        if (time + duration > maxTime)
                            maxTime = time + duration;

                        byte pitch = is.readByte();
                        byte accidental = is.readByte();
                        byte velocity = is.readByte();
                        tracks.get(i).addNote(time, new Note(noteType, duration,
                                                             pitch, accidental,
                                                             velocity));
                    }
                }
            }
            is.close();
            measureCount = maxTime * den / 192 / num;
            if (maxTime % 192 * num / den != 0)
                ++measureCount;
            setTimeSignature(new Fraction(num, den));
        } catch (Exception ignored) {}
    }
}
