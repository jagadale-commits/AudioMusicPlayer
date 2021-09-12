package com.onnet.audiomusicplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PreferenceHandler {

    public static SharedPreferences sharedPreferences;

    public static void init(Context mContext) {
        sharedPreferences = mContext.getSharedPreferences("MUSICPLayer", Context.MODE_PRIVATE);
    }

    public static ArrayList<String> getAllKeys() {

        ArrayList<String> playList = new ArrayList<>();
        Map<String, ?> keys = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
            playList.add(entry.getKey());
        }
        return playList;
    }

    public static void savePlayList(String playListName, ArrayList<Song> playListItems) {

        String json = new Gson().toJson(playListItems);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(playListName, json);
        editor.commit();
    }

    public static ArrayList<Song> getPlayList(String playListName) {
        String playList = sharedPreferences.getString(playListName, null);

        ArrayList<Song> songsList = new ArrayList<>();
        if (playList == null) {
            return songsList;
        }
        Type type = new TypeToken<List<Song>>() {
        }.getType();
        songsList = new Gson().fromJson(playList, type);
        return songsList;
    }

    public static void removePlayList(String playlistname){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(playlistname);
        editor.commit();
    }
}
