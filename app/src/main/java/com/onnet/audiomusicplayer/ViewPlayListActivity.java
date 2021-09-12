package com.onnet.audiomusicplayer;

import android.os.Build;
import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


public class ViewPlayListActivity extends AppCompatActivity {

    private String TAG = this.getClass().getSimpleName();

    ListView lvAudioFiles;

    String playListName;

    ArrayList<Song> songsList;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewplaylist);

        playListName = getIntent().getStringExtra("name");

        lvAudioFiles = findViewById(R.id.songslist);

        songsList = PreferenceHandler.getPlayList(playListName);

        PlaylistAdapter playlistAdapter = new PlaylistAdapter(this, null, songsList);
        lvAudioFiles.setAdapter(playlistAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
