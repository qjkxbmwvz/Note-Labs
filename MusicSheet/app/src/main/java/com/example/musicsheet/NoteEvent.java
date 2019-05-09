package com.example.musicsheet;

import org.billthefarmer.mididriver.MidiDriver;

class NoteEvent {
    enum EventType { START, STOP }

    private MidiDriver midiDriver;
    private EventType eventType;
    private byte channel;
    private Note note;

    NoteEvent(MidiDriver midiDriver, EventType eventType, byte channel, Note note) {
        this.midiDriver = midiDriver;
        this.eventType  = eventType;
        this.channel    = channel;
        this.note       = note;
    }

    void play() {
        byte command = 0;

        switch (eventType) {
        case START:
            command = (byte)0x90;
            break;
        case STOP:
            command = (byte)0x80;
            break;
        }

        byte[] event = new byte[3];

        event[0] = (byte)(command | channel);
        event[1] = (byte)(note.getPitch() + note.getAccidental());
        event[2] = note.getVelocity();

        midiDriver.write(event);
    }

    EventType getEventType() { return eventType; }

    byte getPitch() { return note.getPitch(); }

    void setPitch(byte pitch) { note.setPitch(pitch); }

    Note getNote() { return note; }
}
