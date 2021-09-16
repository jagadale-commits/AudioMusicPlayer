package com.onnet.audiomusicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.HashSet;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private final String TAG = this.getClass().getSimpleName();

    private String songTitle = "";
    private static final int NOTIFY_ID = 123;
    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();
    MainActivity mainActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: called");
        songPosn = 0;
        initMusicPlayer();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Music Player")
                .setContentText(input)
                .setSmallIcon(R.drawable.play)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(NOTIFY_ID, notification);
        return START_NOT_STICKY;
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
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
        MusicService getService() {
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

        playstop();
        player.reset();
        ArrayList<Song> originalSongs = PreferenceHandler.getPlayList("All Songs");
        HashSet<Long> Allsong = new HashSet<>();
        for(Song s : originalSongs)
            Allsong.add(s.getId());
        Song playSong = songs.get(songPosn);

        songTitle = playSong.getTitle();
        long currSong = playSong.getId();
       if(Allsong.contains(currSong)) {
           try {
               Uri trackUri = ContentUris.withAppendedId(
                       android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                       currSong);
               player.setDataSource(getApplicationContext(), trackUri);
               player.prepareAsync();
           } catch (Exception e) {
               Log.e("MUSIC SERVICE", "Error setting data source", e);
           }
       }
       else {
           songs.remove(songPosn);
           playNext();
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
            mainActivity.updateController();
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
        player.pause();
    }

    public void seek(int posn) {
        player.seekTo(posn);

    }

    public void go() {
        player.start();
    }

    public void playPrev() {
        songPosn--;
        if (songPosn < 0) songPosn = songs.size() - 1;
        playSong();
    }

    public void playNext() {
        songPosn++;
        if (songPosn >= songs.size())
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

}