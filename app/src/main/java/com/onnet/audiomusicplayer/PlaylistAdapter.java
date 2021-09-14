package com.onnet.audiomusicplayer;

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

import java.util.ArrayList;

class PlaylistAdapter extends BaseAdapter {

    private String TAG = this.getClass().getSimpleName();
    private final ArrayList<String> playlistNames;
    private final ArrayList<Song> songsArrayList;
    private final LayoutInflater songInf;
    Context mContext;
    ViewHolder viewHolder;

    PlaylistAdapter(Context c, ArrayList<String> theSongs, ArrayList<Song> songArrayList) {
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
        viewHolder.ivMoreAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: called: " + position);
                showPopMenu(finalConvertView, position);
            }
        });
        return convertView;
    }

    public void removeSong(int i, String playlistName) {
        songsArrayList.remove(i);
        PreferenceHandler.savePlayList(playlistName, songsArrayList);
        notifyDataSetChanged();
    }


    public class ViewHolder {
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
        popup.getMenuInflater().inflate(R.menu.menu_playlist, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int i = item.getItemId();
                if (i == R.id.play) {
                    //do something
                    Log.i(TAG, "onMenuItemClick: play: " + position);
                    ArrayList<Song> songsList = PreferenceHandler.getPlayList(playlistNames.get(position));
                    musicService.setSongsList(songsList, 0);
                    musicService.playSong();

                    if (onPlayClicklistener == null) {
                        onPlayClicklistener.onPlayClick(position);
                    }

                    return true;
                } else if (i == R.id.view) {
                    //do something
                    Log.i(TAG, "onMenuItemClick: view: " + position);
                    Intent intent = new Intent(mContext, ViewPlayListActivity.class);
                    intent.putExtra("name", playlistNames.get(position));
                    mContext.startActivity(intent);
                    return true;
                } else if (i == R.id.delete) {
                    //do something
                    PreferenceHandler.removePlayList(playlistNames.get(position));
                    playlistNames.remove(position);
                    notifyDataSetChanged();
                    Log.i(TAG, "onMenuItemClick: delete" + position);
                    return true;
                } else if (i == R.id.add) {
                    //do something
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

    OnPlayClicklistener onPlayClicklistener;

    public void setOnPlayClickListener(OnPlayClicklistener onPlayClickListener) {
        this.onPlayClicklistener = onPlayClickListener;
    }

    public interface OnPlayClicklistener {
        public void onPlayClick(int position);
    }

}
