package com.onnet.audiomusicplayer.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;

import com.onnet.audiomusicplayer.AddSongActivity;
import com.onnet.audiomusicplayer.MainActivity;
import com.onnet.audiomusicplayer.lib.PreferenceHandler;
import com.onnet.audiomusicplayer.R;
import com.onnet.audiomusicplayer.lib.Song;
import com.onnet.audiomusicplayer.ViewPlayListActivity;
import com.onnet.audiomusicplayer.services.MusicService;

import java.util.ArrayList;

public class PlaylistAdapter extends BaseAdapter {

    private final String TAG = this.getClass().getSimpleName();
    private final ArrayList<String> playlistNames;
    private final ArrayList<Song> songsArrayList;
    private final LayoutInflater songInf;
    Context mContext;
    ViewHolder viewHolder;

    public PlaylistAdapter(Context c, ArrayList<String> theSongs, ArrayList<Song> songArrayList) {
        playlistNames = theSongs;
        this.songsArrayList = songArrayList;
        songInf = LayoutInflater.from(c);
        mContext = c;
    }

    @Override
    public int getCount() {
        if (playlistNames == null) {
            return songsArrayList.size();
        } else {
            return playlistNames.size();
        }

    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {

            viewHolder = new ViewHolder();

            convertView = songInf.inflate(R.layout.playlist_song, parent, false);

            viewHolder.songView = convertView.findViewById(R.id.song_title);
            viewHolder.ivMoreAction = convertView.findViewById(R.id.moreaction);
            viewHolder.rlRoot = convertView.findViewById(R.id.rootlayout);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        if (playlistNames == null) {
            Song currSong = songsArrayList.get(position);
            viewHolder.songView.setText(currSong.getTitle());
            viewHolder.ivMoreAction.setVisibility(View.GONE);
        } else {
            String currSong = playlistNames.get(position);
            viewHolder.songView.setText(currSong);
            viewHolder.ivMoreAction.setVisibility(View.VISIBLE);

        }


        View finalConvertView = convertView;
        viewHolder.ivMoreAction.setOnClickListener(view -> {
            Log.i(TAG, "onClick: called: " + position);
            showPopMenu(finalConvertView, position);
        });
        return convertView;
    }



    public static class ViewHolder {
        TextView songView;
        ImageView ivMoreAction;
        RelativeLayout rlRoot;
    }

    MusicService musicService;

    public void setMusicService(MusicService musicService) {
        this.musicService = musicService;
    }

    public void showPopMenu(View finalConvertView, int position) {
        final PopupMenu popup = new PopupMenu(((MainActivity) mContext), finalConvertView.findViewById(R.id.moreaction));
        if (! playlistNames.get(position).equals("모든 노래"))
        popup.getMenuInflater().inflate(R.menu.menu_playlist, popup.getMenu());
        else
            popup.getMenuInflater().inflate(R.menu.allsong_playlist, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int i = item.getItemId();
                if (i == R.id.play) {
                    Log.i(TAG, "onMenuItemClick: play: " + position);
                    ArrayList<Song> songsList = PreferenceHandler.getPlayList(playlistNames.get(position));
                    musicService.setSongsList(songsList, 0);
                    musicService.playSong();

                    return true;
                } else if (i == R.id.view) {
                    Log.i(TAG, "onMenuItemClick: view: " + position);
                    Intent intent = new Intent(mContext, ViewPlayListActivity.class);
                    intent.putExtra("name", playlistNames.get(position));
                    mContext.startActivity(intent);
                    return true;
                } else if (i == R.id.delete) {
                    PreferenceHandler.removePlayList(playlistNames.get(position));
                    playlistNames.remove(position);
                    notifyDataSetChanged();
                    Log.i(TAG, "onMenuItemClick: delete" + position);
                    return true;
                } else if (i == R.id.add) {
                    Intent intent = new Intent(mContext, AddSongActivity.class);
                    intent.putExtra("name", playlistNames.get(position));
                    mContext.startActivity(intent);
                    return true;
                }else {
                    return onMenuItemClick(item);
                }
            }
        });

        popup.show();
    }

}
