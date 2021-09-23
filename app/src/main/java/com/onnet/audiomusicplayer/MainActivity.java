package com.onnet.audiomusicplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.onnet.audiomusicplayer.adapters.PlaylistAdapter;
import com.onnet.audiomusicplayer.lib.PreferenceHandler;
import com.onnet.audiomusicplayer.lib.Song;
import com.onnet.audiomusicplayer.services.MusicService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class MainActivity extends AppCompatActivity {

    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 41 ;

    public static MusicService musicSrv;
    private Intent playIntent;
    public static boolean musicBound = false;
    private ArrayList<Song> songList;

    ListView songListView;
    LinearLayout llError;
    Button btnAddPlaylist;

    TextView tvError;

    ArrayList<String> playList;
    PlaylistAdapter playlistAdapter;
    public static boolean playbackPaused;
    public final seekbarFragment seekbarfragment = new seekbarFragment();

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceHandler.init(this);
        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container_view, seekbarfragment);
            fragmentTransaction.commit();
        }
          setLayout();
    }

    public void setLayout()
    {

        llError = findViewById(R.id.errorlayout);
        songListView = findViewById(R.id.song_list);

        btnAddPlaylist = findViewById(R.id.addplaylist);
        tvError = findViewById(R.id.error);

        songList = new ArrayList<>();
        getSongList();
        Collections.sort(songList, Comparator.comparing(Song::getTitle));
        PreferenceHandler.savePlayList("모든 노래", songList);
        fetchPlayList();
        btnAddPlaylist.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddPlayListActivity.class);
            startActivity(intent);
        });


        songListView.setOnItemClickListener((adapterView, view, i, l) -> {


            Intent intent = new Intent(MainActivity.this, ViewPlayListActivity.class);
            intent.putExtra("name", playList.get(i));
            startActivity(intent);

        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == READ_STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               setLayout();
            } else {
                try {
                    requestPermissionForReadExtertalStorage(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermissionForReadExtertalStorage(this))
        fetchPlayList();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
                songList.add(new Song(thisId, thisTitle, thisArtist, false));
            } while (musicCursor.moveToNext());
        }
    }

    private final ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicSrv = binder.getService();
            if (playlistAdapter != null) {
                playlistAdapter.setMusicService(musicSrv);
            }

            musicSrv.setActivity(MainActivity.this);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    public boolean checkPermissionForReadExtertalStorage(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public void requestPermissionForReadExtertalStorage(Context context) throws Exception {
        try {
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        seekbarfragment.seekBarHandler.removeCallbacks(seekbarfragment.seekRunnable);
    }


    public void fetchPlayList() {
        playList = PreferenceHandler.getAllKeys();
        if (playList.size() == 0) {
            tvError.setVisibility(View.VISIBLE);
        } else {
            Collections.sort(playList);
            ArrayAdapter<String> aa = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playList);
            playlistAdapter = new PlaylistAdapter(this, playList, null);
            if(musicSrv != null){
                playlistAdapter.setMusicService(musicSrv);
            }

            songListView.setAdapter(playlistAdapter);
        }
    }

}
