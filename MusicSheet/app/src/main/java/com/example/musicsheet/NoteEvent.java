package com.example.musicsheet;

import org.billthefarmer.mididriver.MidiDriver;

class NoteEvent {
    enum EventType {START, STOP}

    private MidiDriver midiDriver;
    private EventType eventType;
    private byte channel;
    private byte pitch;
    private byte velocity;
    private Note note;

    NoteEvent(MidiDriver midiDriver, EventType eventType,
              byte channel, Note note) {
        this.midiDriver = midiDriver;
        this.eventType = eventType;
        this.channel = channel;
        this.note = note;
        this.pitch = (byte)(note.getPitch() + note.getAccidental());
        this.velocity = note.getVelocity();
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
        event[1] = pitch;
        event[2] = velocity;

        midiDriver.write(event);
    }

    EventType getEventType()  { return eventType; }

    byte getPitch()           { return pitch; }

    void setPitch(byte pitch) { this.pitch = pitch; }

    Note getNote()            { return note; }
}
