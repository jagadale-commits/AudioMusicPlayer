package com.onnet.audiomusicplayer.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.onnet.audiomusicplayer.R;
import com.onnet.audiomusicplayer.lib.Song;

import java.util.ArrayList;

public class SongAdapter extends BaseAdapter {

    private final String TAG = this.getClass().getSimpleName();
    private final ArrayList<Song> songs;
    private final LayoutInflater songInf;

    ViewHolder viewHolder;

    public SongAdapter(Context c, ArrayList<Song> theSongs) {
        songs = theSongs;
        songInf = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return songs.size();
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
            convertView = songInf.inflate
                    (R.layout.song, parent, false);
            viewHolder.songView = convertView.findViewById(R.id.song_title);
            viewHolder.checkBox = convertView.findViewById(R.id.check);
            viewHolder.rlRoot = convertView.findViewById(R.id.rootlayout);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Song currSong = songs.get(position);
        viewHolder.songView.setText(currSong.getTitle());
        viewHolder.checkBox.setChecked(currSong.isChecked());

        viewHolder.rlRoot.setOnClickListener(view -> {
            Log.i(TAG, "onClick: called" + position);
            currSong.setChecked(!currSong.isChecked());
            notifyDataSetChanged();
        });
        return convertView;
    }


    public static class ViewHolder {
        TextView songView;
        CheckBox checkBox;
        RelativeLayout rlRoot;
    }

    public ArrayList<Song> getSelectedItems(){
        ArrayList<Song> selectedSongs = new ArrayList<>();

        for(Song song : songs){
            if(song.isChecked()){
                selectedSongs.add(song);
            }
        }
        return selectedSongs;
    }
}
