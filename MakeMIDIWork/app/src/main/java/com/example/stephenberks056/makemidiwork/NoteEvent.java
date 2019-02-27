package com.example.stephenberks056.makemidiwork;

import org.billthefarmer.mididriver.MidiDriver;

public class NoteEvent {
    enum EventType { START, STOP, REST };

    private MidiDriver midiDriver;
    private EventType eventType;
    private int duration;
    private byte velocity;
    private byte pitch;
    private byte channel;

    NoteEvent(MidiDriver midiDriver, EventType eventType,
              int duration, byte velocity, byte pitch, byte channel) {
        this.midiDriver = midiDriver;
        this.eventType  = eventType;
        this.duration   = duration;
        this.velocity   = velocity;
        this.pitch      = pitch;
        this.channel    = channel;
    }

    public EventType getEventType() { return eventType; }

    public int getDuration() { return duration; }

    public byte getVelocity() { return velocity; }
    public byte getPitch()    { return pitch; }
    public byte getChannel()  { return channel; }

    public void play() {
        byte command = 0;

        switch (eventType) {
        case START:
            command = (byte)0x90;
            break;
        case STOP:
            command = (byte)0x80;
            break;
        case REST:
            return;
        }

        byte[] event = new byte[3];
        event[0] = (byte)(command | channel);
        event[1] = pitch;
        event[2] = velocity;

        // Send the MIDI event to the synthesizer.
        midiDriver.write(event);
    }
}
