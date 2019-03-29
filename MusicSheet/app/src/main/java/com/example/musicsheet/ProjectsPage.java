package com.example.musicsheet;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class ProjectsPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_projects_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //demandHelp();

       // ListView projectList= (ListView)findViewById(R.id.projects);

        ArrayList<String> projectNames = new ArrayList<>();

        projectNames.add("Song 1");
        projectNames.add("Song 2");
        projectNames.add("Song 3");
        projectNames.add("Song 4");
        projectNames.add("Song 5");
        projectNames.add("Song 6");
        projectNames.add("Song 7");
        projectNames.add("Song 8");
        projectNames.add("Song 9");
        projectNames.add("Song 10");
        projectNames.add("Song 11");
        projectNames.add("Song 12");
        projectNames.add("Song 13");
        projectNames.add("Song 14");
        projectNames.add("Song 15");
        projectNames.add("Song 16");


        //private void generateListContent() {
        //    for(int i = 0; i < 15; i++){
        //        projectNames.add("Song " + i);
        //    }
        //}

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, projectNames);

        //projectList.setAdapter(arrayAdapter);

       /* projectList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(ProjectsPage.this, MusicSheet.class));
            }
        });*/
    }

   /* private void demandHelp(){
        Button btnHelp = (Button)findViewById(R.id.button3);
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProjectsPage.this, ProjectsPage.this.getResources().getString(R.string.help_text), Toast.LENGTH_LONG).show();
            }
        });
    }*/

}
