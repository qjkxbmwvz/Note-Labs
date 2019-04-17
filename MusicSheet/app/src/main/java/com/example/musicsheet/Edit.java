package com.example.musicsheet;

class Edit {
    enum EditType {ADD, REMOVE}

    Note note;
    int time, staff;
    EditType editType;

    Edit(Note note, int time, int staff, EditType editType) {
        this.note = note;
        this.time = time;
        this.staff = staff;
        this.editType = editType;
    }
}
