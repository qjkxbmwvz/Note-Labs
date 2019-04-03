package com.example.musicsheet;

import android.os.Environment;
import android.util.Pair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;

class Score {
    private int tempo;
    private Track[] tracks;
    private Player player;
    private Thread playerThread;

    Score(Player player) {
        tracks = new Track[1];
        tracks[0] = new Track((byte)0);
        this.player = player;
    }

    //velocity 127: good volume
    void addNote(int track, int timePosition, int duration, byte pitch, byte velocity) {
        tracks[track].addNote(timePosition, new Note(Note.NoteType.MELODIC,
                                                     duration, pitch, velocity));
    }

    void play() {
        if (!player.running) {
            player.prepare(tempo, 0, tracks);
            playerThread = new Thread(player);
            playerThread.start();
        } else {
            playerThread.interrupt();
            try {
                playerThread.join();
            } catch (Exception ignored) {}
        }
    }

    void save() {
        try {
            File out = new File(Environment.getExternalStorageDirectory() + "/test.nl");
            DataOutputStream os = new DataOutputStream(new FileOutputStream(out, false));

            os.writeInt(tempo);
            os.writeByte((byte)tracks.length);
            for (Track track : tracks) {
                os.writeByte(track.getInstrument());
                os.writeInt(track.getTrackLength());
                Iterator<Pair<Integer, Note>> it = track.getNoteIterator();
                while (it.hasNext()) {
                    Pair<Integer, Note> p = it.next();
                    os.writeInt(p.first);
                    os.writeInt(p.second.getNoteType().ordinal());
                    os.writeInt(p.second.getDuration());

                    os.writeByte(p.second.getPitch());
                    os.writeByte(p.second.getVelocity());
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
