package com.onnet.audiomusicplayer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import static com.onnet.audiomusicplayer.MainActivity.musicBound;
import static com.onnet.audiomusicplayer.MainActivity.musicSrv;
import static com.onnet.audiomusicplayer.MainActivity.playbackPaused;

public class seekbarFragment extends Fragment implements MediaController.MediaPlayerControl {

    TextView tvSongTitle;
    ImageView ivPlayPause;
    ImageView ivNext;
    ImageView ivPrev;
    SeekBar seekBar;
    TextView tvEndTime;
    TextView tvStartTime;

    public seekbarFragment() {
        // Required empty public constructor
    }

    public static seekbarFragment newInstance() {
        return new seekbarFragment();
    }

    Handler seekBarHandler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seekbar, container, false);
        ivPlayPause = view.findViewById(R.id.playpause);
        ivNext = view.findViewById(R.id.next);
        ivPrev = view.findViewById(R.id.prev);
        seekBar = view.findViewById(R.id.seekbar);
        tvStartTime = view.findViewById(R.id.starttime);
        tvEndTime = view.findViewById(R.id.endtime);
        tvSongTitle = view.findViewById(R.id.songname);


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

        ivPlayPause.setOnClickListener(v -> {
            if (musicSrv.isPng()) {
                musicSrv.pausePlayer();
                ivPlayPause.setImageResource(android.R.drawable.ic_media_play);
            } else {
                musicSrv.go();
                ivPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            }
        });

        ivNext.setOnClickListener(v -> playNext());

        ivPrev.setOnClickListener(v -> playPrev());
        return view;
    }

    public void updateController() {
        long millis = musicSrv.getDur();
        ivPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        seekBar.setMax((int) millis);
        String ms = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        tvEndTime.setText(ms);
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        seekBarHandler.removeCallbacks(seekRunnable);

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

}