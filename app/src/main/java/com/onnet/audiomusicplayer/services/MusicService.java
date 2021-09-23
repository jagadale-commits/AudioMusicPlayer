package com.onnet.audiomusicplayer.services;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.onnet.audiomusicplayer.lib.CreateNotification;
import com.onnet.audiomusicplayer.MainActivity;
import com.onnet.audiomusicplayer.lib.PreferenceHandler;
import com.onnet.audiomusicplayer.lib.Song;
import com.onnet.audiomusicplayer.seekbarFragment;

import java.util.ArrayList;
import java.util.HashSet;

import static com.onnet.audiomusicplayer.lib.CreateNotification.createNotification;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private final String TAG = this.getClass().getSimpleName();

    public static String songTitle = "";
    private MediaPlayer player;
    public static ArrayList<Song> songs;
    public static int songPosn;
    private final IBinder musicBind = new MusicBinder();
    MainActivity mainActivity;
    

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: called");
        songPosn = 0;
        initMusicPlayer();
        createNotificationChannel();
        registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
        startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
    }
    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        stopForeground(true);
        player.stop();
        stopSelf();
        super.onTaskRemoved(rootIntent);

    }

        @Override
    public void onDestroy() {
        Log.d(TAG,"from ser");
        super.onDestroy();
    }

    public void initMusicPlayer() {

        player = new MediaPlayer();
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);

    }

    public void setSongsList(ArrayList<Song> theSongs, int pos) {
        songs = theSongs;
        songPosn = pos;
    }


    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: called");
        return musicBind;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    public void playSong() {
       if(songs!=null) {
           playstop();
           player.reset();
           ArrayList<Song> originalSongs = PreferenceHandler.getPlayList("모든 노래");
           HashSet<Long> Allsong = new HashSet<>();
           for (Song s : originalSongs)
               Allsong.add(s.getId());
           Song playSong = songs.get(songPosn);
           songTitle = playSong.getTitle();
           long currSong = playSong.getId();
           createNotification(this, songs.get(songPosn), android.R.drawable.ic_media_pause);

           if (Allsong.contains(currSong)) {
               try {
                   Uri trackUri = ContentUris.withAppendedId(
                           android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                           currSong);
                   player.setDataSource(getApplicationContext(), trackUri);
                   player.prepareAsync();
               } catch (Exception e) {
                   Log.e("MUSIC SERVICE", "Error setting data source", e);
               }
           } else {
               songs.remove(songPosn);
               playNext();
           }
       }

    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        if (player.getCurrentPosition() > 0) {
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    public void setSong(int songIndex) {
        songPosn = songIndex;
    }

    public void setActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        if (mainActivity != null) {
            mainActivity.seekbarfragment.updateController();
        }
    }

    public int getPosn() {
        return player.getCurrentPosition();
    }

    public int getDur() {
        return player.getDuration();
    }

    public boolean isPng() {
        return player.isPlaying();
    }

    public void pausePlayer() {

        createNotification(this, songs.get(songPosn), android.R.drawable.ic_media_play);
        player.pause();
    }

    public void seek(int posn) {
        player.seekTo(posn);

    }

    public void go() {
        player.start();
        if(songs !=null)
        createNotification(this, songs.get(songPosn), android.R.drawable.ic_media_pause);
    }

    public void playPrev() {
        songPosn--;
        if (songs!=null && songPosn < 0) songPosn = songs.size() - 1;
        playSong();
    }

    public void playNext() {
        songPosn++;
        if (songs!=null && songPosn >= songs.size())
            songPosn = 0;
        playSong();
    }

    public void playstop() {
        if (player.isPlaying()) {
            player.stop();
        }
    }

    public String getSongName() {
        return songTitle;
    }
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");

            switch (action){
                case CreateNotification.ACTION_PREVIUOS:
                    playPrev();
                    break;
                case CreateNotification.ACTION_PLAY:
                    if (isPng()){
                        pausePlayer();
                    } else {
                        go();
                    }
                    break;
                case CreateNotification.ACTION_NEXT:
                    playNext();
                    break;
            }
        }
    };

}