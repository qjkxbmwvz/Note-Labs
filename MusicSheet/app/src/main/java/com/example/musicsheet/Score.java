package com.example.musicsheet;

import android.os.Environment;
import android.util.Pair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Time;
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

    //velocity 127: good volume
    void addNote(int track, int timePosition, int duration, byte pitch, byte velocity) {
        tracks[track].addNote(timePosition, new Note(Note.NoteType.MELODIC,
                                                     duration, pitch, velocity));
        tracks[track].addNote(timePosition + duration, new Note(Note.NoteType.REST,
                                                                      0, (byte)0,
                                                                      (byte)0));
    }

    void addMeasure(Fraction timeSignature) {
        int measureSize = 192 * timeSignature.num / timeSignature.den;

        for (Track track : tracks)
            track.addNote(measureCount * measureSize, new Note(Note.NoteType.REST,
                                                                     measureSize,
                                                                     (byte)0, (byte)0));
        ++measureCount;
    }

    int[] getMeasure(int track, int measureNum, Fraction timeSignature) {
        if (measureNum >= measureCount)
            throw new IndexOutOfBoundsException();
        else {
            TreeSet<Integer> times = tracks[track].getMeasure(measureNum, timeSignature);
            int[] ret = new int[times.size()];
            Iterator<Integer> it = times.iterator();

            for (int i = 0; i < ret.length; ++i)
                ret[i] = it.next() - measureNum * 192 * timeSignature.num / timeSignature.den;

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

    void save() {
        try {
            File out = new File(Environment.getExternalStorageDirectory() + "/test.nl");
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

    void load() {
        try {
            File in = new File(Environment.getExternalStorageDirectory() + "/test.nl");
            DataInputStream is = new DataInputStream(new FileInputStream(in));
            byte[] event = new byte[2];

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

                    byte pitch    = is.readByte();
                    byte velocity = is.readByte();
                    tracks[i].addNote(time, new Note(noteType, duration, pitch, velocity));
                }
            }
            is.close();
        } catch (Exception ignored) {}
    }
}
