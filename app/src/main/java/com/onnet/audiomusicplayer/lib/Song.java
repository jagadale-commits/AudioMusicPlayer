package com.onnet.audiomusicplayer.lib;

public class Song {
    private long id;
    private String title;
    private String artist;
    private boolean isChecked;



    public Song(long songID, String songTitle, String songArtist, boolean isChecked) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        this.isChecked = isChecked;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getTitle() {
        return title;
    }


    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getArtist() {
        return artist;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

}