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
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class PlayerActivity extends AppCompatActivity implements MediaPlayerControl {

    private String TAG = this.getClass().getSimpleName();

    private boolean paused;
    private boolean playbackPaused;
    private MediaController controller;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    private ArrayList<Song> songList;
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 41;
    ListView songListView;
    LinearLayout llError;
    Button btnAddPlaylist;


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songListView = findViewById(R.id.song_list);
        llError = findViewById(R.id.errorlayout);
        btnAddPlaylist = findViewById(R.id.addplaylist);

        PreferenceHandler.init(this);

        if (!checkPermissionForReadExtertalStorage(this)) {
            try {
                requestPermissionForReadExtertalStorage(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        btnAddPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        setController();
//        displayAllSongsList();
        fetchPlayList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            setController();
            paused = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setController() {
        controller = new MediaController(this);
        controller.setPrevNextListeners(v -> playNext(), v -> playPrev());
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
            ((Activity) controller.getContext()).finish();
        return super.dispatchKeyEvent(event);
    }


    private void playNext() {
        musicSrv.playNext();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    private void playPrev() {
        musicSrv.playPrev();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
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
            Log.i(TAG, "onServiceConnected: called");
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicSrv = binder.getService();
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected: called");
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

    public void songsPicked(View view) {
        ListView songView = findViewById(R.id.song_list);
        long[] viewItems = songView.getCheckedItemIds();
        Log.d("fun", viewItems.toString());
        for (int i = 0; i < viewItems.length; i++) {
        }
      /*  if(musicSrv.isPng() && Integer.parseInt(view.getTag().toString()) == musicSrv.getPosn())
            controller.show();
        else
        {
                musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
                musicSrv.playSong();
        }
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);*/
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                break;
            case R.id.create_playlist:
//                setContentView(R.layout.main_allsongs);
                ListView songView = findViewById(R.id.song_list);
                songView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                songList = new ArrayList<>();
                getSongList();
                Collections.sort(songList, Comparator.comparing(Song::getTitle));
                SongAdapter songAdt = new SongAdapter(this, songList);
                songView.setAdapter(songAdt);
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv = null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: called : playerIntent: " + playIntent + " Music connection: " + musicConnection);
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicSrv.pausePlayer();
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public void start() {
        musicSrv.go();
        controller.show();
    }

    @Override
    public int getDuration() {
        if (musicSrv != null && musicBound && musicSrv.isPng())
            return musicSrv.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicSrv != null && musicBound && musicSrv.isPng())
            return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public boolean isPlaying() {
        return musicSrv != null && musicBound && musicSrv.isPng();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }


    public void displayAllSongsList() {
        songList = new ArrayList<>();
        getSongList();
        Collections.sort(songList, Comparator.comparing(Song::getTitle));
        SongAdapter songAdt = new SongAdapter(this, songList);
        songListView.setAdapter(songAdt);

        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                musicSrv.setSongsList(songList, i);
                musicSrv.playSong();
                Song song = songList.get(i);
                song.getArtist();
                long id = song.getId();
            }
        });
    }

    
    public void fetchPlayList(){
        ArrayList<String> playList = PreferenceHandler.getAllKeys();
        if(playList.size() == 0){
            llError.setVisibility(View.VISIBLE);
            songListView.setVisibility(View.GONE);
        }else{
            ArrayAdapter<String> aa = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, playList);
            songListView.setAdapter(aa);
        }
    }


}
