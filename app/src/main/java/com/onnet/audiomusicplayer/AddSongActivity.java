package com.onnet.audiomusicplayer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

public class AddSongActivity extends AppCompatActivity {
    private String TAG = this.getClass().getSimpleName();

    private ArrayList<Song> songsList;
    ListView lvAudioFiles;
    LinearLayout llFirst;
    Button btnNext, btnDone;
    EditText etPlaylistName;
    String playListName;
    HashSet<Long> orginalIDs;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song);
        playListName = getIntent().getStringExtra("name");
        lvAudioFiles = findViewById(R.id.audiolistview);
        llFirst = findViewById(R.id.firstlayout);
        btnDone = findViewById(R.id.submit);
        etPlaylistName = findViewById(R.id.playlistname);

        ArrayList<Song> originalSongs = PreferenceHandler.getPlayList( playListName);
        orginalIDs = new HashSet<>();
        for(Song s : originalSongs)
            orginalIDs.add(s.getId());

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Song> selectedSongs = ((SongAdapter) lvAudioFiles.getAdapter()).getSelectedItems();
                selectedSongs.addAll(originalSongs);
                if (selectedSongs.size() == 0) {
                    Utils.showToast(getApplicationContext(), "Please select, atleast one song");
                } else {
                    PreferenceHandler.savePlayList(playListName, selectedSongs);
                    Log.i(TAG, "onClick: total no of selected files: " + selectedSongs.size());
                    Utils.showToast(getApplicationContext(), "Playlist " + playListName + " created.");
                    finish();
                }
            }
        });

        displayAllSongsList();

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

    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void displayAllSongsList() {
        songsList = new ArrayList<>();
        getSongList();
        Collections.sort(songsList, Comparator.comparing(Song::getTitle));
        SongAdapter songAdapter = new SongAdapter(this, songsList);
        lvAudioFiles.setAdapter(songAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getSongList() {
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = contentResolver.query(songUri, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                if(orginalIDs.contains(thisId))
                songsList.add(new Song(thisId, thisTitle, thisArtist, true));
                else
                    songsList.add(new Song(thisId, thisTitle, thisArtist, false));
            } while (musicCursor.moveToNext());
        }
    }

}
