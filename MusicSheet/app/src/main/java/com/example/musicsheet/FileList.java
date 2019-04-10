package com.example.musicsheet;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class FileList extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
        }, 100);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView projectList = findViewById(R.id.projects);
        ArrayList<String> projectNames = new ArrayList<>();
        File dir = new File(Environment.getExternalStorageDirectory() + "/");

        for (File f : dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) { return filename.endsWith(".nl"); }
        }))
            projectNames.add(f.getName());

        final ArrayAdapter<String> arrayAdapter
                = new ArrayAdapter<>((this), android.R.layout.simple_list_item_1, projectNames);

        projectList.setAdapter(arrayAdapter);
        projectList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(FileList.this, MusicSheet.class);
                intent.putExtra("filename", arrayAdapter.getItem(i));
                startActivity(intent);
            }
        });
    }
}
