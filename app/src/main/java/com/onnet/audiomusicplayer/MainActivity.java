package com.onnet.audiomusicplayer;

import android.app.Activity;
import android.app.PendingIntent;
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
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements MediaPlayerControl {

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

    TextView tvError, tvSongTitle;
    ImageView ivPlayPause, ivNext, ivPrev;
    SeekBar seekBar;
    TextView tvEndTime, tvStartTime;


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivPlayPause = findViewById(R.id.playpause);
        ivNext = findViewById(R.id.next);
        ivPrev = findViewById(R.id.prev);
        seekBar = findViewById(R.id.seekbar);
        tvStartTime = findViewById(R.id.starttime);
        tvEndTime = findViewById(R.id.endtime);

        tvSongTitle = findViewById(R.id.songname);
        tvError = findViewById(R.id.error);
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
                Intent intent = new Intent(MainActivity.this, AddPlayListActivity.class);
                startActivity(intent);
            }
        });

//        displayAllSongsList();
//        fetchPlayList();

        seekBarHandler.postDelayed(seekRunnable, 1000);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(musicSrv.isPng() && fromUser){
                    musicSrv.seek(progress);
                }
            }
        });

        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                Intent intent = new Intent(MainActivity.this, ViewPlayListActivity.class);
                intent.putExtra("name", playList.get(i));
                startActivity(intent);

            }
        });


        ivPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicSrv.isPng()) {
                    musicSrv.pausePlayer();
                    ivPlayPause.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    musicSrv.go();
                    ivPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                }
            }
        });

        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNext();
            }
        });

        ivPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPrev();
            }
        });
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
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        fetchPlayList();
        if (paused) {
            paused = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    Handler seekBarHandler = new Handler();

    public void updateController() {

        displayNotification();
        long millis = musicSrv.getDur();
        ivPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        seekBar.setMax((int) millis);
        String ms = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        tvEndTime.setText(ms);
        Log.i(TAG, "showController: duration: " + musicSrv.getDur() + " " + ms);
    }

    Runnable seekRunnable = new Runnable() {
        @Override
        public void run() {

            if (musicSrv.isPng()) {
                long currentPos = musicSrv.getPosn();
                long duration = musicSrv.getDur();
                String durMS = convertMillisToMS(duration);
                String ms = convertMillisToMS(currentPos);
                tvStartTime.setText(ms);
                tvEndTime.setText(durMS);
                seekBar.setMax((int) duration);
                seekBar.setProgress((int) currentPos);
                ivPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                tvSongTitle.setText(musicSrv.getSongName());
//                Log.i(TAG, "run: " + currentPos + " MS: " + ms);
            } else {
                ivPlayPause.setImageResource(android.R.drawable.ic_media_play);
            }

            seekBarHandler.postDelayed(this, 1000);

        }
    };

    public String convertMillisToMS(long millis) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }


    private void playNext() {
        musicSrv.playNext();
        if (playbackPaused) {
            playbackPaused = false;
        }
    }

    private void playPrev() {
        musicSrv.playPrev();
        if (playbackPaused) {
            playbackPaused = false;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getSongList() {
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = contentResolver.query(songUri, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
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
            if (playlistAdapter != null) {
                playlistAdapter.setMusicService(musicSrv);
            }

            musicSrv.setActivity(MainActivity.this);
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
            /*case R.id.home:
                break;*/
            case R.id.create_playlist:
//                setContentView(R.layout.main_allsongs);

                Intent intent = new Intent(MainActivity.this, AddPlayListActivity.class);
                startActivity(intent);

                /*ListView songView = findViewById(R.id.song_list);
                songView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                songList = new ArrayList<>();
                getSongList();
                Collections.sort(songList, Comparator.comparing(Song::getTitle));
                SongAdapter songAdt = new SongAdapter(this, songList);
                songView.setAdapter(songAdt);*/

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
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "from activity");
        seekBarHandler.removeCallbacks(seekRunnable);
        Intent serviceIntent = new Intent(this, MusicService.class);
        stopService(serviceIntent);
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
        Log.i(TAG, "start: ");
        musicSrv.go();
        controller.show();
    }

    @Override
    public int getDuration() {
        Log.i(TAG, "getDuration: ");
        if (musicSrv != null && musicBound && musicSrv.isPng())
            return musicSrv.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        Log.i(TAG, "getCurrentPosition: ");
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

    ArrayList<String> playList;
    PlaylistAdapter playlistAdapter;

    public void fetchPlayList() {
        playList = PreferenceHandler.getAllKeys();
        if (playList.size() == 0) {
            tvError.setVisibility(View.VISIBLE);
//            songListView.setVisibility(View.GONE);
        } else {
            Collections.sort(playList);
            ArrayAdapter<String> aa = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playList);
            playlistAdapter = new PlaylistAdapter(this, playList, null);
            if(musicSrv != null){
                playlistAdapter.setMusicService(musicSrv);
            }

            playlistAdapter.setOnPlayClickListener(new PlaylistAdapter.OnPlayClicklistener() {
                @Override
                public void onPlayClick(int position) {
                    int duration = musicSrv.getDur();
                    Log.i(TAG, "onPlayClick: " + duration);
                }
            });
            songListView.setAdapter(playlistAdapter);
        }
    }


    String CHANNEL_ID = "MusicPlayer";
    int notificationId = 1234;

    public void displayNotification() {

        Log.i(TAG, "displayNotification: called");
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Onnet Music Player")
                .setContentText("Audio playing")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, builder.build());
    }

}
