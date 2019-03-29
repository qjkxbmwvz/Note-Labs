package com.example.musicsheet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.Iterator;
import java.util.LinkedList;

class TimePosition {
    private int time;
    private LinkedList<NoteEvent> noteEvents;

    TimePosition(int time) {
        this.time = time;
        noteEvents = new LinkedList<>();
    }

    void setTime(int time) { this.time = time; }

    int getTime() { return time; }

    Iterator<NoteEvent> getNoteEventIterator() { return noteEvents.iterator(); }

    void addNote(NoteEvent noteEvent) { noteEvents.add(noteEvent); }

    public static class MainActivity extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            goToProjects();
            goToMusicSheet();
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            return (id == R.id.action_settings) || super.onOptionsItemSelected(item);
        }

        private void goToProjects(){
            Button btnNew = (Button)findViewById(R.id.button2);
            btnNew.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, ProjectsPage.class));
                }
            });
        }

        private void goToMusicSheet(){
            Button btnLoad = (Button)findViewById(R.id.button);
            btnLoad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, MusicSheet.class));
                }
            });
        }
    }
}
