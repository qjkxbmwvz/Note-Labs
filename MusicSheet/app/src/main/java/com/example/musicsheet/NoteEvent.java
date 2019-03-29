package com.example.musicsheet;

import org.billthefarmer.mididriver.MidiDriver;

public class NoteEvent {
    enum EventType { START, STOP }

    private MidiDriver midiDriver;
    private EventType eventType;
    private byte channel;
    private byte pitch;
    private byte velocity;

    NoteEvent(MidiDriver midiDriver, EventType eventType, byte channel, byte pitch, byte velocity) {
        this.midiDriver = midiDriver;
        this.eventType  = eventType;
        this.channel    = channel;
        this.pitch      = pitch;
        this.velocity   = velocity;
    }

    NoteEvent(MidiDriver midiDriver, EventType eventType, byte channel, Note note) {
        this.midiDriver = midiDriver;
        this.eventType  = eventType;
        this.channel    = channel;
        this.pitch      = note.getPitch();
        this.velocity   = note.getVelocity();
    }

    public EventType getEventType() { return eventType; }

    public byte getChannel()  { return channel; }
    public byte getPitch()    { return pitch; }
    public byte getVelocity() { return velocity; }

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
}
