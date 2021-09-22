package com.onnet.audiomusicplayer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.onnet.audiomusicplayer.adapters.songPlaylistAdapter;
import com.onnet.audiomusicplayer.lib.PreferenceHandler;
import com.onnet.audiomusicplayer.lib.Song;

import java.util.ArrayList;


public class ViewPlayListActivity extends AppCompatActivity {

    ListView songFiles;

    String playListName;

    ArrayList<Song> songsList;
    Button addbtn;


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewplaylist);
        addbtn = findViewById(R.id.addsongs);
        playListName = getIntent().getStringExtra("name");
        if(!playListName.equals("모든 노래"))
            addbtn.setVisibility(View.VISIBLE);
        addbtn.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext() , AddSongActivity.class);
            intent.putExtra("name", playListName);
            startActivity(intent);
        });


        songFiles = findViewById(R.id.songslist);
        songsList = PreferenceHandler.getPlayList(playListName);

        songPlaylistAdapter playlistAdapter;
        playlistAdapter = new songPlaylistAdapter(this, playListName, songsList);
        songFiles.setAdapter(playlistAdapter);

    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        songFiles = findViewById(R.id.songslist);
        songsList = PreferenceHandler.getPlayList(playListName);
        songPlaylistAdapter playlistAdapter;

        playlistAdapter = new songPlaylistAdapter(this, playListName, songsList);

        songFiles.setAdapter(playlistAdapter);
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