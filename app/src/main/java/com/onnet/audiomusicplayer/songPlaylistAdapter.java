package com.onnet.audiomusicplayer;

import android.content.Context;
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

class songPlaylistAdapter extends BaseAdapter {

    private final String playlistName;
    private String TAG = this.getClass().getSimpleName();
    private final ArrayList<Song> songsArrayList;
    private final LayoutInflater songInf;
    Context mContext;
    ViewHolder viewHolder;

    songPlaylistAdapter(Context c, String playlistName, ArrayList<Song> songArrayList) {
        this.playlistName = playlistName;
        this.songsArrayList = songArrayList;
        songInf = LayoutInflater.from(c);
        mContext = c;
    }

    @Override
    public int getCount() {
            return songsArrayList.size();
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

            Song currSong = songsArrayList.get(position);
            viewHolder.songView.setText(currSong.getTitle());
        if(!playlistName.equals("All Songs"))
            viewHolder.ivMoreAction.setVisibility(View.VISIBLE);
        else
            viewHolder.ivMoreAction.setVisibility(View.GONE);

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



    public class ViewHolder {
        TextView songView;
        ImageView ivMoreAction;
        RelativeLayout rlRoot;
    }

    public void showPopMenu(View finalConvertView, int position) {
        final PopupMenu popup = new PopupMenu(((ViewPlayListActivity) mContext), finalConvertView.findViewById(R.id.moreaction));
        popup.getMenuInflater().inflate(R.menu.menu_song_delete, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int i = item.getItemId();
                if (i == R.id.delete) {
                    songsArrayList.remove(position);
                    PreferenceHandler.savePlayList(playlistName, songsArrayList);
                    notifyDataSetChanged();
                    return true;
                } else {
                    return onMenuItemClick(item);
                }
            }
        });

        popup.show();
    }

}
