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
    private Track[] tracks;
    private Player player;
    private Thread playerThread;

    Score(Player player) {
        tempo = 60;
        startTime = 0;
        measureCount = 0;
        tracks = new Track[1];
        tracks[0] = new Track((byte)0);
        this.player = player;
    }

    int getTempo() { return tempo; }

    void setTempo(int tempo) { this.tempo = tempo; }

    //velocity 127: good volume
    void addNote(int track, int timePosition, int duration, byte pitch, byte velocity) {
        tracks[track].addNote(timePosition, new Note(Note.NoteType.MELODIC,
                                                     duration, pitch, velocity));
        tracks[track].addNote(timePosition + duration, new Note(Note.NoteType.REST,
                                                                      0, (byte)0,
                                                                      (byte)0));
    }

    void deleteNote(int track, int timePosition, byte pitch) {
        tracks[track].removeNote(timePosition, tracks[track].getNote(timePosition, pitch));
    }

    void addMeasure(Fraction timeSignature) {
        int measureSize = 192 * timeSignature.num / timeSignature.den;

        for (Track track : tracks)
            track.addNote(measureCount * measureSize, new Note(Note.NoteType.REST,
                                                                     measureSize,
                                                                     (byte)0, (byte)0));
        ++measureCount;
    }

    int getMeasureCount() { return measureCount; }

    int durationAtTime(int track, int timePosition) {
        if (tracks[track].checkNote(timePosition).getFirst().getNoteType() != Note.NoteType.REST)
            return tracks[track].checkNote(timePosition).getFirst().getDuration();
        else
            return 0;
    }

    ArrayList<Pair<Integer, LinkedList<Note>>> getMeasure(int track, int measureNum, Fraction timeSignature) {
        if (measureNum >= measureCount)
            throw new IndexOutOfBoundsException();
        else {
            TreeSet<Pair<Integer, LinkedList<Note>>> times
                    = tracks[track].getMeasure(measureNum, timeSignature);
            ArrayList<Pair<Integer, LinkedList<Note>>> ret = new ArrayList<>();

            for (Pair<Integer, LinkedList<Note>> p : times) {
                ret.add(new Pair<>(
                        p.first - measureNum * 192 * timeSignature.num / timeSignature.den,
                        p.second));
            }

            return ret;
        }
    }

    void play() {
        if (!player.running) {
            player.prepare(tempo, startTime, tracks);
            playerThread = new Thread(player);
            playerThread.start();
        }
    }

    void pause() {
        if (player.running) {
            playerThread.interrupt();
            try {
                playerThread.join();
                startTime = player.getStartTime();
            } catch (Exception ignored) {}
        }
    }

    void resetPlayPos() { startTime = 0; }

    void save(String filename) {
        try {
            if (!filename.endsWith(".nl"))
                filename += ".nl";

            File out = new File((Environment.getExternalStorageDirectory() + "/" + filename));
            DataOutputStream os = new DataOutputStream(new FileOutputStream(out, false));

            os.writeInt(tempo);
            os.writeByte((byte)tracks.length);
            for (Track track : tracks) {
                os.writeByte(track.getInstrument());
                os.writeInt(track.getTrackLength());
                Iterator<Integer> tIt = track.getTimeIterator();
                Iterator<LinkedList<Note>> nIt = track.getNoteIterator();
                while (tIt.hasNext()) {
                    int t = tIt.next();
                    LinkedList<Note> nl = nIt.next();

                    for (Note n : nl) {
                        os.writeInt(t);
                        os.writeInt(n.getNoteType().ordinal());
                        os.writeInt(n.getDuration());

                        os.writeByte(n.getPitch());
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
            File in = new File((Environment.getExternalStorageDirectory() + "/" + filename));
            DataInputStream is = new DataInputStream(new FileInputStream(in));
            byte[] event = new byte[2];

            int maxTime = 0;

            tempo = is.readInt();
            byte il = is.readByte();
            tracks = new Track[il];
            for (int i = 0; i < il; ++i) {
                tracks[i] = new Track(is.readByte());
                event[0] = (byte) (0xC0 | i);  // 0xC0 = program change, 0x0X = channel X
                event[1] = tracks[i].getInstrument();
                player.directWrite(event);

                int tl = is.readInt();

                for (int j = 0; j < tl; ++j) {
                    int time = is.readInt();

                    Note.NoteType noteType = Note.NoteType.values()[is.readInt()];

                    int duration = is.readInt();
                    if (time + duration > maxTime)
                        maxTime = time + duration;

                    byte pitch    = is.readByte();
                    byte velocity = is.readByte();
                    tracks[i].addNote(time, new Note(noteType, duration, pitch, velocity));
                }
            }
            is.close();
            measureCount = maxTime / 192;
            if (maxTime % 192 != 0)
                ++measureCount;
        } catch (Exception ignored) {}
    }
}
